/*
 * Copyright 2022 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, verify, when}
import play.api.http.Status
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CacheControllerSpec extends SpecBase {

  private val controller = new CacheController(Helpers.stubControllerComponents(), mockCacheRepository)

  "get" should {

    val fakeRequest = FakeRequest("GET", "/")

    "return 200" when {
      "read from mongo is successful" in {
        val userAnswers = emptyUserAnswers
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))
        val result = controller.get(lrn, eori)(fakeRequest)
        status(result) shouldBe Status.OK
        contentAsJson(result) shouldBe Json.toJson(userAnswers)
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eori))
      }
    }

    "return 404" when {
      "document not found in mongo for given lrn and eori number" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))
        val result = controller.get(lrn, eori)(fakeRequest)
        status(result) shouldBe Status.NOT_FOUND
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eori))
      }
    }

    "return 500" when {
      "read from mongo fails" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.failed(new Throwable()))
        val result = controller.get(lrn, eori)(fakeRequest)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eori))
      }
    }
  }

  "post" should {

    "return 200" when {
      "write to mongo was acknowledged" in {
        val userAnswers = emptyUserAnswers
        val fakeRequest = FakeRequest("POST", "/").withBody(Json.toJson(userAnswers))
        when(mockCacheRepository.set(any())).thenReturn(Future.successful(true))
        val result = controller.post()(fakeRequest)
        status(result) shouldBe Status.OK
        verify(mockCacheRepository).set(eqTo(userAnswers))
      }
    }

    "return 400" when {
      "request body is invalid" in {
        val fakeRequest = FakeRequest("POST", "/").withBody(JsString("foo"))
        val result      = controller.post()(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
        verify(mockCacheRepository, never()).set(any())
      }

      "request body is empty" in {
        val fakeRequest = FakeRequest("POST", "/").withBody(Json.obj())
        val result      = controller.post()(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
        verify(mockCacheRepository, never()).set(any())
      }
    }

    "return 500" when {
      "write to mongo was not acknowledged" in {
        val userAnswers = emptyUserAnswers
        val fakeRequest = FakeRequest("POST", "/").withBody(Json.toJson(userAnswers))
        when(mockCacheRepository.set(any())).thenReturn(Future.successful(false))
        val result = controller.post()(fakeRequest)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(eqTo(userAnswers))
      }

      "write to mongo fails" in {
        val userAnswers = emptyUserAnswers
        val fakeRequest = FakeRequest("POST", "/").withBody(Json.toJson(userAnswers))
        when(mockCacheRepository.set(any())).thenReturn(Future.failed(new Throwable()))
        val result = controller.post()(fakeRequest)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(eqTo(userAnswers))
      }
    }
  }
}
