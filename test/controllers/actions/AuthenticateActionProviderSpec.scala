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

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.AppConfig
import models.request.AuthenticatedRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalacheck.Gen
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.*
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticateActionProviderSpec extends SpecBase with AppWithDefaultMockFixtures {

  def baseApplication: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure("metrics.jvm" -> false)

  private val appConfig = app.injector.instanceOf[AppConfig]

  class Harness(authConnector: AuthConnector) extends AuthenticateAction(authConnector, appConfig) {

    def action(request: Request[?]): Future[Either[Result, AuthenticatedRequest[?]]] =
      refine(request)
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

        val harness = new Harness(mockAuthConnector)

        val request = fakeRequest

        whenReady(harness.action(request)) {
          result =>
            result.value shouldEqual AuthenticatedRequest(request, eoriNumber)
        }
      }
    }

    "when a user has invalid enrolments" must {
      "return Forbidden" in {

        val mockAuthConnector: AuthConnector = mock[AuthConnector]

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.successful(Enrolments(Set(invalidEnrolment))))

        val harness = new Harness(mockAuthConnector)

        val request = fakeRequest

        whenReady(harness.action(request)) {
          result =>
            status(Future.successful(result.left.value)) shouldEqual FORBIDDEN
        }
      }
    }

    "when bearer token is missing" must {
      "return Unauthorized" in {

        val mockAuthConnector: AuthConnector = mock[AuthConnector]

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.failed(MissingBearerToken()))

        val harness = new Harness(mockAuthConnector)

        val request = fakeRequest

        whenReady(harness.action(request)) {
          result =>
            status(Future.successful(result.left.value)) shouldEqual UNAUTHORIZED
        }
      }
    }
  }
}
