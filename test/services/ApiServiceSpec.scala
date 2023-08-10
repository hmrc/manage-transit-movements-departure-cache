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
import models.Departures
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class ApiServiceSpec extends SpecBase with ScalaFutures {

  private lazy val mockApiConnector = mock[ApiConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ApiConnector].toInstance(mockApiConnector)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockApiConnector)
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
      val expectedResult = Some(Departures(Nil))
      when(mockApiConnector.getDepartures(any())(any())).thenReturn(Future.successful(expectedResult))
      val result = service.getDeparturesForLrn(lrn).futureValue
      result shouldBe expectedResult
      verify(mockApiConnector).getDepartures(eqTo(Seq("localReferenceNumber" -> lrn)))(any())
    }
  }
}
