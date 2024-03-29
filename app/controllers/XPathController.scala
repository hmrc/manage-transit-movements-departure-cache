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
import models.XPath
import play.api.Logging
import play.api.libs.json.{JsBoolean, JsError, JsSuccess, JsValue}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.XPathService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class XPathController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  xPathService: XPathService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def isDeclarationAmendable(lrn: String): Action[JsValue] = authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[Seq[XPath]] match {
        case JsSuccess(xPaths, _) =>
          xPathService.isDeclarationAmendable(lrn, request.eoriNumber, xPaths).map(JsBoolean).map(Ok(_))
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as sequence of xPaths: $errors")
          Future.successful(BadRequest)
      }
  }

  def handleErrors(lrn: String): Action[JsValue] = authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[Seq[XPath]] match {
        case JsSuccess(xPaths, _) =>
          xPathService.handleErrors(lrn, request.eoriNumber, xPaths).map(JsBoolean).map(Ok(_))
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as sequence of xPaths: $errors")
          Future.successful(BadRequest)
      }
  }

  def handleAmendmentErrors(lrn: String): Action[JsValue] = authenticate().async(parse.json) {
    implicit request =>
      request.body.validate[Seq[XPath]] match {
        case JsSuccess(xPaths, _) =>
          xPathService.handleAmendmentErrors(lrn, request.eoriNumber, xPaths).map(JsBoolean).map(Ok(_))
        case JsError(errors) =>
          logger.warn(s"Failed to validate request body as sequence of xPaths: $errors")
          Future.successful(BadRequest)
      }
  }

  def handleGuaranteeErrors(lrn: String): Action[AnyContent] = authenticate().async {
    implicit request =>
      xPathService.handleGuaranteeErrors(lrn, request.eoriNumber).map(JsBoolean).map(Ok(_))
  }

}
