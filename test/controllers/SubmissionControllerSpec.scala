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
import models.*
import models.AuditType.{DeclarationAmendment, DeclarationData}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.{CacheRepository, LockRepository}
import services.{ApiService, AuditService}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class SubmissionControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private lazy val mockCacheRepository = mock[CacheRepository]
  private lazy val mockLockRepository  = mock[LockRepository]
  private lazy val mockApiService      = mock[ApiService]
  private lazy val mockAuditService    = mock[AuditService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[CacheRepository].toInstance(mockCacheRepository),
        bind[LockRepository].toInstance(mockLockRepository),
        bind[ApiService].toInstance(mockApiService),
        bind[AuditService].toInstance(mockAuditService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheRepository)
    reset(mockLockRepository)
    reset(mockApiService)
    reset(mockAuditService)

    when(mockCacheRepository.set(any(): Metadata, any(): Option[String]))
      .thenReturn(Future.successful(true))

    when(mockLockRepository.unlock(any(), any()))
      .thenReturn(Future.successful(true))
  }

  "post" should {

    "return 200" when {
      "submission is successful" in {
        val userAnswers        = emptyUserAnswers
        val updatedUserAnswers = userAnswers.updateStatus(SubmissionState.Submitted)

        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        val body = Json.toJson("foo")
        when(mockApiService.submitDeclaration(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Json.stringify(body))))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withHeaders("API-Version" -> "1.0")
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldEqual OK
        contentAsJson(result) shouldEqual body

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockCacheRepository).set(eqTo(updatedUserAnswers.metadata), eqTo(None))
        verify(mockLockRepository).unlock(eqTo(eoriNumber), eqTo(lrn))
        verify(mockApiService).submitDeclaration(eqTo(userAnswers), eqTo(Phase.Phase5))(any())
        verify(mockAuditService).audit(eqTo(DeclarationData), eqTo(updatedUserAnswers))(any())
      }
    }

    "return error" when {
      "submission is unsuccessful" in {
        val userAnswers = emptyUserAnswers
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockApiService.submitDeclaration(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withHeaders("API-Version" -> "1.0")
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldEqual BAD_REQUEST

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService).submitDeclaration(eqTo(userAnswers), eqTo(Phase.Phase5))(any())
      }

      "document not found in cache" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withHeaders("API-Version" -> "1.0")
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldEqual NOT_FOUND

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService, never()).submitDeclaration(any(), any())(any())
      }

      "request body can't be validated as a string" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withHeaders("API-Version" -> "1.0")
          .withBody(Json.toJson("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldEqual BAD_REQUEST

        verify(mockCacheRepository, never()).get(any(), any())
        verify(mockApiService, never()).submitDeclaration(any(), any())(any())
      }
    }
  }

  "postAmendment" should {

    "return 200" when {
      "submission is successful" in {
        val userAnswers        = emptyUserAnswersWithDepartureId
        val updatedUserAnswers = userAnswers.updateStatus(SubmissionState.Submitted)

        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        val body = Json.toJson("foo")
        when(mockApiService.submitAmendment(any(), any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Json.stringify(body))))

        val request = FakeRequest(POST, routes.SubmissionController.postAmendment().url)
          .withHeaders("API-Version" -> "1.0")
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldEqual OK
        contentAsJson(result) shouldEqual body

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockCacheRepository).set(eqTo(updatedUserAnswers.metadata), eqTo(Some("departureId123")))
        verify(mockLockRepository).unlock(eqTo(eoriNumber), eqTo(lrn))
        verify(mockApiService).submitAmendment(eqTo(userAnswers), eqTo(departureId), eqTo(Phase.Phase5))(any())
        verify(mockAuditService).audit(eqTo(DeclarationAmendment), eqTo(updatedUserAnswers))(any())
      }
    }

    "return error" when {
      "submission is unsuccessful" in {
        val userAnswers = emptyUserAnswersWithDepartureId
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockApiService.submitAmendment(any(), any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))

        val request = FakeRequest(POST, routes.SubmissionController.postAmendment().url)
          .withHeaders("API-Version" -> "1.0")
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldEqual BAD_REQUEST

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService).submitAmendment(eqTo(userAnswers), eqTo(departureId), eqTo(Phase.Phase5))(any())
      }

      "document not found in cache" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.SubmissionController.postAmendment().url)
          .withHeaders("API-Version" -> "1.0")
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldEqual NOT_FOUND

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService, never()).submitAmendment(any(), any(), any())(any())
      }

      "departure ID not present in document" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(POST, routes.SubmissionController.postAmendment().url)
          .withHeaders("API-Version" -> "1.0")
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldEqual NOT_FOUND

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService, never()).submitAmendment(any(), any(), any())(any())
      }

      "request body can't be validated as a string" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.SubmissionController.postAmendment().url)
          .withHeaders("API-Version" -> "1.0")
          .withBody(Json.toJson("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldEqual BAD_REQUEST

        verify(mockCacheRepository, never()).get(any(), any())
        verify(mockApiService, never()).submitAmendment(any(), any(), any())(any())
      }
    }
  }

  "get" should {
    "return 200" when {
      "messages found" in {
        val messages = Messages(Seq(Message("IE015")))

        when(mockApiService.get(any(), any())(any()))
          .thenReturn(Future.successful(Some(messages)))

        val request = FakeRequest(GET, routes.SubmissionController.get(lrn).url)
          .withHeaders("API-Version" -> "1.0")

        val result = route(app, request).value

        status(result) shouldEqual OK
        contentAsJson(result) shouldEqual Json.toJson(messages)

        verify(mockApiService).get(eqTo(lrn), any())(any())
      }
    }

    "return 204" when {
      "no messages found" in {
        when(mockApiService.get(any(), any())(any()))
          .thenReturn(Future.successful(Some(Messages(Nil))))

        val request = FakeRequest(GET, routes.SubmissionController.get(lrn).url)
          .withHeaders("API-Version" -> "1.0")

        val result = route(app, request).value

        status(result) shouldEqual NO_CONTENT

        verify(mockApiService).get(eqTo(lrn), any())(any())
      }
    }

    "return 400" when {
      "invalid API-Version header" in {
        val request = FakeRequest(GET, routes.SubmissionController.get(lrn).url)
          .withHeaders("API-Version" -> "foo")

        val result = route(app, request).value

        status(result) shouldEqual BAD_REQUEST

        verifyNoInteractions(mockApiService)
      }
    }

    "return 404" when {
      "no departure found" in {
        when(mockApiService.get(any(), any())(any()))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.SubmissionController.get(lrn).url)
          .withHeaders("API-Version" -> "1.0")

        val result = route(app, request).value

        status(result) shouldEqual NOT_FOUND

        verify(mockApiService).get(eqTo(lrn), any())(any())
      }
    }
  }

  "rejection" should {
    "return 200" when {
      "functional error conversion is successful" in {
        val functionalErrors = Json.parse("""
            |[
            |  {
            |    "errorPointer" : "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            |    "errorCode" : "12",
            |    "errorReason" : "BR20005"
            |  },
            |  {
            |    "errorPointer" : "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            |    "errorCode" : "12",
            |    "errorReason" : "BR20004",
            |    "originalAttributeValue" : "GB635733627000"
            |  }
            |]
            |""".stripMargin)

        val request = FakeRequest(POST, routes.SubmissionController.rejection().url)
          .withJsonBody(functionalErrors)

        val result = route(app, request).value

        val expectedResult = Json.parse("""
            |[
            |  {
            |    "error" : "12",
            |    "businessRuleId" : "BR20005",
            |    "section" : "Trader details",
            |    "invalidDataItem" : "/CC015C/HolderOfTheTransitProcedure/identificationNumber"
            |  },
            |  {
            |    "error" : "12",
            |    "businessRuleId" : "BR20004",
            |    "section" : "Trader details",
            |    "invalidDataItem" : "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            |    "invalidAnswer" : "GB635733627000"
            |  }
            |]
            |""".stripMargin)

        status(result) shouldEqual OK
        contentAsJson(result) shouldEqual expectedResult
      }
    }

    "return 400" when {
      "request body is invalid" in {
        val request = FakeRequest(POST, routes.SubmissionController.rejection().url)
          .withJsonBody(JsString("foo"))

        val result = route(app, request).value

        status(result) shouldEqual BAD_REQUEST
      }

      "request body is empty" in {
        val request = FakeRequest(POST, routes.SubmissionController.rejection().url)
          .withJsonBody(Json.obj())

        val result = route(app, request).value

        status(result) shouldEqual BAD_REQUEST
      }
    }
  }
}
