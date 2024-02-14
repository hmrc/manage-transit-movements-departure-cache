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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class DuplicateServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private lazy val mockApiService: ApiService = mock[ApiService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockApiService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ApiService].toInstance(mockApiService)
      )

  private val service = app.injector.instanceOf[DuplicateService]

  "doesIE028ExistForLrn" should {

    "return true" when {
      "true is returned from isIE028DefinedForDeparture" in {

        when(mockApiService.isIE028DefinedForDeparture(any())(any())).thenReturn(Future.successful(true))

        val result = service.doesIE028ExistForLrn(lrn).futureValue

        result mustBe true
      }
    }

    "return false" when {
      "false is returned from isIE028DefinedForDeparture" in {

        when(mockApiService.isIE028DefinedForDeparture(any())(any())).thenReturn(Future.successful(false))

        val result = service.doesIE028ExistForLrn(lrn).futureValue

        result mustBe false
      }
    }
  }

  "doesDraftExistForLrn" should {

    "return true" when {
      "there is a document in cache with the given lrn" in {

        when(mockCacheRepository.doesDraftExistForLrn(eqTo(lrn))).thenReturn(Future.successful(true))

        val result = service.doesDraftExistForLrn(lrn).futureValue

        result mustBe true

        verify(mockCacheRepository).doesDraftExistForLrn(eqTo(lrn))
      }
    }

    "return false" when {
      "there is not a document in the cache with the given lrn" in {

        when(mockCacheRepository.doesDraftExistForLrn(eqTo(lrn))).thenReturn(Future.successful(false))

        val result = service.doesDraftExistForLrn(lrn).futureValue

        result mustBe false

        verify(mockCacheRepository).doesDraftExistForLrn(eqTo(lrn))
      }
    }
  }

  "doesDraftOrSubmissionExistForLrn" should {
    "return true" when {
      "doesIE028ExistForLrn returns departures" in {
        when(mockApiService.isIE028DefinedForDeparture(any())(any()))
          .thenReturn(Future.successful(true))

        val result = service.doesDraftOrSubmissionExistForLrn(lrn).futureValue

        result mustBe true

        verify(mockApiService).isIE028DefinedForDeparture(eqTo(lrn))(any())
      }

      "doesIE028ExistForLrn returns false, but doesDraftExistForLrn returns true" in {
        when(mockApiService.isIE028DefinedForDeparture(any())(any()))
          .thenReturn(Future.successful(false))
        when(mockCacheRepository.doesDraftExistForLrn(any()))
          .thenReturn(Future.successful(true))

        val result = service.doesDraftOrSubmissionExistForLrn(lrn).futureValue

        result mustBe true

        verify(mockApiService).isIE028DefinedForDeparture(eqTo(lrn))(any())
        verify(mockCacheRepository).doesDraftExistForLrn(eqTo(lrn))
      }
    }

    "return false when both doesIE028ExistForLrn and doesDraftExistForLrn return false" in {
      when(mockApiService.isIE028DefinedForDeparture(any())(any()))
        .thenReturn(Future.successful(false))
      when(mockCacheRepository.doesDraftExistForLrn(any()))
        .thenReturn(Future.successful(false))

      val result = service.doesDraftOrSubmissionExistForLrn(lrn).futureValue

      result mustBe false

      verify(mockApiService).isIE028DefinedForDeparture(eqTo(lrn))(any())
    }
  }

  "doesDeclarationExist" should {

    "return true" when {
      "a document exists in the cache for the given LRN and EORI " in {
        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val result = service.doesDeclarationExist(lrn, eoriNumber).futureValue

        result mustBe true

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }

    "return false" when {
      "a document doesn't exist in the cache for the given LRN and EORI" in {

        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val result = service.doesDeclarationExist(lrn, eoriNumber).futureValue

        result mustBe false

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
      }
    }
  }

}
