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
import models.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future
import scala.xml.NodeSeq

class ApiServiceSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

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
      forAll(arbitrary[Phase]) {
        version =>
          beforeEach()

          val userAnswers = emptyUserAnswers

          val expectedResult = HttpResponse(OK, "")

          when(mockApiConnector.submitDeclaration(any(), any())(any())).thenReturn(Future.successful(expectedResult))

          val result = service.submitDeclaration(userAnswers, version).futureValue
          result shouldEqual expectedResult

          verify(mockApiConnector).submitDeclaration(eqTo(xml), eqTo(version))(any())
          verify(mockDeclaration).transform(eqTo(userAnswers), eqTo(MovementReferenceNumber.Empty), eqTo(version))
      }
    }
  }

  "submitAmendment" must {
    val mrn = MovementReferenceNumber(Some("mrn"))

    "call connector" in {
      forAll(arbitrary[Phase]) {
        version =>
          beforeEach()

          val userAnswers = emptyUserAnswersWithDepartureId

          val expectedResult = HttpResponse(OK, "")

          when(mockApiConnector.getMRN(any(), any())(any())).thenReturn(Future.successful(mrn))
          when(mockApiConnector.submitAmendment(any(), any(), any())(any())).thenReturn(Future.successful(expectedResult))

          val result = service.submitAmendment(userAnswers, departureId, version).futureValue
          result shouldEqual expectedResult

          verify(mockApiConnector).getMRN(eqTo(departureId), eqTo(version))(any())
          verify(mockApiConnector).submitAmendment(eqTo(departureId), eqTo(xml), eqTo(version))(any())
          verify(mockDeclaration).transform(eqTo(userAnswers), eqTo(mrn), eqTo(version))
      }
    }
  }

  "get" when {
    "no departure found" must {
      "return None" in {
        forAll(arbitrary[Phase]) {
          version =>
            beforeEach()

            when(mockApiConnector.getDeparture(any(), any())(any()))
              .thenReturn(Future.successful(None))

            val result = service.get(lrn, version).futureValue
            result shouldEqual None

            verify(mockApiConnector).getDeparture(eqTo(lrn), eqTo(version))(any())
        }
      }
    }

    "departure found" when {
      "messages found" must {
        "return list of messages" in {
          forAll(arbitrary[Phase]) {
            version =>
              beforeEach()

              val departure = Departure(departureId, lrn)
              val messages  = Messages(Seq(Message("IE015")))

              when(mockApiConnector.getDeparture(any(), any())(any()))
                .thenReturn(Future.successful(Some(departure)))

              when(mockApiConnector.getMessages(any(), any())(any()))
                .thenReturn(Future.successful(messages))

              val result = await(service.get(lrn, version))
              result shouldEqual Some(messages)

              verify(mockApiConnector).getDeparture(eqTo(lrn), eqTo(version))(any())
              verify(mockApiConnector).getMessages(eqTo(departureId), eqTo(version))(any())
          }
        }
      }
    }
  }
}
