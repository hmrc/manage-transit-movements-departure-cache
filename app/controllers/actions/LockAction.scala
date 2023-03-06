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

package controllers.actions

import com.google.inject.Inject
import models.Lock
import models.request.AuthenticatedRequest
import play.api.Logging
import play.api.mvc.Results.{BadRequest, Locked}
import play.api.mvc.{ActionFilter, Result}
import repositories.DefaultLockRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.{Clock, Instant}
import scala.concurrent.{ExecutionContext, Future}

class LockActionProvider @Inject() (repository: DefaultLockRepository, clock: Clock)(implicit ec: ExecutionContext) {

  def apply(lrn: String): ActionFilter[AuthenticatedRequest] =
    new LockAction(lrn, repository, clock)
}

class LockAction(lrn: String, repository: DefaultLockRepository, clock: Clock)(implicit val executionContext: ExecutionContext)
    extends ActionFilter[AuthenticatedRequest]
    with Logging {

  override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId
      .map {
        sessionId =>
          val now = Instant.now(clock)

          val lock: Lock = Lock(
            sessionId = sessionId.value,
            eoriNumber = request.eoriNumber,
            lrn = lrn,
            createdAt = now,
            lastUpdated = now
          )

          repository.lock(lock).map {
            case true  => None
            case false => Some(Locked)
          }
      }
      .getOrElse(Future.successful(Some(BadRequest)))
  }
}
