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

import base.{AppWithDefaultMockFixtures, SpecBase}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.LockRepository
import uk.gov.hmrc.http.HeaderNames

import scala.concurrent.Future

class LockControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val mockLockRepository: LockRepository = mock[LockRepository]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[LockRepository].toInstance(mockLockRepository))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockLockRepository)
  }

  "checkLock" should {

    "return 200" when {
      "when document is not locked" in {
        when(mockLockRepository.lock(any(), any(), any())).thenReturn(Future.successful(true))

        val request = FakeRequest(GET, routes.LockController.checkLock(lrn).url)
          .withHeaders((HeaderNames.xSessionId, "sessionId"))

        val result = route(app, request).value

        status(result) shouldEqual OK
      }
    }

    "return 423" when {

      "when document is locked but header carrier session Id doesnt align to lock session Id" in {
        when(mockLockRepository.lock(any(), any(), any())).thenReturn(Future.successful(false))

        val request = FakeRequest(GET, routes.LockController.checkLock(lrn).url)
          .withHeaders((HeaderNames.xSessionId, "cd123"))

        val result = route(app, request).value

        status(result) shouldEqual LOCKED
      }
    }

    "return 500" when {

      "when session id is not defined" in {
        val request = FakeRequest(GET, routes.LockController.checkLock(lrn).url)

        val result = route(app, request).value

        status(result) shouldEqual BAD_REQUEST
      }
    }
  }

  "delete lock" should {
    "return 200" when {

      "document is deleted" in {
        when(mockLockRepository.unlock(any(), any(), any())).thenReturn(Future.successful(true))

        val request = FakeRequest(DELETE, routes.LockController.deleteLock(lrn).url)
          .withHeaders((HeaderNames.xSessionId, "sessionId"))

        val result = route(app, request).value

        status(result) shouldEqual OK
      }
    }

    "return 500" when {

      "when lock should exist but isnt found" in {
        when(mockLockRepository.unlock(any(), any(), any())).thenReturn(Future.successful(false))

        val request = FakeRequest(DELETE, routes.LockController.deleteLock(lrn).url)
          .withHeaders((HeaderNames.xSessionId, "sessionId"))

        val result = route(app, request).value

        status(result) shouldEqual INTERNAL_SERVER_ERROR
      }
    }

    "return 500" when {

      "when sessionId is not defined" in {
        when(mockLockRepository.unlock(any(), any(), any())).thenReturn(Future.successful(false))

        val request = FakeRequest(DELETE, routes.LockController.deleteLock(lrn).url)

        val result = route(app, request).value

        status(result) shouldEqual BAD_REQUEST
      }
    }
  }

}
