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
import models.SubmissionState
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Results.BadRequest
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ApiService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class SubmissionControllerSpec extends SpecBase {

  private lazy val mockApiService = mock[ApiService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ApiService].toInstance(mockApiService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheRepository)
    reset(mockApiService)
  }

  "post" should {

    "return 200" when {
      "submission is successful" in {
        val userAnswers = emptyUserAnswers
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        val body = Json.toJson("foo")
        when(mockApiService.submitDeclaration(any())(any()))
          .thenReturn(Future.successful(Right(HttpResponse(OK, Json.stringify(body)))))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe body

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService).submitDeclaration(eqTo(userAnswers))(any())
      }
    }

    "return error" when {
      "submission is unsuccessful" in {
        val userAnswers = emptyUserAnswers
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockApiService.submitDeclaration(any())(any()))
          .thenReturn(Future.successful(Left(BadRequest)))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService).submitDeclaration(eqTo(userAnswers))(any())
      }

      "document not found in cache" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withBody(Json.toJson(lrn))

        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockApiService, never()).submitDeclaration(any())(any())
      }

      "request body can't be validated as a string" in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.SubmissionController.post().url)
          .withBody(Json.toJson("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST

        verify(mockCacheRepository, never()).get(any(), any())
        verify(mockApiService, never()).submitDeclaration(any())(any())
      }
    }
  }

  "getSubmissionStatus" should {
    "return submission status" in {
      when(mockApiService.getSubmissionStatus(any())(any()))
        .thenReturn(Future.successful(SubmissionState.Submitted))

      val request = FakeRequest(GET, routes.SubmissionController.getSubmissionStatus(lrn).url)
        .withBody(Json.toJson(lrn))

      val result = route(app, request).value

      status(result) shouldBe OK

      verify(mockApiService).getSubmissionStatus(eqTo(lrn))(any())
    }
  }
}
