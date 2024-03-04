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

import api.submission.Declaration
import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ApiConnector
import generators.Generators
import models._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future
import scala.xml.NodeSeq

class ApiServiceSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private lazy val mockApiConnector = mock[ApiConnector]
  private lazy val mockDeclaration  = mock[Declaration]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ApiConnector].toInstance(mockApiConnector),
        bind[Declaration].toInstance(mockDeclaration)
      )

  private val xml: NodeSeq =
    <ncts:CC013C PhaseID="NCTS5.0" xmlns:ncts="http://ncts.dgtaxud.ec">
      <foo>bar</foo>
    </ncts:CC013C>

  private val mrn = MovementReferenceNumber(Some("mrn"))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockApiConnector)
    reset(mockDeclaration)

    when(mockDeclaration.transform(any(), any(), any()))
      .thenReturn(xml)
  }

  private val service = app.injector.instanceOf[ApiService]

  "submitDeclaration" must {
    "call connector" in {
      val userAnswers = emptyUserAnswers
      val phase       = arbitrary[Phase].sample.value

      val expectedResult = Right(HttpResponse(OK, ""))

      when(mockApiConnector.submitDeclaration(any())(any())).thenReturn(Future.successful(expectedResult))

      val result = service.submitDeclaration(userAnswers, phase).futureValue
      result shouldBe expectedResult

      verify(mockApiConnector).submitDeclaration(eqTo(xml))(any())
    }
  }

  "submitAmend" must {
    "call connector" in {
      val userAnswers = emptyUserAnswersWithDepartureId
      val departureId = "departureId123"
      val phase       = arbitrary[Phase].sample.value

      val expectedResult = Right(HttpResponse(OK, ""))

      when(mockApiConnector.getMRN(any())(any())).thenReturn(Future.successful(mrn))
      when(mockApiConnector.submitAmendment(any(), any())(any())).thenReturn(Future.successful(expectedResult))

      val result = service.submitAmendment(userAnswers, departureId, phase).futureValue
      result shouldBe expectedResult

      verify(mockApiConnector).getMRN(eqTo(departureId))(any())
      verify(mockApiConnector).submitAmendment(eqTo(departureId), eqTo(xml))(any())
    }
  }

  "isIE028DefinedForDeparture" must {
    "must return true" when {
      "IE028 is defined" in {

        val departure      = Departures(Seq(Departure("departureId", "lrn")))
        val departureTypes = DepartureMessageTypes(Seq(DepartureMessageType("IE028")))

        when(mockApiConnector.getDepartures()(any())).thenReturn(Future.successful(departure))
        when(mockApiConnector.getMessages(any())(any())).thenReturn(Future.successful(departureTypes))

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
        when(mockApiConnector.getMessages(any())(any())).thenReturn(Future.successful(departureTypes))

        await(service.isIE028DefinedForDeparture(lrn)) shouldBe false
      }

    }
  }
}
