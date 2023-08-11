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
import models._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HttpResponse

import java.time.Instant
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

  "getDeparturesForLrn" must {
    "call connector with expected query param" in {
      val expectedResult = Some(Nil)
      when(mockApiConnector.getDepartures(any())(any())).thenReturn(Future.successful(expectedResult))
      val result = service.getDeparturesForLrn(lrn).futureValue
      result shouldBe expectedResult
      verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
    }
  }

  "getSubmissionStatus" must {
    "return Submitted" when {
      "most recently received message is an IE015" in {
        val departures = Seq(
          Departure("departureId1", "lrn1", Instant.ofEpochMilli(1667569012332L)),
          Departure("departureId2", "lrn2", Instant.ofEpochMilli(1667568475522L))
        )

        val messages = Seq(
          DepartureMessage("messageId", "IE015", Instant.ofEpochMilli(1667569012332L))
        )

        when(mockApiConnector.getDepartures(any())(any())).thenReturn(Future.successful(Some(departures)))
        when(mockApiConnector.getDepartureMessages(any())(any())).thenReturn(Future.successful(Some(messages)))

        val result = service.getSubmissionStatus(lrn, eoriNumber).futureValue
        result shouldBe SubmissionState.Submitted

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
        verify(mockApiConnector).getDepartureMessages(eqTo("departureId1"))(any())
      }

      "most recently received message is an IE056 and is not amendable" in {
        val departures = Seq(
          Departure("departureId1", "lrn1", Instant.ofEpochMilli(1667569012332L)),
          Departure("departureId2", "lrn2", Instant.ofEpochMilli(1667568475522L))
        )

        val messages = Seq(
          DepartureMessage("messageId1", "IE015", Instant.ofEpochMilli(1667568475522L)),
          DepartureMessage("messageId2", "IE056", Instant.ofEpochMilli(1667569012332L))
        )

        val functionalErrors = Seq(
          FunctionalError("/CC015C/Authorisation[1]/referenceNumber"),
          FunctionalError("/CC015C/Guarantee[1]/guaranteeType")
        )

        val xPaths = Seq(
          XPath("/CC015C/Authorisation[1]/referenceNumber"),
          XPath("/CC015C/Guarantee[1]/guaranteeType")
        )

        when(mockApiConnector.getDepartures(any())(any())).thenReturn(Future.successful(Some(departures)))
        when(mockApiConnector.getDepartureMessages(any())(any())).thenReturn(Future.successful(Some(messages)))
        when(mockApiConnector.getDepartureMessage[IE056Message](any(), any())(any(), any()))
          .thenReturn(Future.successful(IE056Message(IE056Body(functionalErrors))))
        when(mockXPathService.isDeclarationAmendable(any(), any(), any())).thenReturn(Future.successful(false))

        val result = service.getSubmissionStatus(lrn, eoriNumber).futureValue
        result shouldBe SubmissionState.Submitted

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
        verify(mockApiConnector).getDepartureMessages(eqTo("departureId1"))(any())
        verify(mockApiConnector).getDepartureMessage[IE056Message](eqTo("departureId1"), eqTo("messageId2"))(any(), any())
        verify(mockXPathService).isDeclarationAmendable(eqTo(lrn), eqTo(eoriNumber), eqTo(xPaths))
      }
    }

    "return RejectedPendingChanges" when {
      "most recently received message is an IE056 and is amendable" in {
        val departures = Seq(
          Departure("departureId1", "lrn1", Instant.ofEpochMilli(1667569012332L)),
          Departure("departureId2", "lrn2", Instant.ofEpochMilli(1667568475522L))
        )

        val messages = Seq(
          DepartureMessage("messageId1", "IE015", Instant.ofEpochMilli(1667568475522L)),
          DepartureMessage("messageId2", "IE056", Instant.ofEpochMilli(1667569012332L))
        )

        val functionalErrors = Seq(
          FunctionalError("/CC015C/Authorisation[1]/referenceNumber"),
          FunctionalError("/CC015C/Guarantee[1]/guaranteeType")
        )

        val xPaths = Seq(
          XPath("/CC015C/Authorisation[1]/referenceNumber"),
          XPath("/CC015C/Guarantee[1]/guaranteeType")
        )

        when(mockApiConnector.getDepartures(any())(any())).thenReturn(Future.successful(Some(departures)))
        when(mockApiConnector.getDepartureMessages(any())(any())).thenReturn(Future.successful(Some(messages)))
        when(mockApiConnector.getDepartureMessage[IE056Message](any(), any())(any(), any()))
          .thenReturn(Future.successful(IE056Message(IE056Body(functionalErrors))))
        when(mockXPathService.isDeclarationAmendable(any(), any(), any())).thenReturn(Future.successful(true))

        val result = service.getSubmissionStatus(lrn, eoriNumber).futureValue
        result shouldBe SubmissionState.RejectedPendingChanges

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
        verify(mockApiConnector).getDepartureMessages(eqTo("departureId1"))(any())
        verify(mockApiConnector).getDepartureMessage[IE056Message](eqTo("departureId1"), eqTo("messageId2"))(any(), any())
        verify(mockXPathService).isDeclarationAmendable(eqTo(lrn), eqTo(eoriNumber), eqTo(xPaths))
      }
    }

    "return NotSubmitted" when {
      "error retrieving departures for LRN" in {
        when(mockApiConnector.getDepartures(any())(any())).thenReturn(Future.successful(None))

        val result = service.getSubmissionStatus(lrn, eoriNumber).futureValue
        result shouldBe SubmissionState.NotSubmitted

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
      }

      "no departures found for LRN" in {
        when(mockApiConnector.getDepartures(any())(any())).thenReturn(Future.successful(Some(Nil)))

        val result = service.getSubmissionStatus(lrn, eoriNumber).futureValue
        result shouldBe SubmissionState.NotSubmitted

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
      }

      "error retrieving messages for departure ID" in {
        val departures = Seq(
          Departure("departureId1", "lrn1", Instant.ofEpochMilli(1667569012332L)),
          Departure("departureId2", "lrn2", Instant.ofEpochMilli(1667568475522L))
        )

        when(mockApiConnector.getDepartures(any())(any())).thenReturn(Future.successful(Some(departures)))
        when(mockApiConnector.getDepartureMessages(any())(any())).thenReturn(Future.successful(None))

        val result = service.getSubmissionStatus(lrn, eoriNumber).futureValue
        result shouldBe SubmissionState.NotSubmitted

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
        verify(mockApiConnector).getDepartureMessages(eqTo("departureId1"))(any())
      }

      "no messages found for departure ID" in {
        val departures = Seq(
          Departure("departureId1", "lrn1", Instant.ofEpochMilli(1667569012332L)),
          Departure("departureId2", "lrn2", Instant.ofEpochMilli(1667568475522L))
        )

        when(mockApiConnector.getDepartures(any())(any())).thenReturn(Future.successful(Some(departures)))
        when(mockApiConnector.getDepartureMessages(any())(any())).thenReturn(Future.successful(Some(Nil)))

        val result = service.getSubmissionStatus(lrn, eoriNumber).futureValue
        result shouldBe SubmissionState.NotSubmitted

        verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
        verify(mockApiConnector).getDepartureMessages(eqTo("departureId1"))(any())
      }
    }
  }
}