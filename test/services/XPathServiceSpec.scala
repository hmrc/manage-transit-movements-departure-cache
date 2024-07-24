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
import cats.data.NonEmptyList
import models.Rejection.{BusinessRejectionType, IE055Rejection, IE056Rejection}
import models.Status.Completed
import models.Task._
import models._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, verifyNoInteractions, verifyNoMoreInteractions, when}
import play.api.libs.json.JsObject

import scala.concurrent.Future

class XPathServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val service = app.injector.instanceOf[XPathService]

  private val unamendableXPath = XPath("/CC014C")

  private val amendableXPath = XPath("/CC015C/Authorisation[1]/referenceNumber")

  "isDeclarationAmendable" must {

    "return true" when {
      "a document exists in the cache for the given LRN and EORI " +
        "and at least one of the errors is amendable" in {

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val xPaths = Seq.fill(9)(unamendableXPath) :+ amendableXPath

          val result = service.isDeclarationAmendable(lrn, eoriNumber, xPaths).futureValue

          result shouldBe true

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }
    }

    "return false" when {
      "a document doesn't exist in the cache for the given LRN and EORI " +
        "and there are 10 or fewer errors " +
        "and at least one of the errors is amendable" in {

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

          val xPaths = Seq.fill(9)(unamendableXPath) :+ amendableXPath

          val result = service.isDeclarationAmendable(lrn, eoriNumber, xPaths).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }

      "a document exists in the cache for the given LRN and EORI " +
        "and none of the errors are amendable" in {

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val xPaths = Seq.fill(10)(unamendableXPath)

          val result = service.isDeclarationAmendable(lrn, eoriNumber, xPaths).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }
    }
  }

  "handleErrors" must {
    val xPaths = Seq(
      XPath("/CC015C/Authorisation/referenceNumber"),
      XPath("/CC015C/CustomsOfficeOfDeparture/customsOffice"),
      XPath("/CC015C/Representative/firstName")
    )

    "return true" when {
      "a document exists in the cache for the given LRN and EORI " +
        "and setting the document tasks to error succeeds" in {

          val metaData    = Metadata(lrn, eoriNumber, JsObject.empty, tasks = Map(".preTaskList" -> Completed, ".documents" -> Completed))
          val userAnswers = emptyUserAnswers.copy(metaData)

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))
          when(mockCacheRepository.set(any(): UserAnswers, any(): SubmissionState, any(): Option[String])).thenReturn(Future.successful(true))

          val result = service.handleErrors(lrn, eoriNumber, xPaths).futureValue

          val expectedMeta = Metadata(
            lrn,
            eoriNumber,
            JsObject.empty,
            tasks = Map(
              ".preTaskList"      -> Status.Error,
              ".transportDetails" -> Status.Error,
              ".traderDetails"    -> Status.Error,
              ".documents"        -> Status.Completed
            )
          )

          val expectedUa = emptyUserAnswers.copy(metadata = expectedMeta)

          result shouldBe true

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          verify(mockCacheRepository).set(eqTo(expectedUa), eqTo(SubmissionState.RejectedPendingChanges), eqTo(None))
        }
    }

    "return false" when {
      "a document doesn't exist in the cache for the given LRN and EORI" in {

        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val result = service.handleErrors(lrn, eoriNumber, xPaths).futureValue

        result shouldBe false

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verifyNoMoreInteractions(mockCacheRepository)
      }

      "a document exists in the cache for the given LRN and EORI " +
        "and setting the document tasks to error fails" in {

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockCacheRepository.set(any(): UserAnswers, any(): SubmissionState, eqTo(None))).thenReturn(Future.successful(false))

          val result = service.handleErrors(lrn, eoriNumber, xPaths).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          verify(mockCacheRepository).set(any(), eqTo(SubmissionState.RejectedPendingChanges), eqTo(None))
        }

      "there are no tasks to update" in {

        val xPaths = Seq.empty

        val result = service.handleErrors(lrn, eoriNumber, xPaths).futureValue

        result shouldBe false

        verifyNoInteractions(mockCacheRepository)
      }
    }
  }

  "handleAmendmentErrors" must {
    val xPaths = Seq(
      XPath("/CC015C/Authorisation/referenceNumber"),
      XPath("/CC015C/CustomsOfficeOfDeparture/customsOffice"),
      XPath("/CC015C/Representative/firstName")
    )

    "when there is xPaths return true" when {
      "a document exists in the cache for the given LRN and EORI " +
        "and setting the document tasks to error succeeds" in {

          val metaData    = Metadata(lrn, eoriNumber, JsObject.empty, tasks = Map(".preTaskList" -> Completed, ".documents" -> Completed))
          val userAnswers = emptyUserAnswers.copy(metaData)

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))
          when(mockCacheRepository.set(any(): UserAnswers, any(): SubmissionState, any(): Option[String])).thenReturn(Future.successful(true))

          val result = service.handleAmendmentErrors(lrn, eoriNumber, xPaths).futureValue

          val expectedMeta = Metadata(
            lrn,
            eoriNumber,
            JsObject.empty,
            tasks = Map(
              ".preTaskList"      -> Status.Error,
              ".transportDetails" -> Status.Error,
              ".traderDetails"    -> Status.Error,
              ".documents"        -> Status.Completed
            )
          )

          val expectedUa = emptyUserAnswers.copy(metadata = expectedMeta)

          result shouldBe true

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          verify(mockCacheRepository).set(eqTo(expectedUa), eqTo(SubmissionState.Amendment), eqTo(None))
        }
    }

    "when there is no xPaths return true" when {
      "a document exists in the cache for the given LRN and EORI " in {

        val metaData    = Metadata(lrn, eoriNumber, JsObject.empty, tasks = Map(".preTaskList" -> Completed, ".documents" -> Completed))
        val userAnswers = emptyUserAnswers.copy(metaData)

        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockCacheRepository.set(any(): UserAnswers, any(): SubmissionState, any(): Option[String])).thenReturn(Future.successful(true))

        val result = service.handleAmendmentErrors(lrn, eoriNumber, Seq.empty).futureValue

        val expectedMeta = Metadata(
          lrn,
          eoriNumber,
          JsObject.empty,
          tasks = Map(
            ".preTaskList" -> Status.Completed,
            ".documents"   -> Status.Completed
          )
        )

        val expectedUa = emptyUserAnswers.copy(metadata = expectedMeta)

        result shouldBe true

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verify(mockCacheRepository).set(eqTo(expectedUa), eqTo(SubmissionState.Amendment), eqTo(None))
      }
    }

    "return false" when {
      "a document doesn't exist in the cache for the given LRN and EORI" in {

        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val result = service.handleErrors(lrn, eoriNumber, xPaths).futureValue

        result shouldBe false

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verifyNoMoreInteractions(mockCacheRepository)
      }

      "a document exists in the cache for the given LRN and EORI " +
        "and setting the document tasks to error fails" in {

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockCacheRepository.set(any(): UserAnswers, any(): SubmissionState, eqTo(None))).thenReturn(Future.successful(false))

          val result = service.handleErrors(lrn, eoriNumber, xPaths).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          verify(mockCacheRepository).set(any(), eqTo(SubmissionState.RejectedPendingChanges), eqTo(None))
        }
    }
  }

  "handleGuaranteeErrors" must {

    "return true" when {
      "a document exists in the cache for the given LRN and EORI " +
        "and setting the document tasks to error succeeds" in {

          val tasks: Map[String, Status.Value] =
            Map(
              PreTaskList.taskName      -> Status.Unavailable,
              TraderDetails.taskName    -> Status.Unavailable,
              RouteDetails.taskName     -> Status.Unavailable,
              TransportDetails.taskName -> Status.Unavailable,
              Documents.taskName        -> Status.Unavailable,
              Items.taskName            -> Status.Unavailable,
              GuaranteeDetails.taskName -> Status.Error
            )

          val userAnswers = emptyUserAnswers.updateTasks(tasks)

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockCacheRepository.set(any(): UserAnswers, any(): SubmissionState, any(): Option[String])).thenReturn(Future.successful(true))

          val result = service.handleGuaranteeErrors(lrn, eoriNumber).futureValue

          result shouldBe true

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          verify(mockCacheRepository).set(eqTo(userAnswers), eqTo(SubmissionState.GuaranteeAmendment), eqTo(None))
        }
    }

    "return false" when {
      "a document doesn't exist in the cache for the given LRN and EORI" in {

        when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

        val result = service.handleGuaranteeErrors(lrn, eoriNumber).futureValue

        result shouldBe false

        verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        verifyNoMoreInteractions(mockCacheRepository)
      }

      "a document exists in the cache for the given LRN and EORI " +
        "and setting the document tasks to error fails" in {

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockCacheRepository.set(any(): UserAnswers, any(): SubmissionState, any(): Option[String])).thenReturn(Future.successful(false))

          val result = service.handleGuaranteeErrors(lrn, eoriNumber).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          verify(mockCacheRepository).set(any(), eqTo(SubmissionState.GuaranteeAmendment), eqTo(None))
        }
    }
  }

  "handleRejection" should {
    "update user answers" when {

      val tasks = Map(
        PreTaskList.taskName      -> Status.Completed,
        TraderDetails.taskName    -> Status.Completed,
        RouteDetails.taskName     -> Status.Completed,
        TransportDetails.taskName -> Status.Completed,
        Documents.taskName        -> Status.Completed,
        Items.taskName            -> Status.Completed,
        GuaranteeDetails.taskName -> Status.Completed
      )

      "IE055 rejection" in {
        val userAnswers = emptyUserAnswers.updateTasks(tasks)

        val rejection = IE055Rejection(departureId)

        val result = service.handleRejection(userAnswers, rejection)

        result.status shouldBe SubmissionState.GuaranteeAmendment
        result.metadata.tasks shouldBe Map(
          PreTaskList.taskName      -> Status.Unavailable,
          TraderDetails.taskName    -> Status.Unavailable,
          RouteDetails.taskName     -> Status.Unavailable,
          TransportDetails.taskName -> Status.Unavailable,
          Documents.taskName        -> Status.Unavailable,
          Items.taskName            -> Status.Unavailable,
          GuaranteeDetails.taskName -> Status.Error
        )
        result.departureId shouldBe Some(departureId)
      }

      "IE056 rejection" when {
        "013 business rejection type" in {
          val userAnswers = emptyUserAnswers.updateTasks(tasks)

          val rejection = IE056Rejection(
            departureId,
            BusinessRejectionType.AmendmentRejection,
            NonEmptyList.of(
              XPath("/CC015C/Representative/status")
            )
          )

          val result = service.handleRejection(userAnswers, rejection)

          result.status shouldBe SubmissionState.Amendment
          result.metadata.tasks shouldBe Map(
            PreTaskList.taskName      -> Status.Completed,
            TraderDetails.taskName    -> Status.Error,
            RouteDetails.taskName     -> Status.Completed,
            TransportDetails.taskName -> Status.Completed,
            Documents.taskName        -> Status.Completed,
            Items.taskName            -> Status.Completed,
            GuaranteeDetails.taskName -> Status.Completed
          )
          result.departureId shouldBe Some(departureId)
        }

        "015 business rejection type" in {
          val userAnswers = emptyUserAnswers.updateTasks(tasks)

          val rejection = IE056Rejection(
            departureId,
            BusinessRejectionType.DeclarationRejection,
            NonEmptyList.of(
              XPath("/CC015C/Representative/status")
            )
          )

          val result = service.handleRejection(userAnswers, rejection)

          result.status shouldBe SubmissionState.RejectedPendingChanges
          result.metadata.tasks shouldBe Map(
            PreTaskList.taskName      -> Status.Completed,
            TraderDetails.taskName    -> Status.Error,
            RouteDetails.taskName     -> Status.Completed,
            TransportDetails.taskName -> Status.Completed,
            Documents.taskName        -> Status.Completed,
            Items.taskName            -> Status.Completed,
            GuaranteeDetails.taskName -> Status.Completed
          )
          result.departureId shouldBe None
        }
      }
    }
  }
}
