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

import base.SpecBase
import models.Lock
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.LocalDateTime
import scala.concurrent.Future

class LockControllerSpec extends SpecBase {

  "checkLock" should {

    val lock = Lock(
      sessionId = "sessionId",
      eoriNumber = "AB123",
      lrn = "CD123",
      createdAt = LocalDateTime.now(),
      lastUpdated = LocalDateTime.now()
    )

    "return 200" when {
      "when document is not locked" in {
        when(mockLockRepository.findLocks(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.LockController.checkLock(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe OK
      }

      "when document is locked but header carrier session Id aligns to lock session Id" in {
        when(mockLockRepository.findLocks(any(), any())).thenReturn(Future.successful(Some(lock)))

        val request = FakeRequest(GET, routes.LockController.checkLock(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe OK
      }
    }
  }

}
