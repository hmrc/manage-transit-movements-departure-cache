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
import models.SubmissionState
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
import uk.gov.hmrc.http.HeaderCarrier

@Singleton()
class SubmissionController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  apiService: ApiService,
  cacheRepository: CacheRepository
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def post(): Action[JsValue] =
    authenticate().async(parse.json) {
      implicit request =>
        request.body.validate[String] match {
          case JsSuccess(lrn, _) =>
            cacheRepository.get(lrn, request.eoriNumber).flatMap {
              case Some(uA) =>
                apiService.submitDeclaration(uA).flatMap {
                  case Right(response) =>
                    cacheRepository.set(uA, SubmissionState.Submitted).map {
                      _ =>
                        println("lookherejoe0", response.body)
                        Ok(response.body)
                    }
                  case Left(error) => Future.successful(error)
                }
              case None => Future.successful(InternalServerError)
            }
          case JsError(errors) =>
            logger.warn(s"Failed to validate request body as String: $errors")
            Future.successful(BadRequest)
        }
    }

  def postAmendment(): Action[JsValue] =
    authenticate().async(parse.json) {
      implicit request =>
        val res = request.body.validate[String] match {
          case JsSuccess(lrn, _) =>
            cacheRepository.get(lrn, request.eoriNumber).flatMap {
              case Some(uA) =>
                uA.metadata.departureId match {
                  case Some(value) =>
                    apiService.submitAmmendDeclaration(uA, value).flatMap {
                      case Right(response) =>
                        println("lookherejoe6", response)
                        cacheRepository.set(uA, SubmissionState.Submitted).map {
                          _ =>
                            Ok(response.body)
                        }
                      case Left(error) => Future.successful(error)
                    }
                  case None => Future.successful(InternalServerError)
                }
              case None => Future.successful(InternalServerError)
            }
          case JsError(errors) =>
            logger.warn(s"Failed to validate request body as String: $errors")
            Future.successful(BadRequest)
        }
        res
    }

  def postAmendmentCats: Action[JsValue] =
    authenticate().async(parse.json) {
      implicit request: AuthenticatedRequest[JsValue] =>
        request.body.validate[String] match {
          case JsSuccess(lrn, _) => successAmendment(lrn, request).value.map(_.getOrElse(InternalServerError))

          case JsError(errors) =>
            logger.warn(s"Failed to validate request body as String: $errors")
            Future.successful(BadRequest)
        }

    }

  private def successAmendment(lrn: String, request: AuthenticatedRequest[JsValue])(implicit hc: HeaderCarrier): OptionT[Future, Result] =
    for {
      uA                  <- OptionT(cacheRepository.get(lrn, request.eoriNumber))
      departureId: String <- OptionT.fromOption[Future](uA.metadata.departureId)
      result <- OptionT(apiService.submitAmmendDeclaration(uA, departureId).flatMap {
        case Right(response) =>
          cacheRepository.set(uA, SubmissionState.Submitted).map {
            _ =>
              Option(Ok(response.body))
          }
        case Left(error) => Future.successful(Option(error))
      })

    } yield result

}
