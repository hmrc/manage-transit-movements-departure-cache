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

package models

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, JsValue, Json}

import java.time.{Instant, LocalDateTime}
import java.util.UUID

class UserAnswersSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val userAnswers = UserAnswers(
    metadata = Metadata(
      lrn = lrn,
      eoriNumber = eoriNumber,
      data = Json.obj(),
      tasks = Map(
        "task1" -> Status.Completed,
        "task2" -> Status.InProgress,
        "task3" -> Status.NotStarted,
        "task4" -> Status.CannotStartYet
      )
    ),
    createdAt = Instant.ofEpochMilli(1662393524188L),
    lastUpdated = Instant.ofEpochMilli(1662546803472L),
    id = UUID.fromString(uuid),
    status = SubmissionState.NotSubmitted
  )

  private val userAnswersWithDepartureId = userAnswers.copy(departureId = Some(departureId))

  "User answers" when {

    "being passed between backend and frontend" should {

      val json: JsValue = Json.parse(s"""
          |{
          |    "_id" : "$uuid",
          |    "lrn" : "$lrn",
          |    "eoriNumber" : "$eoriNumber",
          |    "data" : {},
          |    "tasks" : {
          |        "task1" : "completed",
          |        "task2" : "in-progress",
          |        "task3" : "not-started",
          |        "task4" : "cannot-start-yet"
          |    },
          |    "createdAt" : "2022-09-05T15:58:44.188Z",
          |    "lastUpdated" : "2022-09-07T10:33:23.472Z",
          |    "isSubmitted" : "notSubmitted"
          |}
          |""".stripMargin)

      val jsonWithDepartureId: JsValue = Json.parse(s"""
           |{
           |    "_id" : "$uuid",
           |    "lrn" : "$lrn",
           |    "eoriNumber" : "$eoriNumber",
           |    "data" : {},
           |    "tasks" : {
           |        "task1" : "completed",
           |        "task2" : "in-progress",
           |        "task3" : "not-started",
           |        "task4" : "cannot-start-yet"
           |    },
           |    "createdAt" : "2022-09-05T15:58:44.188Z",
           |    "lastUpdated" : "2022-09-07T10:33:23.472Z",
           |    "isSubmitted" : "notSubmitted",
           |    "departureId": "$departureId"
           |}
           |""".stripMargin)

      "read correctly" in {
        val result = json.as[UserAnswers]
        result shouldBe userAnswers
      }

      "read correctly with departureId" in {
        val result = jsonWithDepartureId.as[UserAnswers]
        result shouldBe userAnswersWithDepartureId
      }

      "write correctly" in {
        val result = Json.toJson(userAnswers)
        result shouldBe json
      }

      "write correctly with departureId" in {
        val result = Json.toJson(userAnswersWithDepartureId)
        result shouldBe jsonWithDepartureId
      }

      "be readable as a LocalDateTime for backwards compatibility" in {
        val json = Json.toJson(Instant.now())
        json.validate[LocalDateTime] shouldBe a[JsSuccess[_]]
      }
    }

    "being passed between backend and mongo" should {

      val json: JsValue = Json.parse(s"""
          |{
          |    "_id" : "$uuid",
          |    "lrn" : "$lrn",
          |    "eoriNumber" : "$eoriNumber",
          |    "data" : {},
          |    "isSubmitted" : "notSubmitted",
          |    "tasks" : {
          |        "task1" : "completed",
          |        "task2" : "in-progress",
          |        "task3" : "not-started",
          |        "task4" : "cannot-start-yet"
          |    },
          |    "createdAt" : {
          |        "$$date" : {
          |            "$$numberLong" : "1662393524188"
          |        }
          |    },
          |
          |    "lastUpdated" : {
          |        "$$date" : {
          |            "$$numberLong" : "1662546803472"
          |        }
          |    }
          |}
          |""".stripMargin)

      val jsonWithDepartureId: JsValue = Json.parse(s"""
           |{
           |    "_id" : "$uuid",
           |    "lrn" : "$lrn",
           |    "eoriNumber" : "$eoriNumber",
           |    "data" : {},
           |    "isSubmitted" : "notSubmitted",
           |    "tasks" : {
           |        "task1" : "completed",
           |        "task2" : "in-progress",
           |        "task3" : "not-started",
           |        "task4" : "cannot-start-yet"
           |    },
           |    "createdAt" : {
           |        "$$date" : {
           |            "$$numberLong" : "1662393524188"
           |        }
           |    },
           |
           |    "lastUpdated" : {
           |        "$$date" : {
           |            "$$numberLong" : "1662546803472"
           |        }
           |    },
           |    "departureId": "$departureId"
           |}
           |""".stripMargin)

      "read correctly" in {
        val result = json.as[UserAnswers](UserAnswers.mongoFormat)
        result shouldBe userAnswers
      }

      "read correctly with departureId" in {
        val result = jsonWithDepartureId.as[UserAnswers](UserAnswers.mongoFormat)
        result shouldBe userAnswersWithDepartureId
      }

      "write correctly" in {
        val result = Json.toJson(userAnswers)(UserAnswers.mongoFormat)
        result shouldBe json
      }

      "write correctly with departureId" in {
        val result = Json.toJson(userAnswersWithDepartureId)(UserAnswers.mongoFormat)
        result shouldBe jsonWithDepartureId
      }
    }
  }

}
