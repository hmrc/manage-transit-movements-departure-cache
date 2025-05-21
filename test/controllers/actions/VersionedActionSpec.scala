/*
 * Copyright 2025 HM Revenue & Customs
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
import models.Version
import models.request.{AuthenticatedRequest, VersionedRequest}
import play.api.mvc.*
import play.api.mvc.Results.BadRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VersionedActionSpec extends SpecBase {

  private class Harness extends VersionedAction {

    def callRefine[A](request: AuthenticatedRequest[A]): Future[Either[Result, VersionedRequest[A]]] =
      refine(request)
  }

  "VersionedAction" when {

    "phase 5 header" should {
      "return request with phase 5 value" in {
        val action = new Harness()

        val request              = fakeRequest.withHeaders("API-Version" -> "1.0")
        val authenticatedRequest = AuthenticatedRequest(request, "EORINumber")
        val result               = action.callRefine(authenticatedRequest).futureValue

        result.value shouldEqual VersionedRequest(authenticatedRequest, Version.Phase5)
      }
    }

    "phase 6 header" should {
      "return request with phase 6 value" in {
        val action = new Harness()

        val request              = fakeRequest.withHeaders("API-Version" -> "2.0")
        val authenticatedRequest = AuthenticatedRequest(request, "EORINumber")
        val result               = action.callRefine(authenticatedRequest).futureValue

        result.value shouldEqual VersionedRequest(authenticatedRequest, Version.Phase6)
      }
    }

    "undefined header" should {
      "return request with phase 5 value" in {
        val action = new Harness()

        val authenticatedRequest = AuthenticatedRequest(fakeRequest, "EORINumber")
        val result               = action.callRefine(authenticatedRequest).futureValue

        result.value shouldEqual VersionedRequest(authenticatedRequest, Version.Phase5)
      }
    }

    "invalid header" should {
      "return bad request" in {
        val action = new Harness()

        val request              = fakeRequest.withHeaders("API-Version" -> "foo")
        val authenticatedRequest = AuthenticatedRequest(request, "EORINumber")
        val result               = action.callRefine(authenticatedRequest).futureValue

        result.left.value shouldEqual BadRequest("foo is not a valid version")
      }
    }
  }
}
