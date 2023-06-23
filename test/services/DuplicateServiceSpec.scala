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
import connectors.ApiConnector
import models.{Departure, Departures}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class DuplicateServiceSpec extends AnyFreeSpec with AppWithDefaultMockFixtures with ScalaFutures {

  val lrn                        = "lrn"
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockApiConnector: ApiConnector = mock[ApiConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockApiConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ApiConnector].toInstance(mockApiConnector)
      )

  private val service = app.injector.instanceOf[DuplicateService]

  "apiLRNCheck" - {

    "must return true" - {
      "when Some(_) is returned from getDepartures" in {

        val mockedResponse: Option[Departures] = Some(Departures(Seq(Departure(lrn))))

        // val x = eqTo(Seq("localReferenceNumber" -> lrn)

        when(mockApiConnector.getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())).thenReturn(Future.successful(mockedResponse))

        val result = service.apiLRNCheck(lrn).futureValue

        result mustBe true

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
      }
    }

    "must return false" - {
      "when None is returned from getDepartures" in {

        val mockedResponse = None

        when(mockApiConnector.getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())).thenReturn(Future.successful(mockedResponse))

        val result = service.apiLRNCheck(lrn).futureValue

        result mustBe false

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
      }
    }
  }
}
