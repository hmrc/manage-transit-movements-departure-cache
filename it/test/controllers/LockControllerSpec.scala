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

package controllers

import itbase.LockRepositorySpecBase
import uk.gov.hmrc.http.HeaderNames

class LockControllerSpec extends LockRepositorySpecBase {

  "GET /user-answers/:lrn/lock" should {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/user-answers/$lrn/lock"

    "respond with Ok status with sessionId" in {
      val response = wsClient
        .url(url)
        .addHttpHeaders((HeaderNames.xSessionId, "sessionId"))
        .get()
        .futureValue

      response.status shouldEqual 200
    }

    "respond with BadRequest status without sessionId" in {
      val response = wsClient
        .url(url)
        .get()
        .futureValue

      response.status shouldEqual 400
    }
  }

  "DELETE /user-answers/:lrn/lock" should {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/user-answers/$lrn/lock"

    "respond with Ok status with sessionId" in {
      val response = wsClient
        .url(url)
        .addHttpHeaders((HeaderNames.xSessionId, "sessionId"))
        .get()
        .futureValue

      response.status shouldEqual 200
    }

    "respond with BadRequest status without sessionId" in {
      val response = wsClient
        .url(url)
        .get()
        .futureValue

      response.status shouldEqual 400
    }
  }
}
