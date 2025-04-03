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
import cats.implicits.*
import controllers.actions.Actions
import models.*
import models.AuditType.*
import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import repositories.{CacheRepository, LockRepository}
import services.{ApiService, AuditService, MetricsService}
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class SubmissionController @Inject() (
  cc: ControllerComponents,
  actions: Actions,
  apiService: ApiService,
  cacheRepository: CacheRepository,
  lockRepository: LockRepository,
  auditService: AuditService,
  metricsService: MetricsService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  private def log(method: String, message: String, args: String*): String =
    s"SubmissionController:$method:${args.mkString(":")} - $message"

  def post(): Action[JsValue] =
    actions.authenticateAndGetVersion().async(parse.json) {
      implicit request =>
        import request.*
        body.validate[String] match {
          case JsSuccess(lrn, _) =>
            val result = for {
              userAnswers <- OptionT(cacheRepository.get(lrn, eoriNumber))
              result <- OptionT.liftF {
                apiService
                  .submitDeclaration(userAnswers, phase)
                  .flatMap(responseToResult(userAnswers, _, None, DeclarationData.apply))
              }
            } yield result

            result.value.map(_.getOrElse {
              metricsService.increment(DeclarationData.name, NOT_FOUND)
              logger.error(log("post", "Could not find user answers", eoriNumber, lrn))
              NotFound
            })
          case JsError(errors) =>
            metricsService.increment(DeclarationData.name, BAD_REQUEST)
            logger.warn(log("post", "Failed to validate request body as String", eoriNumber))
            Future.successful(BadRequest)
        }
    }

  def postAmendment(): Action[JsValue] =
    actions.authenticateAndGetVersion().async(parse.json) {
      implicit request =>
        import request.*
        body.validate[String] match {
          case JsSuccess(lrn, _) =>
            val result = for {
              userAnswers <- OptionT(cacheRepository.get(lrn, eoriNumber))
              departureId <- OptionT.fromOption[Future](userAnswers.departureId)
              result <- OptionT.liftF {
                apiService
                  .submitAmendment(userAnswers, departureId, phase)
                  .flatMap(responseToResult(userAnswers, _, Some(departureId), DeclarationAmendment.apply))
              }
            } yield result

            result.value.map(_.getOrElse {
              metricsService.increment(DeclarationAmendment.name, NOT_FOUND)
              logger.error(log("postAmendment", "Could not find user answers, or they did not contain a departure ID", eoriNumber, lrn))
              NotFound
            })
          case JsError(errors) =>
            metricsService.increment(DeclarationAmendment.name, BAD_REQUEST)
            logger.warn(log("postAmendment", s"Failed to validate request body as String: $errors", eoriNumber))
            Future.successful(BadRequest)
        }
    }

  private def responseToResult(
    userAnswers: UserAnswers,
    response: HttpResponse,
    departureId: Option[String],
    auditType: (UserAnswers, Int) => AuditType
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val updatedUserAnswers = userAnswers.updateStatus(SubmissionState.Submitted)
    import updatedUserAnswers.{eoriNumber, lrn, metadata}
    metricsService.increment(auditType(updatedUserAnswers, response.status))
    response.status match {
      case status if is2xx(status) =>
        for {
          _ <- cacheRepository.set(metadata, departureId, None)
          _ <- lockRepository.unlock(eoriNumber, lrn)
          _ = auditService.audit(auditType(updatedUserAnswers, response.status))
        } yield Ok(response.body)
      case BAD_REQUEST =>
        logger.warn(log("responseToResult", "Bad request", auditType.toString, eoriNumber, lrn))
        Future.successful(BadRequest)
      case e =>
        logger.error(log("responseToResult", s"Something went wrong: $e", auditType.toString, eoriNumber, lrn))
        Future.successful(InternalServerError)
    }
  }

  def get(lrn: String): Action[AnyContent] = actions.authenticateAndGetVersion().async {
    implicit request =>
      import request.*
      apiService.get(lrn, phase).map {
        case Some(Messages(Nil)) =>
          logger.warn(log("get", "No messages found for LRN", eoriNumber, lrn))
          NoContent
        case Some(messages) =>
          Ok(Json.toJson(messages))
        case None =>
          logger.warn(log("get", "No departure found", eoriNumber, lrn))
          NotFound
      }
  }

  def rejection(): Action[JsValue] = actions.authenticate()(parse.json) {
    implicit request =>
      import request.*
      body.validate[Seq[FunctionalError]] match {
        case JsSuccess(value, _) =>
          val json = value.map(Json.toJson(_))
          Ok(JsArray(json))
        case JsError(errors) =>
          logger.warn(log("rejection", s"Failed to validate request body as functional errors: $errors", eoriNumber))
          BadRequest
      }
  }
}
