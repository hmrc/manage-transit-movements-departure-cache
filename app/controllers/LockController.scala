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
import models.UserAnswers
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.{CacheRepository, DefaultLockRepository}
import uk.gov.hmrc.mongo.lock.LockRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{Clock, LocalDateTime}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class LockController @Inject() (
  cc: ControllerComponents,
  authenticate: AuthenticateActionProvider,
  lockRepository: DefaultLockRepository
)(implicit ec: ExecutionContext, clock: Clock)
    extends BackendController(cc)
    with Logging {

  def checkLock(lrn: String, sessionId: String): Action[AnyContent] = authenticate().async {
    implicit request =>
      lockRepository.findLocks(request.eoriNumber, lrn).map {
        case Some(value) if sessionId != value.sessionId => Locked
        case _ => Ok
      }
  }
}
