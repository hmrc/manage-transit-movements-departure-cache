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

import controllers.actions.Actions
import models.*
import models.AuditType.*
import models.Rejection.*
import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.CacheRepository
import services.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton()
class CacheController @Inject() (
  cc: ControllerComponents,
  actions: Actions,
  cacheRepository: CacheRepository,
  auditService: AuditService,
  metricsService: MetricsService,
  xPathService: XPathService,
  dateTimeService: DateTimeService,
  userAnswersSummaryService: UserAnswersSummaryService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def get(lrn: String): Action[AnyContent] =
    getUserAnswers[UserAnswers](lrn)(identity)

  def post(lrn: String): Action[JsValue] = actions.authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[Metadata] match {
        case JsSuccess(data, _) =>
          if (request.eoriNumber == data.eoriNumber) {
            set(data, None)()
          } else {
            logger.warn(s"Enrolment EORI (${request.eoriNumber}) does not match EORI in user answers (${data.eoriNumber})")
            Future.successful(Forbidden)
          }
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as UserAnswers: $errors")
          Future.successful(BadRequest)
      }
  }

  def put(): Action[JsValue] = actions.authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[String] match {
        case JsSuccess(lrn, _) =>
          set(Metadata(lrn, request.eoriNumber, SubmissionState.NotSubmitted), None) {
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
    departureId: Option[String]
  )(block: => Unit = ()): Future[Status] =
    cacheRepository
      .set(data, departureId)
      .map {
        case true =>
          block
          Ok
        case false =>
          logger.error("Write was not acknowledged")
          InternalServerError
      }
      .recover {
        case NonFatal(e) =>
          logger.error("Failed to write user answers to mongo", e)
          InternalServerError
      }

  def getAll(
    lrn: Option[String] = None,
    state: Option[SubmissionState],
    limit: Option[Int] = None,
    skip: Option[Int] = None,
    sortBy: Option[String] = None
  ): Action[AnyContent] =
    actions.authenticate().async {
      implicit request =>
        cacheRepository
          .getAll(request.eoriNumber, lrn, state, limit, skip, sortBy)
          .map(userAnswersSummaryService.toHateoas)
          .map(Ok(_))
          .recover {
            case NonFatal(e) =>
              logger.error("Failed to read user answers summary from mongo", e)
              InternalServerError
          }
    }

  def getExpiry(lrn: String): Action[AnyContent] =
    getUserAnswers[Long](lrn) {
      userAnswers =>
        dateTimeService.expiresInDays(userAnswers.createdAt)
    }

  private def getUserAnswers[T](lrn: String)(f: UserAnswers => T)(implicit writes: Writes[T]): Action[AnyContent] =
    actions.authenticate().async {
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
            case NonFatal(e) =>
              logger.error("Failed to read user answers from mongo", e)
              InternalServerError
          }
    }

  def handleErrors(lrn: String): Action[JsValue] = actions.authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[Rejection] match {
        case JsSuccess(rejection, _) =>
          cacheRepository.get(lrn, request.eoriNumber).flatMap {
            case Some(userAnswers) =>
              val updatedUserAnswers = xPathService.handleRejection(userAnswers, rejection)
              set(updatedUserAnswers.metadata, updatedUserAnswers.departureId)()
            case None =>
              Future.successful(NotFound)
          }
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as Rejection: $errors")
          Future.successful(BadRequest)
      }
  }

  def isRejectionAmendable(lrn: String): Action[JsValue] = actions.authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[Rejection] match {
        case JsSuccess(rejection, _) =>
          xPathService.isRejectionAmendable(lrn, request.eoriNumber, rejection).map(JsBoolean).map(Ok(_))
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as Rejection: $errors")
          Future.successful(BadRequest)
      }
  }

  def prepareForAmendment(lrn: String): Action[JsValue] = actions.authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[String] match {
        case JsSuccess(departureId, _) =>
          cacheRepository.get(lrn, request.eoriNumber).flatMap {
            case Some(userAnswers) =>
              val updatedUserAnswers = xPathService.prepareForAmendment(userAnswers, departureId)
              set(updatedUserAnswers.metadata, updatedUserAnswers.departureId)()
            case None =>
              Future.successful(NotFound)
          }
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as departure ID: $errors")
          Future.successful(BadRequest)
      }
  }

  def copy(lrn: String): Action[JsValue] = actions.authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[String] match {
        case JsSuccess(newLrn, _) =>
          cacheRepository.get(lrn, request.eoriNumber).flatMap {
            case Some(userAnswers) =>
              val updatedUserAnswers = userAnswers.updateLrn(newLrn)
              set(updatedUserAnswers.metadata, None)()
            case None =>
              Future.successful(NotFound)
          }
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as LRN: $errors")
          Future.successful(BadRequest)
      }
  }
}
