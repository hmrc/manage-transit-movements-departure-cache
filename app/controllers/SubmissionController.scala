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

import cats.data.OptionT
import cats.implicits._
import controllers.actions.{AuthenticateActionProvider, VersionedAction}
import models.AuditType._
import models.SubmissionState._
import models.{AuditType, Messages, UserAnswers}
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import repositories.CacheRepository
import services.{ApiService, AuditService, MetricsService}
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class SubmissionController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  getVersion: VersionedAction,
  apiService: ApiService,
  cacheRepository: CacheRepository,
  auditService: AuditService,
  metricsService: MetricsService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def post(): Action[JsValue] =
    (authenticate() andThen getVersion).async(parse.json) {
      implicit request =>
        val auditType = DeclarationData
        request.body.validate[String] match {
          case JsSuccess(lrn, _) =>
            val result = for {
              userAnswers <- OptionT(cacheRepository.get(lrn, request.eoriNumber))
              result <- OptionT.liftF {
                apiService
                  .submitDeclaration(userAnswers, request.phase)
                  .flatMap(responseToResult(userAnswers, _, None, DeclarationData))
              }
            } yield result

            result.value.map(_.getOrElse {
              metricsService.increment(auditType.name, NOT_FOUND)
              logger.error(s"SubmissionController:post:$auditType: Could not find user answers")
              NotFound
            })
          case JsError(errors) =>
            metricsService.increment(auditType.name, BAD_REQUEST)
            logger.warn(s"SubmissionController:post:$auditType: Failed to validate request body as String: $errors")
            Future.successful(BadRequest)
        }
    }

  def postAmendment(): Action[JsValue] =
    (authenticate() andThen getVersion).async(parse.json) {
      implicit request =>
        val auditType = DeclarationAmendment
        request.body.validate[String] match {
          case JsSuccess(lrn, _) =>
            val result = for {
              userAnswers <- OptionT(cacheRepository.get(lrn, request.eoriNumber))
              departureId <- OptionT.fromOption[Future](userAnswers.departureId)
              result <- OptionT.liftF {
                apiService
                  .submitAmendment(userAnswers, departureId, request.phase)
                  .flatMap(responseToResult(userAnswers, _, Some(departureId), auditType))
              }
            } yield result

            result.value.map(_.getOrElse {
              metricsService.increment(auditType.name, NOT_FOUND)
              logger.error(s"SubmissionController:post:$auditType: Could not find user answers, or they did not contain a departure ID")
              NotFound
            })
          case JsError(errors) =>
            metricsService.increment(auditType.name, BAD_REQUEST)
            logger.warn(s"SubmissionController:post:$auditType: Failed to validate request body as String: $errors")
            Future.successful(BadRequest)
        }
    }

  private def responseToResult(
    userAnswers: UserAnswers,
    response: HttpResponse,
    departureId: Option[String],
    auditType: AuditType
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val submissionState = Submitted
    metricsService.increment(auditType.name, response)
    response.status match {
      case status if is2xx(status) =>
        cacheRepository
          .set(userAnswers, submissionState, departureId)
          .map {
            _ =>
              auditService.audit(auditType, userAnswers.copy(status = submissionState))
              Ok(response.body)
          }
      case BAD_REQUEST =>
        logger.warn(s"SubmissionController:post:$auditType: Bad request")
        Future.successful(BadRequest)
      case e =>
        logger.error(s"SubmissionController:post:$auditType: Something went wrong: $e")
        Future.successful(InternalServerError)
    }
  }

  def get(lrn: String): Action[AnyContent] = authenticate().async {
    implicit request =>
      apiService.get(lrn).map {
        case Some(Messages(Nil)) =>
          logger.info(s"No messages found for LRN $lrn")
          NoContent
        case Some(messages) =>
          Ok(Json.toJson(messages))
        case None =>
          logger.warn(s"No departure found for LRN $lrn")
          NotFound
      }
  }
}
