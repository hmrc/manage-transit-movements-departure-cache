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
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.Helpers._
import repositories.DefaultLockRepository
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderNames

import scala.concurrent.Future

class LockActionProviderSpec extends SpecBase {

  def baseApplication: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure("metrics.jvm" -> false)

  class Harness(lock: AuthenticateAndLockActionProvider) {

    def action(): Action[AnyContent] = lock(lrn) {
      _ =>
        Results.Ok(lrn)
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

  "LockAction" when {

    "when a user has a sessionId" should {
      "return ok after successful lock" in {

        val mockAuthConnector  = mock[AuthConnector]
        val mockLockRepository = mock[DefaultLockRepository]

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.successful(Enrolments(Set(validEnrolment))))

        when(mockLockRepository.lock(any()))
          .thenReturn(Future.successful(true))

        val application = baseApplication
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .overrides(bind[DefaultLockRepository].toInstance(mockLockRepository))
          .build()

        val actionProvider: AuthenticateAndLockActionProvider = application.injector.instanceOf[AuthenticateAndLockActionProvider]

        val controller = new Harness(actionProvider)
        val result     = controller.action()(fakeRequest.withHeaders((HeaderNames.xSessionId, "sessionId")))

        status(result) shouldBe OK
        contentAsString(result) shouldBe lrn
      }

      "return locked when lock already exists" in {

        val mockAuthConnector  = mock[AuthConnector]
        val mockLockRepository = mock[DefaultLockRepository]

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.successful(Enrolments(Set(validEnrolment))))

        when(mockLockRepository.lock(any()))
          .thenReturn(Future.successful(false))

        val application = baseApplication
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .overrides(bind[DefaultLockRepository].toInstance(mockLockRepository))
          .build()

        val actionProvider: AuthenticateAndLockActionProvider = application.injector.instanceOf[AuthenticateAndLockActionProvider]

        val controller = new Harness(actionProvider)
        val result     = controller.action()(fakeRequest.withHeaders((HeaderNames.xSessionId, "sessionId")))

        status(result) shouldBe LOCKED
      }
    }

    "when a user doesnt have a sessionId" should {

      "return bad request" in {

        val mockAuthConnector  = mock[AuthConnector]
        val mockLockRepository = mock[DefaultLockRepository]

        when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.successful(Enrolments(Set(validEnrolment))))

        when(mockLockRepository.lock(any()))
          .thenReturn(Future.successful(false))

        val application = baseApplication
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .overrides(bind[DefaultLockRepository].toInstance(mockLockRepository))
          .build()

        val actionProvider: AuthenticateAndLockActionProvider = application.injector.instanceOf[AuthenticateAndLockActionProvider]

        val controller = new Harness(actionProvider)
        val result     = controller.action()(fakeRequest)

        status(result) shouldBe BAD_REQUEST
      }
    }
  }
}
