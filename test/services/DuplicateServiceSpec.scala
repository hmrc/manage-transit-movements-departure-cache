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

import base.AppWithDefaultMockFixtures
import connectors.ApiConnector
import models.{Departure, Departures}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.{CacheRepository, DefaultLockRepository}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class DuplicateServiceSpec extends AnyFreeSpec with AppWithDefaultMockFixtures with ScalaFutures {

  val lrn                        = "lrn"
  val eoriNumber                 = "eoriNumber"
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockApiConnector: ApiConnector            = mock[ApiConnector]
  val mockCacheRepository: CacheRepository      = mock[CacheRepository]
  val mockLockRepository: DefaultLockRepository = mock[DefaultLockRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockApiConnector)
    reset(mockCacheRepository)
    reset(mockLockRepository)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[CacheRepository].toInstance(mockCacheRepository),
        bind[DefaultLockRepository].toInstance(mockLockRepository),
        bind[ApiConnector].toInstance(mockApiConnector)
      )

  private val service = app.injector.instanceOf[DuplicateService]

  "doesSubmissionExistForLrn" - {

    "must return true" - {
      "when Some(_) is returned from getDepartures" in {

        val mockedResponse: Option[Departures] = Some(Departures(Seq(Departure(lrn))))

        when(mockApiConnector.getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())).thenReturn(Future.successful(mockedResponse))

        val result = service.doesSubmissionExistForLrn(lrn).futureValue

        result mustBe true

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
      }
    }

    "must return false" - {
      "when None is returned from getDepartures" in {

        val mockedResponse = None

        when(mockApiConnector.getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())).thenReturn(Future.successful(mockedResponse))

        val result = service.doesSubmissionExistForLrn(lrn).futureValue

        result mustBe false

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
      }
    }
  }

  "doesDraftExistForLrn" - { // TODO Update as part of CTCP-3481

    "must return true" - {
      "when there is a document in cache with the given lrn" in {

        when(mockCacheRepository.existsLRN(eqTo(lrn))).thenReturn(Future.successful(true))

        val result = service.doesDraftExistForLrn(lrn).futureValue

        result mustBe true

        verify(mockCacheRepository).existsLRN(eqTo(lrn))
      }
    }

    "must return false" - {
      "when there is not a document in the cache with the given lrn" in {

        when(mockCacheRepository.existsLRN(eqTo(lrn))).thenReturn(Future.successful(false))

        val result = service.doesDraftExistForLrn(lrn).futureValue

        result mustBe false

        verify(mockCacheRepository).existsLRN(eqTo(lrn))
      }
    }
  }

  "doesDraftOrSubmissionExistForLrn" - {
    "must return true" - {
      "when doesSubmissionExistForLrn returns departures" in {
        when(mockApiConnector.getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any()))
          .thenReturn(Future.successful(Some(Departures(Seq(Departure(lrn))))))

        val result = service.doesDraftOrSubmissionExistForLrn(lrn).futureValue

        result mustBe true

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
      }
      "when doesSubmissionExistForLrn returns no departures and doesDraftExistForLrn returns true" ignore { // TODO CTCP-3481
        when(mockApiConnector.getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())).thenReturn(Future.successful(None))
        when(mockCacheRepository.existsLRN(eqTo(lrn))).thenReturn(Future.successful(true))

        val result = service.doesDraftOrSubmissionExistForLrn(lrn).futureValue

        result mustBe true

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
        verify(mockCacheRepository).existsLRN(eqTo(lrn))
      }
    }

    "must return false when both doesSubmissionExistForLrn and doesDraftExistForLrn return false" in {
      when(mockApiConnector.getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())).thenReturn(Future.successful(None))
      // when(mockCacheRepository.existsLRN(eqTo(lrn))).thenReturn(Future.successful(false)) // TODO CTCP-3481

      val result = service.doesDraftOrSubmissionExistForLrn(lrn).futureValue

      result mustBe false

      verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
      // verify(mockCacheRepository).existsLRN(eqTo(lrn)) // TODO CTCP-3481
    }
  }

}
