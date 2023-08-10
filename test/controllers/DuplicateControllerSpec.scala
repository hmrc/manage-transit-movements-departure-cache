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
import generators.Generators
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsBoolean
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DuplicateService

import scala.concurrent.Future

class DuplicateControllerSpec extends SpecBase with Generators {

  private lazy val mockDuplicateService = mock[DuplicateService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDuplicateService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DuplicateService].toInstance(mockDuplicateService))

  "doesSubmissionExistForLrn" should {

    "return 200 with false" when {
      "lrn does not exist in the API" in {
        when(mockDuplicateService.doesSubmissionExistForLrn(eqTo(lrn))(any())).thenReturn(Future.successful(false))

        val request = FakeRequest(GET, routes.DuplicateController.doesSubmissionExistForLrn(lrn).url)

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(false)
        verify(mockDuplicateService).doesSubmissionExistForLrn(eqTo(lrn))(any())
      }
    }

    "return 200 with true" when {
      "when lrn exists in the API" in {
        when(mockDuplicateService.doesSubmissionExistForLrn(eqTo(lrn))(any())).thenReturn(Future.successful(true))

        val request = FakeRequest(GET, routes.DuplicateController.doesSubmissionExistForLrn(lrn).url)

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(true)
        verify(mockDuplicateService).doesSubmissionExistForLrn(eqTo(lrn))(any())
      }

    }
  }

  "doesDraftOrSubmissionExistForLrn" should {

    "return 200 with false" when {
      "lrn does not exist in the API or the cache" in {
        when(mockDuplicateService.doesSubmissionExistForLrn(any())(any())).thenReturn(Future.successful(false))
        when(mockDuplicateService.doesDraftOrSubmissionExistForLrn(any())(any())).thenReturn(Future.successful(false))

        val request = FakeRequest(GET, routes.DuplicateController.doesDraftOrSubmissionExistForLrn(lrn).url)

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(false)
        verify(mockDuplicateService).doesDraftOrSubmissionExistForLrn(eqTo(lrn))(any())
      }
    }

    "return 200 with true" when {
      "when lrn exists in the API" in {
        when(mockDuplicateService.doesSubmissionExistForLrn(any())(any())).thenReturn(Future.successful(true))
        when(mockDuplicateService.doesDraftOrSubmissionExistForLrn(any())(any())).thenReturn(Future.successful(true))

        val request = FakeRequest(GET, routes.DuplicateController.doesDraftOrSubmissionExistForLrn(lrn).url)

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(true)
        verify(mockDuplicateService).doesDraftOrSubmissionExistForLrn(eqTo(lrn))(any())
      }

      "when lrn exists in the cache, but not the API" in {
        when(mockDuplicateService.doesSubmissionExistForLrn(any())(any())).thenReturn(Future.successful(false))
        when(mockDuplicateService.doesDraftOrSubmissionExistForLrn(any())(any())).thenReturn(Future.successful(true))

        val request = FakeRequest(GET, routes.DuplicateController.doesDraftOrSubmissionExistForLrn(lrn).url)

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(true)
        verify(mockDuplicateService).doesDraftOrSubmissionExistForLrn(eqTo(lrn))(any())
      }

    }
  }
}
