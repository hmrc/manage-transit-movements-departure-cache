/*
 * Copyright 2022 HM Revenue & Customs
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

import itbase.ItSpecBase
import models.UserAnswers
import org.mongodb.scala.model.Filters
import play.api.libs.json.{JsString, Json}

class CacheControllerSpec extends ItSpecBase {

  "GET /user-answers/:lrn" when {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/$frontend/user-answers/$lrn"

    "document does not exist" should {
      "respond with 404 status" in {
        val response = wsClient
          .url(url)
          .get()
          .futureValue

        response.status shouldBe 404
      }
    }

    "document does exist" should {
      "respond with 200 status" in {
        val userAnswers = emptyUserAnswers
        insert(userAnswers).futureValue

        val response = wsClient
          .url(url)
          .get()
          .futureValue

        response.status shouldBe 200

        response.json.as[UserAnswers] shouldBe userAnswers
      }
    }
  }

  "POST /user-answers" when {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/$frontend/user-answers"

    "document successfully written to mongo" should {
      "respond with 200 status" in {
        val userAnswers = emptyUserAnswers

        val response = wsClient
          .url(url)
          .post(Json.toJson(userAnswers))
          .futureValue

        response.status shouldBe 200

        val results = find(Filters.eq("_id", userAnswers.id.toString)).futureValue
        results.size shouldBe 1
        val result = results.head
        result.id shouldBe userAnswers.id
        result.lrn shouldBe userAnswers.lrn
        result.eoriNumber shouldBe userAnswers.eoriNumber
        result.data shouldBe userAnswers.data
        result.lastUpdated isAfter userAnswers.lastUpdated shouldBe true
      }
    }

    "empty request body" should {
      "respond with 400 status" in {
        val response = wsClient
          .url(url)
          .post(Json.obj())
          .futureValue

        response.status shouldBe 400
      }
    }

    "invalid request body" should {
      "respond with 400 status" in {
        val response = wsClient
          .url(url)
          .post(JsString("foo"))
          .futureValue

        response.status shouldBe 400
      }
    }

    "the EORI in the enrolment and the EORI in user answers do not match" should {
      "respond with 403 status" in {
        val userAnswers = emptyUserAnswers.copy(eoriNumber = "different eori")

        val response = wsClient
          .url(url)
          .post(Json.toJson(userAnswers))
          .futureValue

        response.status shouldBe 403
      }
    }
  }
}
