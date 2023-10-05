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

import base.SpecBase
import connectors.ApiConnector
import models.{Departure, DepartureMessageType, DepartureMessageTypes, Departures}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApiServiceSpec extends SpecBase with ScalaFutures {

  private lazy val mockApiConnector = mock[ApiConnector]
  private lazy val mockXPathService = mock[XPathService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ApiConnector].toInstance(mockApiConnector),
        bind[XPathService].toInstance(mockXPathService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockApiConnector)
    reset(mockXPathService)
  }

  private val service = app.injector.instanceOf[ApiService]

  "submitDeclaration" must {
    "call connector" in {
      val userAnswers    = emptyUserAnswers
      val expectedResult = Right(HttpResponse(OK, ""))
      when(mockApiConnector.submitDeclaration(any())(any())).thenReturn(Future.successful(expectedResult))
      val result = service.submitDeclaration(userAnswers).futureValue
      result shouldBe expectedResult
      verify(mockApiConnector).submitDeclaration(eqTo(userAnswers))(any())
    }
  }

  "submitAmmend" must {
    "call connector" in {
      val departureId = "departureId123"
      val userAnswers = emptyUserAnswersWithDepartureId
      val expectedResult = Right(HttpResponse(OK, ""))
      when(mockApiConnector.submitAmmend(any(), any())(any())).thenReturn(Future.successful(expectedResult))
      val result = service.submitAmmendDeclaration(userAnswers, departureId).futureValue
      result shouldBe expectedResult
      verify(mockApiConnector).submitAmmend(eqTo(userAnswers), eqTo(departureId))(any())
    }
  }

  "isIE028DefinedForDeparture" must {
    "must return true" when {
      "IE028 is defined" in {

        val departure      = Departures(Seq(Departure("lrn", "test/path")))
        val departureTypes = DepartureMessageTypes(Seq(DepartureMessageType("IE028")))

        when(mockApiConnector.getDepartures()(any())).thenReturn(Future.successful(departure))
        when(mockApiConnector.getMessageTypesByPath(any())(any(), any(), any())).thenReturn(Future.successful(departureTypes))

        await(service.isIE028DefinedForDeparture(lrn)) shouldBe true
      }

    }

    "must return false" when {

      "LRN does not match any returned movements " in {

        val departure = Departures(Seq(Departure(lrn, "test/path")))

        when(mockApiConnector.getDepartures()(any())).thenReturn(Future.successful(departure))

        await(service.isIE028DefinedForDeparture("invalid MRN")) shouldBe false
      }

      "when returned messages do not contain IE028 " in {

        val departure      = Departures(Seq(Departure("lrn", "test/path")))
        val departureTypes = DepartureMessageTypes(Seq(DepartureMessageType("IE015")))

        when(mockApiConnector.getDepartures()(any())).thenReturn(Future.successful(departure))
        when(mockApiConnector.getMessageTypesByPath(any())(any(), any(), any())).thenReturn(Future.successful(departureTypes))

        await(service.isIE028DefinedForDeparture(lrn)) shouldBe false
      }

    }
  }
}
