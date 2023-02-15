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
import connectors.ApiConnector
import controllers.actions.AuthenticateActionProvider
import models.UserAnswers
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class SubmissionController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  apiConnector: ApiConnector,
  cacheRepository: CacheRepository,
  appConfig: AppConfig
)(implicit ec: ExecutionContext, clock: Clock)
    extends BackendController(cc)
    with Logging {

  def post(): Action[JsValue] = authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[String] match {
        case JsSuccess(lrn, _) =>
          val maybeUa: Future[Option[UserAnswers]] = cacheRepository.get(lrn, request.eoriNumber)
          maybeUa.flatMap {
            case Some(uA) =>
              apiConnector.submitDeclaration(uA).map {
                case Right(uA)       => Ok (Json.toJson(uA))
                case Left(errorCode) => new Status(errorCode)
              }
            case None => Future.successful(InternalServerError)
          }
        case JsError(errors) =>
          logger.error(s"Failed to validate request body as String: $errors")
          Future.successful(BadRequest)
      }
  }
}
