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
import models.XPath
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, verifyNoInteractions, verifyNoMoreInteractions, when}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class XPathServiceSpec extends SpecBase with ScalaFutures {

  private val service = app.injector.instanceOf[XPathService]

  private val unamendableXPath = XPath("/CC014C")

  private val amendableXPath = XPath("/CC015C/Authorisation[1]/referenceNumber")

  "isDeclarationAmendable" must {

    "return true" when {
      "a document exists in the cache for the given LRN and EORI" +
        "and there are 10 or fewer errors" +
        "and at least one of the errors is amendable" +
        "and isSubmitted is true" in {

          val userAnswers = emptyUserAnswers.copy(isSubmitted = Some(true))

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(userAnswers)))

          val xPaths = Seq.fill(9)(unamendableXPath) :+ amendableXPath

          val result = service.isDeclarationAmendable(lrn, eoriNumber, xPaths).futureValue

          result shouldBe true

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }
    }

    "return false" when {
      "a document doesn't exist in the cache for the given LRN and EORI" +
        "and there are 10 or fewer errors" +
        "and at least one of the errors is amendable" in {

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(None))

          val xPaths = Seq.fill(9)(unamendableXPath) :+ amendableXPath

          val result = service.isDeclarationAmendable(lrn, eoriNumber, xPaths).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }

      "a document exists in the cache for the given LRN and EORI" +
        "and there are more than 10 errors" +
        "and at least one of the errors is amendable" in {

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val xPaths = Seq.fill(10)(unamendableXPath) :+ amendableXPath

          val result = service.isDeclarationAmendable(lrn, eoriNumber, xPaths).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }

      "a document exists in the cache for the given LRN and EORI" +
        "and there are more than 10 errors" +
        "and none of the errors are amendable" in {

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val xPaths = Seq.fill(10)(unamendableXPath)

          val result = service.isDeclarationAmendable(lrn, eoriNumber, xPaths).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
        }

      "a document exists in the cache for the given LRN and EORI" +
        "and there are less than 10 errors" +
        "and all of the errors are amendable" +
        "and isSubmitted is false" in {

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val xPaths = Seq.fill(3)(unamendableXPath) :+ amendableXPath

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

          when(mockCacheRepository.get(any(), any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockCacheRepository.set(any())).thenReturn(Future.successful(true))

          val result = service.handleErrors(lrn, eoriNumber, xPaths).futureValue

          result shouldBe true

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          verify(mockCacheRepository).set(any())
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
          when(mockCacheRepository.set(any())).thenReturn(Future.successful(false))

          val result = service.handleErrors(lrn, eoriNumber, xPaths).futureValue

          result shouldBe false

          verify(mockCacheRepository).get(eqTo(lrn), eqTo(eoriNumber))
          verify(mockCacheRepository).set(any())
        }

      "there are no tasks to update" in {

        val xPaths = Seq.empty

        val result = service.handleErrors(lrn, eoriNumber, xPaths).futureValue

        result shouldBe false

        verifyNoInteractions(mockCacheRepository)
      }
    }
  }

}
