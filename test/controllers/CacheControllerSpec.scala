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
import models.{HateoasUserAnswersSummary, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, verify, when}
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.Future

class CacheControllerSpec extends SpecBase {

  "get" should {

    "return 200" when {
      "read from mongo is successful" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, routes.CacheController.get(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(emptyUserAnswers)
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 404" when {
      "document not found in mongo for given lrn and eori number" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.CacheController.get(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe NOT_FOUND
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 500" when {
      "read from mongo fails" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(GET, routes.CacheController.get(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }
  }

  "post" should {

    "return 200" when {
      "write to mongo was acknowledged" in {
        val userAnswers = emptyUserAnswers
        when(mockCacheRepository.set(any())).thenReturn(Future.successful(true))

        val request = FakeRequest(POST, routes.CacheController.post().url)
          .withBody(Json.toJson(userAnswers))
        val result = route(app, request).value

        status(result) shouldBe OK
        verify(mockCacheRepository).set(eqTo(userAnswers))
      }
    }

    "return 400" when {
      "request body is invalid" in {

        val request = FakeRequest(POST, routes.CacheController.post().url)
          .withBody(JsString("foo"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verify(mockCacheRepository, never()).set(any())
      }

      "request body is empty" in {
        val request = FakeRequest(POST, routes.CacheController.post().url)
          .withBody(Json.obj())

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verify(mockCacheRepository, never()).set(any())
      }
    }

    "return 403" when {
      "the EORI in the enrolment and the EORI in user answers do not match" in {
        val userAnswers = emptyUserAnswers.copy(eoriNumber = "different eori")

        val request = FakeRequest(POST, routes.CacheController.post().url)
          .withBody(Json.toJson(userAnswers))
        val result = route(app, request).value

        status(result) shouldBe FORBIDDEN
        verify(mockCacheRepository, never()).set(any())
      }
    }

    "return 500" when {
      "write to mongo was not acknowledged" in {
        val userAnswers = emptyUserAnswers

        when(mockCacheRepository.set(any())).thenReturn(Future.successful(false))

        val request = FakeRequest(POST, routes.CacheController.post().url)
          .withBody(Json.toJson(userAnswers))
        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(eqTo(userAnswers))
      }

      "write to mongo fails" in {
        val userAnswers = emptyUserAnswers
        when(mockCacheRepository.set(any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(POST, routes.CacheController.post().url)
          .withBody(Json.toJson(userAnswers))

        val result = route(app, request).value
        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(eqTo(userAnswers))
      }
    }
  }

  "delete" should {

    "return 200" when {
      "deletion was successful" in {
        when(mockCacheRepository.remove(any(), any())).thenReturn(Future.successful(true))

        val request = FakeRequest(DELETE, routes.CacheController.delete(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe OK
      }
    }

    "return 500" when {
      "deletion was unsuccessful" in {
        when(mockCacheRepository.remove(any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(DELETE, routes.CacheController.delete(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "getAll" should {

    "return 200" when {

      "read from mongo is successful" in {

        val userAnswer1 = UserAnswers("AB123", eoriNumber, Json.obj(), Map(), LocalDateTime.now(), LocalDateTime.now(), UUID.randomUUID())
        val userAnswer2 = UserAnswers("CD123", eoriNumber, Json.obj(), Map(), LocalDateTime.now(), LocalDateTime.now(), UUID.randomUUID())

        when(mockCacheRepository.getAll(any(), any(), any(), any())).thenReturn(Future.successful(Seq(userAnswer1, userAnswer2)))

        val request = FakeRequest(GET, routes.CacheController.getAll().url)
        val result  = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe HateoasUserAnswersSummary(eoriNumber, Seq(userAnswer1, userAnswer2), 30)
        verify(mockCacheRepository).getAll(eqTo(eoriNumber), any(), any(), any())
      }
    }

    "return 404" when {
      "document not found in mongo for given eori number" in {
        when(mockCacheRepository.getAll(any(), any(), any(), any())).thenReturn(Future.successful(Seq.empty))

        val request = FakeRequest(GET, routes.CacheController.getAll().url)
        val result  = route(app, request).value

        status(result) shouldBe NOT_FOUND
        verify(mockCacheRepository).getAll(eqTo(eoriNumber), any(), any(), any())
      }
    }

    "return 500" when {
      "read from mongo fails" in {
        when(mockCacheRepository.getAll(any(), any(), any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(GET, routes.CacheController.getAll().url)
        val result  = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).getAll(eqTo(eoriNumber), any(), any(), any())
      }
    }
  }

}
