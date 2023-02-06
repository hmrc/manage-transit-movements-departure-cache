/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import config.AppConfig
import controllers.actions.AuthenticateActionProvider
import models.UserAnswers
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{Clock, LocalDateTime}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class CacheController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  cacheRepository: CacheRepository,
  appConfig: AppConfig
)(implicit ec: ExecutionContext, clock: Clock)
    extends BackendController(cc)
    with Logging {

  // TODO replace with getAll when param's are added
  def get(lrn: String): Action[AnyContent] = authenticate().async {
    implicit request =>
      cacheRepository
        .get(lrn, request.eoriNumber)
        .map {
          case Some(userAnswers) => Ok(Json.toJson(userAnswers))
          case None =>
            logger.warn(s"No document found for LRN '$lrn' and EORI '${request.eoriNumber}'")
            NotFound
        }
        .recover {
          case e =>
            logger.error("Failed to read user answers from mongo", e)
            InternalServerError
        }
  }

  def post(): Action[JsValue] = authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[UserAnswers] match {
        case JsSuccess(userAnswers, _) =>
          if (request.eoriNumber == userAnswers.eoriNumber) {
            set(userAnswers)
          } else {
            logger.error(s"Enrolment EORI (${request.eoriNumber}) does not match EORI in user answers (${userAnswers.eoriNumber})")
            Future.successful(Forbidden)
          }
        case JsError(errors) =>
          logger.error(s"Failed to validate request body as UserAnswers: $errors")
          Future.successful(BadRequest)
      }
  }

  def put(): Action[JsValue] = authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[String] match {
        case JsSuccess(lrn, _) =>
          val now = LocalDateTime.now(clock)
          val userAnswers = UserAnswers(
            lrn = lrn,
            eoriNumber = request.eoriNumber,
            data = Json.obj(),
            tasks = Map.empty,
            createdAt = now,
            lastUpdated = now,
            id = UUID.randomUUID()
          )
          set(userAnswers)
        case JsError(errors) =>
          logger.error(s"Failed to validate request body as String: $errors")
          Future.successful(BadRequest)
      }
  }

  private def set(userAnswers: UserAnswers): Future[Status] =
    cacheRepository
      .set(userAnswers)
      .map {
        case true => Ok
        case false =>
          logger.error("Write was not acknowledged")
          InternalServerError
      }
      .recover {
        case e =>
          logger.error("Failed to write user answers to mongo", e)
          InternalServerError
      }

  def delete(lrn: String): Action[AnyContent] = authenticate().async {
    implicit request =>
      cacheRepository
        .remove(lrn, request.eoriNumber)
        .map {
          _ => Ok
        }
        .recover {
          case e =>
            logger.error("Failed to delete draft", e)
            InternalServerError
        }
  }

  def getAll(lrn: Option[String] = None, limit: Option[Int] = None, skip: Option[Int] = None): Action[AnyContent] = authenticate().async {
    implicit request =>
      cacheRepository
        .getAll(request.eoriNumber, lrn, limit, skip)
        .map {
          case result if result.userAnswers.nonEmpty => Ok(result.toHateoas())
          case _ =>
            logger.warn(s"No documents found for EORI: '${request.eoriNumber}'")
            NotFound
        }
        .recover {
          case e =>
            logger.error("Failed to read user answers summary from mongo", e)
            InternalServerError
        }
  }
}
