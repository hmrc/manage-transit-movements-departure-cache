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
import play.api.mvc.{ActionRefiner, Result}
import repositories.DefaultLockRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class LockActionProvider @Inject() (repository: DefaultLockRepository)(implicit ec: ExecutionContext) {

  def apply(lrn: String): ActionRefiner[AuthenticatedRequest, AuthenticatedRequest] =
    new LockAction(lrn, repository)
}

class LockAction(lrn: String, repository: DefaultLockRepository)(implicit val executionContext: ExecutionContext)
    extends ActionRefiner[AuthenticatedRequest, AuthenticatedRequest]
    with Logging {

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId
      .map {
        sessionId =>
          val lock: Lock = Lock(
            sessionId = sessionId.value,
            eoriNumber = request.eoriNumber,
            lrn = lrn,
            createdAt = LocalDateTime.now(),
            lastUpdated = LocalDateTime.now()
          )

          repository.lock(lock).map {
            case true =>
              Right(request)
            case false =>
              Left(Locked)
          }
      }
      .getOrElse(Future.successful(Left(BadRequest)))
  }
}
