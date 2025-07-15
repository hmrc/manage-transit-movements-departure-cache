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
import org.mockito.Mockito.{reset, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.CacheRepository

import scala.concurrent.Future

class XPathServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val mockCacheRepository: CacheRepository = mock[CacheRepository]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[CacheRepository].toInstance(mockCacheRepository))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheRepository)
  }

  private val service = app.injector.instanceOf[XPathService]

  private val unamendableXPath = XPath("/CC014C")

  private val amendableXPath = XPath("/CC015C/Authorisation[1]/referenceNumber")

  "isRejectionAmendable" must {

    "return true" when {
      "IE055 rejection" when {
        "a document exists in the cache for the given LRN and EORI" in {
          val userAnswers = emptyUserAnswers.updateStatus(SubmissionState.Submitted)

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

          val rejection = IE055Rejection(departureId)

          val result = service.isRejectionAmendable(lrn, eoriNumber, rejection).futureValue

          result shouldEqual true

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }
      }

      "IE056 rejection" when {
        "a document exists in the cache for the given LRN and EORI" when {
          "at least one of the errors is amendable" in {
            val userAnswers = emptyUserAnswers.updateStatus(SubmissionState.Submitted)

            when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

            val rejection = IE056Rejection(
              departureId,
              BusinessRejectionType.AmendmentRejection,
              Seq(amendableXPath)
            )

            val result = service.isRejectionAmendable(lrn, eoriNumber, rejection).futureValue

            result shouldEqual true

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

          result shouldEqual false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }

        "a document exists in the cache with status NotSubmitted" in {
          val userAnswers = emptyUserAnswers.updateStatus(SubmissionState.NotSubmitted)

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

          val rejection = IE055Rejection(departureId)

          val result = service.isRejectionAmendable(lrn, eoriNumber, rejection).futureValue

          result shouldEqual false

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

            result shouldEqual false

            verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          }
        }

        "a document doesn't exist in the cache for the given LRN and EORI" in {
          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

          val rejection = IE056Rejection(
            departureId,
            BusinessRejectionType.AmendmentRejection,
            Seq(amendableXPath)
          )

          val result = service.isRejectionAmendable(lrn, eoriNumber, rejection).futureValue

          result shouldEqual false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }

        "a document exists in the cache with status NotSubmitted" in {
          val userAnswers = emptyUserAnswers.updateStatus(SubmissionState.NotSubmitted)

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

          val rejection = IE056Rejection(
            departureId,
            BusinessRejectionType.AmendmentRejection,
            Seq(amendableXPath)
          )

          val result = service.isRejectionAmendable(lrn, eoriNumber, rejection).futureValue

          result shouldEqual false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }
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

        result.metadata.isSubmitted shouldEqual SubmissionState.GuaranteeAmendment
        result.metadata.tasks shouldEqual Map(
          PreTaskList.taskName      -> Status.Unavailable,
          TraderDetails.taskName    -> Status.Unavailable,
          RouteDetails.taskName     -> Status.Unavailable,
          TransportDetails.taskName -> Status.Unavailable,
          Documents.taskName        -> Status.Unavailable,
          Items.taskName            -> Status.Unavailable,
          GuaranteeDetails.taskName -> Status.Error
        )
        result.departureId shouldEqual Some(departureId)
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

          result.metadata.isSubmitted shouldEqual SubmissionState.Amendment
          result.metadata.tasks shouldEqual Map(
            PreTaskList.taskName      -> Status.Completed,
            TraderDetails.taskName    -> Status.Error,
            RouteDetails.taskName     -> Status.Completed,
            TransportDetails.taskName -> Status.Completed,
            Documents.taskName        -> Status.Completed,
            Items.taskName            -> Status.Completed,
            GuaranteeDetails.taskName -> Status.Completed
          )
          result.departureId shouldEqual Some(departureId)
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

          result.metadata.isSubmitted shouldEqual SubmissionState.RejectedPendingChanges
          result.metadata.tasks shouldEqual Map(
            PreTaskList.taskName      -> Status.Completed,
            TraderDetails.taskName    -> Status.Error,
            RouteDetails.taskName     -> Status.Completed,
            TransportDetails.taskName -> Status.Completed,
            Documents.taskName        -> Status.Completed,
            Items.taskName            -> Status.Completed,
            GuaranteeDetails.taskName -> Status.Completed
          )
          result.departureId should not be defined
        }
      }
    }
  }

  "prepareForAmendment" should {
    "update user answers" in {
      val userAnswers = emptyUserAnswers

      val result = service.prepareForAmendment(userAnswers, departureId)

      result.metadata.isSubmitted shouldEqual SubmissionState.Amendment
      result.departureId shouldEqual Some(departureId)
    }
  }
}
