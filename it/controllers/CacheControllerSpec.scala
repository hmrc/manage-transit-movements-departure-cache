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

import itbase.ItSpecBase
import models.UserAnswers
import org.mongodb.scala.model.Filters
import play.api.libs.json.{JsObject, JsString, Json}

import java.time.LocalDateTime
import java.util.UUID

class CacheControllerSpec extends ItSpecBase {

  "GET /user-answers/:lrn" when {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/user-answers/$lrn"

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

        response.json.as[UserAnswers].data shouldBe userAnswers.data

        response.json.as[UserAnswers].createdAt shouldBe userAnswers.createdAt.truncatedTo(
          java.time.temporal.ChronoUnit.MILLIS
        )

        response.json.as[UserAnswers].lastUpdated shouldBe userAnswers.lastUpdated.truncatedTo(
          java.time.temporal.ChronoUnit.MILLIS
        )
      }
    }
  }

  "POST /user-answers" when {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/user-answers"

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
        result.tasks shouldBe userAnswers.tasks
        result.createdAt shouldBe userAnswers.createdAt.truncatedTo(java.time.temporal.ChronoUnit.MILLIS)

        result.lastUpdated isAfter userAnswers.lastUpdated.truncatedTo(
          java.time.temporal.ChronoUnit.MILLIS
        ) shouldBe true
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

  "PUT /user-answers" when {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/user-answers"

    "document successfully written to mongo" should {
      "respond with 200 status" in {
        val response = wsClient
          .url(url)
          .put(JsString(lrn))
          .futureValue

        response.status shouldBe 200

        val filters = Filters.and(
          Filters.eq("lrn", lrn),
          Filters.eq("eoriNumber", eoriNumber)
        )
        val results = find(filters).futureValue
        results.size shouldBe 1
      }
    }

    "empty request body" should {
      "respond with 400 status" in {
        val response = wsClient
          .url(url)
          .put(Json.obj())
          .futureValue

        response.status shouldBe 400
      }
    }

    "invalid request body" should {
      "respond with 400 status" in {
        val response = wsClient
          .url(url)
          .put(Json.obj("foo" -> "bar"))
          .futureValue

        response.status shouldBe 400
      }
    }
  }

  "DELETE /user-answers/:lrn" when {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/user-answers/$lrn"

    "document exists" should {
      "remove document and respond with 200 status" in {
        insert(emptyUserAnswers).futureValue

        val response = wsClient
          .url(url)
          .delete()
          .futureValue

        response.status shouldBe 200

        findAll().futureValue shouldBe empty
      }
    }

    "document does not exist" should {
      "respond with 200 status" in {
        val response = wsClient
          .url(url)
          .delete()
          .futureValue

        response.status shouldBe 200
      }
    }
  }

  "GET /user-answers" when {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/user-answers"

    "documents do exist" should {
      "respond with 200 status" in {
        val userAnswers1 = UserAnswers("AB123", eoriNumber, Json.obj(), Map(), LocalDateTime.now(), LocalDateTime.now(), UUID.randomUUID())
        val userAnswers2 =
          UserAnswers("CD123", eoriNumber, Json.obj(), Map(), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), UUID.randomUUID())

        insert(userAnswers1).futureValue
        insert(userAnswers2).futureValue

        val response = wsClient
          .url(url)
          .get()
          .futureValue

        response.status shouldBe 200

        (response.json \ "userAnswers").as[Seq[JsObject]].length shouldBe 2

        val lrnResults = response.json \ "userAnswers" \\ "lrn"

        lrnResults.head.validate[String].get shouldBe "AB123"
        lrnResults(1).validate[String].get shouldBe "CD123"

        val urlResults = response.json \ "userAnswers" \\ "_links"

        (urlResults.head \ "self" \ "href").validate[String].get shouldBe controllers.routes.CacheController.get("AB123").url
        (urlResults(1) \ "self" \ "href").validate[String].get shouldBe controllers.routes.CacheController.get("CD123").url
      }
    }
  }
}
