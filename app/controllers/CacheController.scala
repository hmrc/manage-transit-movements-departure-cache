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
import controllers.actions.{AuthenticateActionProvider, AuthenticateAndLockActionProvider}
import models.AuditType._
import models.{Metadata, Rejection, SubmissionState, UserAnswers}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.CacheRepository
import services.{AuditService, MetricsService, XPathService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class CacheController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  authenticateAndLock: AuthenticateAndLockActionProvider,
  cacheRepository: CacheRepository,
  auditService: AuditService,
  metricsService: MetricsService,
  xPathService: XPathService
)(implicit ec: ExecutionContext, clock: Clock, appConfig: AppConfig)
    extends BackendController(cc)
    with Logging {

  def get(lrn: String): Action[AnyContent] =
    getUserAnswers[UserAnswers](lrn)(identity)

  def post(lrn: String): Action[JsValue] = authenticateAndLock(lrn).async(parse.json) {
    implicit request =>
      request.body.validate[Metadata] match {
        case JsSuccess(data, _) =>
          if (request.eoriNumber == data.eoriNumber) {
            val status: Option[SubmissionState] = (request.body \ "isSubmitted").asOpt[SubmissionState]
            val departureId: Option[String]     = (request.body \ "departureId").asOpt[String]
            set(data, status, departureId)()
          } else {
            logger.warn(s"Enrolment EORI (${request.eoriNumber}) does not match EORI in user answers (${data.eoriNumber})")
            Future.successful(Forbidden)
          }
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as UserAnswers: $errors")
          Future.successful(BadRequest)
      }
  }

  def put(): Action[JsValue] = authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[String] match {
        case JsSuccess(lrn, _) =>
          set(Metadata(lrn, request.eoriNumber)) {
            val auditType = DepartureDraftStarted
            auditService.audit(auditType, lrn, request.eoriNumber)
            metricsService.increment(auditType.name)
          }
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as String: $errors")
          Future.successful(BadRequest)
      }
  }

  private def set(
    data: Metadata,
    status: Option[SubmissionState] = None,
    departureId: Option[String] = None
  )(block: => Unit = ()): Future[Status] =
    cacheRepository
      .set(data, status, departureId)
      .map {
        case true =>
          block
          Ok
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
          _ =>
            val auditType = DepartureDraftDeleted
            auditService.audit(auditType, lrn, request.eoriNumber)
            metricsService.increment(auditType.name)
            Ok
        }
        .recover {
          case e =>
            logger.error("Failed to delete draft", e)
            InternalServerError
        }
  }

  def getAll(
    lrn: Option[String] = None,
    state: Option[SubmissionState],
    limit: Option[Int] = None,
    skip: Option[Int] = None,
    sortBy: Option[String] = None
  ): Action[AnyContent] =
    authenticate().async {
      implicit request =>
        cacheRepository
          .getAll(request.eoriNumber, lrn, state, limit, skip, sortBy)
          .map(_.toHateoas())
          .map(Ok(_))
          .recover {
            case e =>
              logger.error("Failed to read user answers summary from mongo", e)
              InternalServerError
          }
    }

  def getExpiry(lrn: String): Action[AnyContent] =
    getUserAnswers[Long](lrn)(_.expiryInDays)

  private def getUserAnswers[T](lrn: String)(f: UserAnswers => T)(implicit writes: Writes[T]): Action[AnyContent] =
    authenticate().async {
      implicit request =>
        val eoriNumber = request.eoriNumber
        cacheRepository
          .get(lrn, eoriNumber)
          .map {
            case Some(userAnswers) =>
              Ok(Json.toJson(f(userAnswers)))
            case None =>
              logger.warn(s"No document found for LRN '$lrn' and EORI '$eoriNumber'")
              NotFound
          }
          .recover {
            case e =>
              logger.error("Failed to read user answers from mongo", e)
              InternalServerError
          }
    }

  def handleErrors(lrn: String): Action[JsValue] = authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[Rejection] match {
        case JsSuccess(rejection, _) =>
          cacheRepository.get(lrn, request.eoriNumber).flatMap {
            case Some(userAnswers) =>
              val updatedUserAnswers = xPathService.handleRejection(userAnswers, rejection)
              set(updatedUserAnswers.metadata, Some(updatedUserAnswers.status), updatedUserAnswers.departureId)()
            case None =>
              Future.successful(NotFound)
          }
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as Rejection: $errors")
          Future.successful(BadRequest)
      }
  }
}
