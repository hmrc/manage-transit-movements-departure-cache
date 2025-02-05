/*
 * Copyright 2025 HM Revenue & Customs
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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.Assertion

import scala.concurrent.Future

class SessionServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val sessionService = app.injector.instanceOf[SessionService]

  "SessionService" should {

    "remove user answers and locks for the given LRN and EORI" in {

      when(mockCacheRepository.remove(any(), any())).thenReturn(Future.successful(true))
      when(mockLockRepository.unlock(any(), any())).thenReturn(Future.successful(true))

      val future = sessionService.deleteUserAnswersAndLocks(eoriNumber, lrn)

      whenReady[Unit, Assertion](future) {
        result =>
          result shouldBe ()
      }
    }

    "fail when user answers removal was not acknowledged" in {

      when(mockCacheRepository.remove(any(), any())).thenReturn(Future.successful(false))
      when(mockLockRepository.unlock(any(), any())).thenReturn(Future.successful(true))

      val future = sessionService.deleteUserAnswersAndLocks(eoriNumber, lrn)

      whenReady[Throwable, Assertion](future.failed) {
        result =>
          result shouldBe a[Exception]
      }
    }

    "fail when user answers removal throws exception" in {

      when(mockCacheRepository.remove(any(), any())).thenReturn(Future.failed(new Throwable()))
      when(mockLockRepository.unlock(any(), any())).thenReturn(Future.successful(true))

      val future = sessionService.deleteUserAnswersAndLocks(eoriNumber, lrn)

      whenReady[Throwable, Assertion](future.failed) {
        result =>
          result shouldBe a[Throwable]
      }
    }

    "fail when lock removal was not acknowledged" in {

      when(mockCacheRepository.remove(any(), any())).thenReturn(Future.successful(true))
      when(mockLockRepository.unlock(any(), any())).thenReturn(Future.successful(false))

      val future = sessionService.deleteUserAnswersAndLocks(eoriNumber, lrn)

      whenReady[Throwable, Assertion](future.failed) {
        result =>
          result shouldBe a[Exception]
      }
    }

    "fail when lock removal throws exception" in {

      when(mockCacheRepository.remove(any(), any())).thenReturn(Future.successful(true))
      when(mockLockRepository.unlock(any(), any())).thenReturn(Future.failed(new Throwable()))

      val future = sessionService.deleteUserAnswersAndLocks(eoriNumber, lrn)

      whenReady[Throwable, Assertion](future.failed) {
        result =>
          result shouldBe a[Throwable]
      }
    }
  }
}
