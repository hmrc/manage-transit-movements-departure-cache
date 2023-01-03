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

package controllers.actions

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalacheck.Gen
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._

import scala.concurrent.Future

class AuthenticateActionProviderSpec extends SpecBase {

  def baseApplication: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure("metrics.jvm" -> false)

  class Harness(authenticate: AuthenticateActionProvider) {

    def action(): Action[AnyContent] = authenticate() {
      result =>
        Results.Ok(result.eoriNumber)
    }
  }

  private val enrolmentKey           = "HMRC-CTC-ORG"
  private val enrolmentIdentifierKey = "EORINumber"
  private val state                  = "Activated"

  private val validEnrolment: Enrolment =
    Enrolment(
      key = enrolmentKey,
      identifiers = Seq(
        EnrolmentIdentifier(
          enrolmentIdentifierKey,
          eoriNumber
        )
      ),
      state = state
    )

  private val invalidEnrolment: Enrolment =
    Enrolment(
      key = Gen.alphaNumStr.sample.value,
      identifiers = Seq(
        EnrolmentIdentifier(
          Gen.alphaNumStr.sample.value,
          Gen.alphaNumStr.sample.value
        )
      ),
      state = state
    )

  "authenticate" when {

    "a user has a valid enrolment" must {
      "return Ok" in {

        val mockAuthConnector = mock[AuthConnector]

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.successful(Enrolments(Set(validEnrolment))))

        val application = baseApplication
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .build()

        val actionProvider = application.injector.instanceOf[AuthenticateActionProvider]

        val controller = new Harness(actionProvider)
        val result     = controller.action()(fakeRequest)

        status(result) shouldBe OK
        contentAsString(result) shouldBe eoriNumber
      }
    }

    "when a user has invalid enrolments" must {
      "return Forbidden" in {

        val mockAuthConnector: AuthConnector = mock[AuthConnector]

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.successful(Enrolments(Set(invalidEnrolment))))

        val application = baseApplication
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))

        val actionProvider = application.injector().instanceOf[AuthenticateActionProvider]

        val controller = new Harness(actionProvider)
        val result     = controller.action()(fakeRequest)

        status(result) shouldBe FORBIDDEN
      }
    }

    "when bearer token is missing" must {
      "return Unauthorized" in {

        val mockAuthConnector: AuthConnector = mock[AuthConnector]

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.failed(MissingBearerToken()))

        val application = baseApplication
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))

        val actionProvider = application.injector().instanceOf[AuthenticateActionProvider]

        val controller = new Harness(actionProvider)
        val result     = controller.action()(fakeRequest)

        status(result) shouldBe UNAUTHORIZED
      }
    }
  }
}
