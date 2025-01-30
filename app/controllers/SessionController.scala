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
import models.AuditType.*
import play.api.Logging
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class SessionController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  auditService: AuditService,
  metricsService: MetricsService,
  sessionService: SessionService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def delete(lrn: String): Action[AnyContent] = authenticate().async {
    implicit request =>
      sessionService
        .deleteUserAnswersAndLocks(request.eoriNumber, lrn)
        .map {
          _ =>
            val auditType = DepartureDraftDeleted
            auditService.audit(auditType, lrn, request.eoriNumber)
            metricsService.increment(auditType.name)
            Ok
        }
        .recover {
          case e =>
            logger.error("Failed to delete draft and locks", e)
            InternalServerError
        }
  }
}
