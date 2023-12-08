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

import controllers.actions.AuthenticateActionProvider
import models.{SubmissionState, UserAnswers}
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.mvc.{Action, ControllerComponents, Result}
import repositories.CacheRepository
import services.ApiService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import cats.data.OptionT
import cats.implicits._
import models.request.AuthenticatedRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

@Singleton()
class SubmissionController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  apiService: ApiService,
  cacheRepository: CacheRepository
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def post(): Action[JsValue] = authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[String] match {
        case JsSuccess(lrn, _) => successPost(lrn, request.eoriNumber).value.map(_.getOrElse(InternalServerError))
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as String: $errors")
          Future.successful(BadRequest)
      }
  }

  def postAmendment(): Action[JsValue] =
    authenticate().async(parse.json) {
      implicit request: AuthenticatedRequest[JsValue] =>
        request.body.validate[String] match {
          case JsSuccess(lrn, _) => successPostAmendment(lrn, request.eoriNumber).value.map(_.getOrElse(InternalServerError))
          case JsError(errors) =>
            logger.warn(s"Failed to validate request body as String: $errors")
            Future.successful(BadRequest)
        }

    }

  private def successPost(lrn: String, eoriNumber: String)(implicit hc: HeaderCarrier): OptionT[Future, Result] =
    for {
      userAnswers <- OptionT(cacheRepository.get(lrn, eoriNumber))
      result <- OptionT(apiService.submitDeclaration(userAnswers).flatMap {
        responseToResult(userAnswers, _, None, SubmissionState.Submitted)
      })

    } yield result

  private def successPostAmendment(lrn: String, eoriNumber: String)(implicit hc: HeaderCarrier): OptionT[Future, Result] =
    for {
      userAnswers <- OptionT(cacheRepository.get(lrn, eoriNumber))
      departureId <- OptionT.fromOption[Future](userAnswers.departureId)
      result <- OptionT(
        apiService
          .submitAmendDeclaration(userAnswers, departureId)
          .flatMap(responseToResult(userAnswers, _, userAnswers.departureId, SubmissionState.Amendment))
      )

    } yield result

  private def responseToResult(userAnswers: UserAnswers,
                               resultOrResponse: Either[Result, HttpResponse],
                               departureId: Option[String],
                               submissionState: SubmissionState
  ): Future[Option[Result]] =
    resultOrResponse match {
      case Right(response) =>
        cacheRepository
          .set(userAnswers, submissionState, departureId)
          .map(
            _ => Option(Ok(response.body))
          )
      case Left(error) => Future.successful(Option(error))
    }
}
