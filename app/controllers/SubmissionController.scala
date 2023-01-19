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

import connectors.ApiConnector
import controllers.actions.AuthenticateActionProvider
import play.api.Logging
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.http.HttpReads.{is2xx, is4xx}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class SubmissionController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  cacheRepository: CacheRepository,
  apiConnector: ApiConnector
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def submit(lrn: String): Action[AnyContent] = authenticate().async {
    implicit request =>
      cacheRepository
        .get(lrn, request.eoriNumber)
        .flatMap {
          case Some(userAnswers) =>
            apiConnector.submitDeclaration(userAnswers).map {
              case response if is2xx(response.status) =>
                Ok(lrn)
              case response if is4xx(response.status) =>
                // TODO - log and audit fail. How to handle this?
                BadRequest
              case _ =>
                // TODO - log and audit fail. How to handle this?
                InternalServerError
            }
          case None =>
            logger.warn(s"No document found for LRN '$lrn' and EORI '${request.eoriNumber}'")
            Future.successful(NotFound)
        }
        .recover {
          case e =>
            logger.error("Failed to read user answers from mongo", e)
            InternalServerError
        }
  }
}
