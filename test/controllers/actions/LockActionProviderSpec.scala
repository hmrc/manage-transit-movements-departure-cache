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

import base.SpecBase
import models.request.AuthenticatedRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.*
import play.api.test.Helpers.*
import repositories.LockRepository
import services.DateTimeService
import uk.gov.hmrc.http.HeaderNames

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LockActionProviderSpec extends SpecBase {

  def baseApplication: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure("metrics.jvm" -> false)

  private val dateTimeService = app.injector.instanceOf[DateTimeService]

  class Harness(lockRepository: LockRepository) extends LockAction(lrn)(lockRepository, dateTimeService) {

    def action(request: AuthenticatedRequest[?]): Future[Option[Result]] =
      filter(request)
  }

  "LockAction" when {

    "when a user has a sessionId" should {
      "return ok after successful lock" in {

        val mockLockRepository = mock[LockRepository]

        when(mockLockRepository.lock(any()))
          .thenReturn(Future.successful(true))

        val harness = new Harness(mockLockRepository)

        val request = AuthenticatedRequest(fakeRequest.withHeaders((HeaderNames.xSessionId, "sessionId")), eoriNumber)

        whenReady(harness.action(request)) {
          result =>
            result shouldEqual None
        }
      }

      "return locked when lock already exists" in {

        val mockLockRepository = mock[LockRepository]

        when(mockLockRepository.lock(any()))
          .thenReturn(Future.successful(false))

        val harness = new Harness(mockLockRepository)

        val request = AuthenticatedRequest(fakeRequest.withHeaders((HeaderNames.xSessionId, "sessionId")), eoriNumber)

        whenReady(harness.action(request)) {
          result =>
            status(Future.successful(result.value)) shouldEqual LOCKED
        }
      }
    }

    "when a user doesn't have a sessionId" should {

      "return bad request" in {

        val mockLockRepository = mock[LockRepository]

        when(mockLockRepository.lock(any()))
          .thenReturn(Future.successful(false))

        val harness = new Harness(mockLockRepository)

        val request = AuthenticatedRequest(fakeRequest, eoriNumber)

        whenReady(harness.action(request)) {
          result =>
            status(Future.successful(result.value)) shouldEqual BAD_REQUEST
        }
      }
    }
  }
}
