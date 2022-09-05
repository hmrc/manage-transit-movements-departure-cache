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

package models

import base.SpecBase
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDateTime
import java.util.UUID

class UserAnswersSpec extends SpecBase {

  private val uuid = "2e8ede47-dbfb-44ea-a1e3-6c57b1fe6fe2"

  private val userAnswers = UserAnswers(
    lrn = lrn,
    eoriNumber = eoriNumber,
    data = Json.obj(),
    lastUpdated = LocalDateTime.of(2022: Int, 9: Int, 5: Int, 15: Int, 58: Int, 44: Int, 188000000: Int),
    id = UUID.fromString(uuid)
  )

  "User answers" when {

    "being passed between backend and frontend" should {

      val json: JsValue = Json.parse(s"""
          |{
          |    "_id" : "$uuid",
          |    "lrn" : "$lrn",
          |    "eoriNumber" : "$eoriNumber",
          |    "data" : {},
          |    "lastUpdated" : "2022-09-05T15:58:44.188"
          |}
          |""".stripMargin)

      "read correctly" in {
        val result = json.as[UserAnswers]
        result shouldBe userAnswers
      }

      "write correctly" in {
        val result = Json.toJson(userAnswers)
        result shouldBe json
      }
    }

    "being passed between backend and mongo" should {

      val json: JsValue = Json.parse(s"""
          |{
          |    "_id" : "$uuid",
          |    "lrn" : "$lrn",
          |    "eoriNumber" : "$eoriNumber",
          |    "data" : {},
          |    "lastUpdated" : {
          |        "$$date" : {
          |            "$$numberLong" : "1662393524188"
          |        }
          |    }
          |}
          |""".stripMargin)

      "read correctly" in {
        val result = json.as[UserAnswers](UserAnswers.mongoFormat)
        result shouldBe userAnswers
      }

      "write correctly" in {
        val result = Json.toJson(userAnswers)(UserAnswers.mongoFormat)
        result shouldBe json
      }
    }
  }

}
