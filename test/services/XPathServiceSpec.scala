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
import models.*
import models.Rejection.*
import models.Task.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}

import scala.concurrent.Future

class XPathServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val service = app.injector.instanceOf[XPathService]

  private val unamendableXPath = XPath("/CC014C")

  private val amendableXPath = XPath("/CC015C/Authorisation[1]/referenceNumber")

  "isRejectionAmendable" must {

    "return true" when {
      "IE055 rejection" when {
        "a document exists in the cache for the given LRN and EORI" in {
          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val rejection = IE055Rejection(departureId)

          val result = service.isRejectionAmendable(lrn, eoriNumber, rejection).futureValue

          result shouldBe true

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }
      }

      "IE056 rejection" when {
        "a document exists in the cache for the given LRN and EORI" when {
          "at least one of the errors is amendable" in {

            when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

            val rejection = IE056Rejection(
              departureId,
              BusinessRejectionType.AmendmentRejection,
              Seq(amendableXPath)
            )

            val result = service.isRejectionAmendable(lrn, eoriNumber, rejection).futureValue

            result shouldBe true

            verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          }
        }
      }
    }

    "return false" when {
      "IE055 rejection" when {
        "a document doesn't exist in the cache for the given LRN and EORI" in {
          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

          val rejection = IE055Rejection(departureId)

          val result = service.isRejectionAmendable(lrn, eoriNumber, rejection).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }
      }

      "IE056 rejection" when {
        "a document exists in the cache for the given LRN and EORI" when {
          "no errors are amendable" in {
            when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

            val rejection = IE056Rejection(
              departureId,
              BusinessRejectionType.AmendmentRejection,
              Seq(unamendableXPath)
            )

            val result = service.isRejectionAmendable(lrn, eoriNumber, rejection).futureValue

            result shouldBe false

            verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          }
        }

        "a document doesn't exist in the cache for the given LRN and EORI" in {
          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

          val rejection = IE055Rejection(departureId)

          val result = service.isRejectionAmendable(lrn, eoriNumber, rejection).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }
      }
    }
  }

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

        result.metadata.isSubmitted shouldBe SubmissionState.GuaranteeAmendment
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
            Seq(
              XPath("/CC015C/Representative/status")
            )
          )

          val result = service.handleRejection(userAnswers, rejection)

          result.metadata.isSubmitted shouldBe SubmissionState.Amendment
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
            Seq(
              XPath("/CC015C/Representative/status")
            )
          )

          val result = service.handleRejection(userAnswers, rejection)

          result.metadata.isSubmitted shouldBe SubmissionState.RejectedPendingChanges
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

  "prepareForAmendment" should {
    "update user answers" in {
      val userAnswers = emptyUserAnswers

      val result = service.prepareForAmendment(userAnswers, departureId)

      result.metadata.isSubmitted shouldBe SubmissionState.Amendment
      result.departureId shouldBe Some(departureId)
    }
  }
}
