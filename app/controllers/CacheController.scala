/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.actions.AuthenticateActionProvider
import models.{Frontend, UserAnswers}
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.CacheRepository.CacheRepositoryProvider
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class CacheController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  cacheRepositoryProvider: CacheRepositoryProvider
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def get(frontend: Frontend, lrn: String): Action[AnyContent] = authenticate().async {
    implicit request =>
      cacheRepositoryProvider(frontend)
        .get(lrn, request.eoriNumber)
        .map {
          case Some(userAnswers) => Ok(Json.toJson(userAnswers))
          case None =>
            logger.error(s"No document found for LRN '$lrn' and EORI '${request.eoriNumber}'")
            NotFound
        }
        .recover {
          case e =>
            logger.error("Failed to read user answers from mongo", e)
            InternalServerError
        }
  }

  def post(frontend: Frontend): Action[JsValue] = authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[UserAnswers] match {
        case JsSuccess(userAnswers, _) =>
          cacheRepositoryProvider(frontend)
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
        case JsError(errors) =>
          logger.error(s"Failed to validate request body as UserAnswers: $errors")
          Future.successful(BadRequest)
      }
  }
}
