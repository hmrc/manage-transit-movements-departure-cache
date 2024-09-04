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
import models.Phase
import models.request.{AuthenticatedRequest, VersionedRequest}
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VersionedActionSpec extends SpecBase with ScalaFutures {

  private class Harness extends VersionedAction {

    def callRefine[A](request: AuthenticatedRequest[A]): Future[Either[Result, VersionedRequest[A]]] =
      refine(request)
  }

  "VersionedAction" when {

    "transition header" should {
      "return request with transition phase value" in {

        val action = new Harness()

        val request              = fakeRequest.withHeaders("APIVersion" -> "2.0")
        val authenticatedRequest = AuthenticatedRequest(request, "EORINumber")
        val result               = action.callRefine(authenticatedRequest).futureValue

        result.value shouldBe VersionedRequest(authenticatedRequest, Phase.Transition)
      }
    }

    "post-transition header" should {
      "return request with post-transition phase value" in {

        val action = new Harness()

        val request              = fakeRequest.withHeaders("APIVersion" -> "2.1")
        val authenticatedRequest = AuthenticatedRequest(request, "EORINumber")
        val result               = action.callRefine(authenticatedRequest).futureValue

        result.value shouldBe VersionedRequest(authenticatedRequest, Phase.PostTransition)
      }
    }
  }
}
