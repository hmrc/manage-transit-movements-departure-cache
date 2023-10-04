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
import models.{Metadata, SubmissionState, UserAnswersSummary}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class CacheControllerSpec extends SpecBase {

  "get" should {

    "return 200" when {
      "read from mongo is successful" in {
        val userAnswers = emptyUserAnswers
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        val request = FakeRequest(GET, routes.CacheController.get(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(userAnswers)
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
      "write to mongo was acknowledged" when {
        "when submission state not in body" in {
          val metaData = emptyMetadata
          when(mockCacheRepository.set(any(): Metadata, any(): Option[SubmissionState]))
            .thenReturn(Future.successful(true))

          val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
            .withBody(Json.toJson(metaData))
          val result = route(app, request).value

          status(result) shouldBe OK
          verify(mockCacheRepository).set(eqTo(metaData), eqTo(None))
        }

        "when submission state in body" in {
          val userAnswers = emptyUserAnswers
          when(mockCacheRepository.set(any(): Metadata, any(): Option[SubmissionState]))
            .thenReturn(Future.successful(true))

          val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
            .withBody(Json.toJson(userAnswers.copy(status = SubmissionState.RejectedPendingChanges)))
          val result = route(app, request).value

          status(result) shouldBe OK
          verify(mockCacheRepository).set(eqTo(userAnswers.metadata), eqTo(Some(SubmissionState.RejectedPendingChanges)))
        }
      }
    }

    "return 400" when {
      "request body is invalid" in {
        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withBody(JsString("foo"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verifyNoInteractions(mockCacheRepository)
      }

      "request body is empty" in {
        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withBody(Json.obj())

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verifyNoInteractions(mockCacheRepository)
      }
    }

    "return 403" when {
      "the EORI in the enrolment and the EORI in user answers do not match" in {
        val metadata    = emptyMetadata.copy(eoriNumber = "different eori")
        val userAnswers = emptyUserAnswers.copy(metadata = metadata)

        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withBody(Json.toJson(userAnswers))

        val result = route(app, request).value

        status(result) shouldBe FORBIDDEN
        verifyNoInteractions(mockCacheRepository)
      }
    }

    "return 500" when {
      "write to mongo was not acknowledged on set" in {
        val metaData = emptyMetadata

        when(mockCacheRepository.set(any(): Metadata, any(): Option[SubmissionState]))
          .thenReturn(Future.successful(false))

        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withBody(Json.toJson(metaData))

        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(eqTo(metaData), eqTo(None))
      }

      "write to mongo fails on set" in {
        val metaData = emptyMetadata
        when(mockCacheRepository.set(any(): Metadata, any(): Option[SubmissionState]))
          .thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(POST, routes.CacheController.post("AB123").url)
          .withBody(Json.toJson(metaData))

        val result = route(app, request).value
        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(eqTo(metaData), eqTo(None))
      }
    }
  }

  "put" should {

    "return 200" when {
      "write to mongo was acknowledged" in {
        when(mockCacheRepository.set(any(): Metadata, any(): Option[SubmissionState]))
          .thenReturn(Future.successful(true))

        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withBody(JsString(lrn))

        val result                                   = route(app, request).value
        val metadataCaptor: ArgumentCaptor[Metadata] = ArgumentCaptor.forClass(classOf[Metadata])

        status(result) shouldBe OK
        verify(mockCacheRepository).set(metadataCaptor.capture(), eqTo(None))
        metadataCaptor.getValue.lrn shouldBe lrn
      }
    }

    "return 400" when {
      "request body is invalid" in {
        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withBody(Json.obj("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verifyNoInteractions(mockCacheRepository)
      }

      "request body is empty" in {
        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withBody(Json.obj())

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verifyNoInteractions(mockCacheRepository)
      }
    }

    "return 500" when {
      "write to mongo was not acknowledged" in {
        when(mockCacheRepository.set(any(): Metadata, any(): Option[SubmissionState]))
          .thenReturn(Future.successful(false))

        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withBody(JsString(lrn))

        val result                                   = route(app, request).value
        val metadataCaptor: ArgumentCaptor[Metadata] = ArgumentCaptor.forClass(classOf[Metadata])

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(metadataCaptor.capture(), eqTo(None))
        metadataCaptor.getValue.lrn shouldBe lrn
      }

      "write to mongo fails" in {
        when(mockCacheRepository.set(any(): Metadata, any(): Option[SubmissionState]))
          .thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(PUT, routes.CacheController.put().url)
          .withBody(JsString(lrn))

        val result                                   = route(app, request).value
        val metadataCaptor: ArgumentCaptor[Metadata] = ArgumentCaptor.forClass(classOf[Metadata])

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).set(metadataCaptor.capture(), eqTo(None))
        metadataCaptor.getValue.lrn shouldBe lrn
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
        val userAnswer1 = emptyUserAnswers.copy(metadata = Metadata("AB123", eoriNumber))
        val userAnswer2 = emptyUserAnswers.copy(metadata = Metadata("CD123", eoriNumber))
        val userAnswers = Seq(userAnswer1, userAnswer2)

        when(mockCacheRepository.getAll(any(), any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(UserAnswersSummary(eoriNumber, userAnswers, 2, 2)))

        val request = FakeRequest(GET, routes.CacheController.getAll().url)
        val result  = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe UserAnswersSummary(eoriNumber, userAnswers, 2, 2).toHateoas()
        verify(mockCacheRepository).getAll(eqTo(eoriNumber), any(), any(), any(), any(), any())
      }
    }

    "return 500" when {
      "read from mongo fails" in {
        when(mockCacheRepository.getAll(any(), any(), any(), any(), any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(GET, routes.CacheController.getAll().url)
        val result  = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).getAll(eqTo(eoriNumber), any(), any(), any(), any(), any())
      }
    }
  }

  "getExpiry" should {

    "return 200" when {
      "read from mongo is successful" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, routes.CacheController.getExpiry(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(30)
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 404" when {
      "document not found in mongo for given lrn and eori number" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.CacheController.getExpiry(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe NOT_FOUND
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return 500" when {
      "read from mongo fails" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(GET, routes.CacheController.getExpiry(lrn).url)
        val result  = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }
  }
}
