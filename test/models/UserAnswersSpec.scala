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

import base.SpecBase
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, JsValue, Json}

import java.time.{Instant, LocalDateTime}
import java.util.UUID

class UserAnswersSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val userAnswers = UserAnswers(
    metadata = Metadata(
      lrn        = lrn,
      eoriNumber = eoriNumber,
      data       = Json.obj(),
      tasks = Map(
        "task1" -> Status.Completed,
        "task2" -> Status.InProgress,
        "task3" -> Status.NotStarted,
        "task4" -> Status.CannotStartYet
      )
    ),
    createdAt   = Instant.ofEpochMilli(1662393524188L),
    lastUpdated = Instant.ofEpochMilli(1662546803472L),
    id          = UUID.fromString(uuid),
    status      = SubmissionState.NotSubmitted
  )

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

      "read correctly" in {
        val result = json.as[UserAnswers]
        result shouldBe userAnswers
      }

      "read correctly with departureId" in new DepartureIdScope {
        val result = json.as[UserAnswers]
        result shouldBe userAnswers.copy(metadata = userAnswers.metadata.copy(departureId = Some(depId1)))
      }

      "write correctly" in {
        val result = Json.toJson(userAnswers)
        result shouldBe json
      }

      "write correctly with departureId" in new DepartureIdScope {
        val result = Json.toJson(userAnswers.copy(metadata = userAnswers.metadata.copy(departureId = Some(depId1))))
        result shouldBe json
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

      "read correctly" in {
        val result = json.as[UserAnswers](UserAnswers.mongoFormat)
        result shouldBe userAnswers
      }

      "read correctly with departureId" in new DepartureIdScope {
        val result = json.as[UserAnswers](UserAnswers.mongoFormat)
        result shouldBe userAnswers.copy(metadata = userAnswers.metadata.copy(departureId = Some(depId1)))
      }

      "write correctly" in {
        val result = Json.toJson(userAnswers)(UserAnswers.mongoFormat)
        result shouldBe json
      }

      "write correctly with departureId" in new DepartureIdScope {
        val result = Json.toJson(userAnswers.copy(metadata = userAnswers.metadata.copy(departureId = Some(depId1))))((UserAnswers.mongoFormat))
        result shouldBe json
      }
    }
  }

}

trait DepartureIdScope {

  val lrn           = "lrn"
  val eoriNumber    = "eori"
  val uuid          = "2e8ede47-dbfb-44ea-a1e3-6c57b1fe6fe2"
  val depId1        = "1d234567fg"
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
       |    },
       |    "departureId": "$depId1"
       |}
       |""".stripMargin)
}
