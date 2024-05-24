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
import models.AuditType.{DeclarationAmendment, DeclarationData}
import models._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{ApiService, AuditService}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class SubmissionControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private lazy val mockApiService = mock[ApiService]

  private lazy val mockAuditService = mock[AuditService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ApiService].toInstance(mockApiService),
        bind[AuditService].toInstance(mockAuditService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheRepository)
    reset(mockApiService)
    reset(mockAuditService)

    when(mockCacheRepository.set(any(): UserAnswers, any(): SubmissionState, any(): Option[String])).thenReturn(Future.successful(true))
  }

  "post" should {

    "return 200" when {
      "submission is successful" in {
        val userAnswers = emptyUserAnswers
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        val body = Json.toJson("foo")
        when(mockApiService.submitDeclaration(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Json.stringify(body))))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe body

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockCacheRepository).set(eqTo(userAnswers), eqTo(SubmissionState.Submitted), eqTo(None))
        verify(mockApiService).submitDeclaration(eqTo(userAnswers), eqTo(Phase.Transition))(any())
        verify(mockAuditService).audit(eqTo(DeclarationData), eqTo(userAnswers.copy(status = SubmissionState.Submitted)))(any())
      }
    }

    "return error" when {
      "submission is unsuccessful" in {
        val userAnswers = emptyUserAnswers
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockApiService.submitDeclaration(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService).submitDeclaration(eqTo(userAnswers), eqTo(Phase.Transition))(any())
      }

      "document not found in cache" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldBe NOT_FOUND

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService, never()).submitDeclaration(any(), any())(any())
      }

      "request body can't be validated as a string" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withBody(Json.toJson("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST

        verify(mockCacheRepository, never()).get(any(), any())
        verify(mockApiService, never()).submitDeclaration(any(), any())(any())
      }
    }
  }

  "postAmendment" should {

    "return 200" when {
      "submission is successful" in {
        val userAnswers = emptyUserAnswersWithDepartureId
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        val body = Json.toJson("foo")
        when(mockApiService.submitAmendment(any(), any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Json.stringify(body))))

        val request = FakeRequest(POST, routes.SubmissionController.postAmendment().url)
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe body

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockCacheRepository).set(eqTo(userAnswers), eqTo(SubmissionState.Submitted), eqTo(Some("departureId123")))
        verify(mockApiService).submitAmendment(eqTo(userAnswers), eqTo(departureId), eqTo(Phase.Transition))(any())
        verify(mockAuditService).audit(eqTo(DeclarationAmendment), eqTo(userAnswers.copy(status = SubmissionState.Submitted)))(any())
      }
    }

    "return error" when {
      "submission is unsuccessful" in {
        val userAnswers = emptyUserAnswersWithDepartureId
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockApiService.submitAmendment(any(), any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))

        val request = FakeRequest(POST, routes.SubmissionController.postAmendment().url)
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService).submitAmendment(eqTo(userAnswers), eqTo(departureId), eqTo(Phase.Transition))(any())
      }

      "document not found in cache" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.SubmissionController.postAmendment().url)
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldBe NOT_FOUND

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService, never()).submitAmendment(any(), any(), any())(any())
      }

      "departure ID not present in document" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(POST, routes.SubmissionController.postAmendment().url)
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldBe NOT_FOUND

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService, never()).submitAmendment(any(), any(), any())(any())
      }

      "request body can't be validated as a string" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.SubmissionController.postAmendment().url)
          .withBody(Json.toJson("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST

        verify(mockCacheRepository, never()).get(any(), any())
        verify(mockApiService, never()).submitAmendment(any(), any(), any())(any())
      }
    }
  }

  "get" should {
    "return 200" when {
      "messages found" in {
        val messages = Messages(Seq(Message("IE015")))

        when(mockApiService.get(any())(any()))
          .thenReturn(Future.successful(Some(messages)))

        val request = FakeRequest(GET, routes.SubmissionController.get(lrn).url)

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(messages)

        verify(mockApiService).get(eqTo(lrn))(any())
      }
    }

    "return 204" when {
      "no messages found" in {
        when(mockApiService.get(any())(any()))
          .thenReturn(Future.successful(Some(Messages(Nil))))

        val request = FakeRequest(GET, routes.SubmissionController.get(lrn).url)

        val result = route(app, request).value

        status(result) shouldBe NO_CONTENT

        verify(mockApiService).get(eqTo(lrn))(any())
      }
    }

    "return 404" when {
      "no departure found" in {
        when(mockApiService.get(any())(any()))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.SubmissionController.get(lrn).url)

        val result = route(app, request).value

        status(result) shouldBe NOT_FOUND

        verify(mockApiService).get(eqTo(lrn))(any())
      }
    }
  }
}
