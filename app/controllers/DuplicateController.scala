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

import controllers.actions.{AuthenticateActionProvider, AuthenticateAndLockActionProvider}
import play.api.Logging
import play.api.libs.json.JsBoolean
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.DuplicateService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class DuplicateController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  authenticateAndLock: AuthenticateAndLockActionProvider,
  duplicateService: DuplicateService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def isDuplicateLRN(lrn: String): Action[AnyContent] = authenticate().async {
    implicit request =>
      duplicateService.isDuplicateLRN(lrn, request.eoriNumber).map(JsBoolean).map(Ok(_))
  }

  def apiLRNCheck(lrn: String): Action[AnyContent] = authenticate().async {
    implicit request =>
      duplicateService.apiLRNCheck(lrn).map(JsBoolean).map(Ok(_))
  }

}
