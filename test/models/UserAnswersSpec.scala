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
import play.api.libs.json.{Format, JsSuccess, JsValue, Json}
import play.api.test.Helpers.running

import java.time.{Instant, LocalDateTime}
import java.util.UUID

class UserAnswersSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val userAnswers = UserAnswers(
    metadata = Metadata(
      lrn = lrn,
      eoriNumber = eoriNumber,
      isSubmitted = SubmissionState.NotSubmitted,
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
    departureId = Some(departureId),
    isTransitional = true
  )

  "User answers" when {

    "being passed between backend and frontend" should {

      val json: JsValue = Json.parse(s"""
          |{
          |  "_id" : "$uuid",
          |  "lrn" : "$lrn",
          |  "eoriNumber" : "$eoriNumber",
          |  "data" : {},
          |  "tasks" : {
          |    "task1" : "completed",
          |    "task2" : "in-progress",
          |    "task3" : "not-started",
          |    "task4" : "cannot-start-yet"
          |  },
          |  "createdAt" : "2022-09-05T15:58:44.188Z",
          |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
          |  "isSubmitted" : "notSubmitted",
          |  "departureId": "$departureId",
          |  "isTransitional": true
          |}
          |""".stripMargin)

      "read correctly" in {
        val result = json.as[UserAnswers]
        result shouldEqual userAnswers
      }

      "default non existent isTransitional to true" in {
        Json
          .parse(s"""
             |{
             |  "_id" : "$uuid",
             |  "lrn" : "$lrn",
             |  "eoriNumber" : "$eoriNumber",
             |  "data" : {},
             |  "tasks" : {
             |    "task1" : "completed",
             |    "task2" : "in-progress",
             |    "task3" : "not-started",
             |    "task4" : "cannot-start-yet"
             |  },
             |  "createdAt" : "2022-09-05T15:58:44.188Z",
             |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
             |  "isSubmitted" : "notSubmitted",
             |  "departureId": "$departureId"
             |}
             |""".stripMargin)
          .as[UserAnswers] shouldEqual userAnswers
      }

      "write correctly" in {
        val result = Json.toJson(userAnswers)
        result shouldEqual json
      }

      "be readable as a LocalDateTime for backwards compatibility" in {
        val json = Json.toJson(Instant.now())
        json.validate[LocalDateTime] shouldBe a[JsSuccess[?]]
      }
    }

    "being passed between backend and mongo" when {

      "encryption enabled" must {
        val app = guiceApplicationBuilder()
          .configure("encryption.enabled" -> true)
          .build()

        running(app) {
          val sensitiveFormats                     = app.injector.instanceOf[SensitiveFormats]
          implicit val format: Format[UserAnswers] = UserAnswers.sensitiveFormat(sensitiveFormats)

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "data" : "T+FWrvLPJMKyRZ1aoW8rdZmETyL89CdpWxaog0joG6B/hxCF",
               |  "isSubmitted" : "notSubmitted",
               |  "tasks" : {
               |    "task1" : "completed",
               |    "task2" : "in-progress",
               |    "task3" : "not-started",
               |    "task4" : "cannot-start-yet"
               |  },
               |  "createdAt" : {
               |    "$$date" : {
               |      "$$numberLong" : "1662393524188"
               |    }
               |  },
               |  "lastUpdated" : {
               |    "$$date" : {
               |      "$$numberLong" : "1662546803472"
               |    }
               |  },
               |  "departureId": "$departureId",
               |  "isTransitional": true
               |}
               |""".stripMargin)

          "read correctly" when {
            "data is empty" in {
              val result = json.as[UserAnswers]
              result shouldEqual userAnswers
            }

            "items and guarantee details at old path" in {
              val json: JsValue = Json.parse(s"""
                   |{
                   |  "_id" : "$uuid",
                   |  "lrn" : "$lrn",
                   |  "eoriNumber" : "$eoriNumber",
                   |  "data" : "tVWjq7/INmVWSQeX36H3nuS/mVkyalKoTLmriv77p/3a7R82VAAGMY0lVbqvwbBFhxxmoJDfn4lUOnytUr/V5KY3QQGbW2D4cH0UqreIyUE/lvwFY4G08keAxHqNtn39DnErxD92KoIHqq8Bmtd6b+Vj6kqs6SQrEBa8TBwWMq4VVpuOuvjfW/bayoGzwiXgNyfyW3Zrn+RWNChdsG/kvDKAscSBbcpGvZHAfHejjstklvJBt191UJXBnv5GQOfNBhiDpyqU+BUzkB89dcSe30+w06+GCn5jYhlKHgHVYGiq+3JzdBa9",
                   |  "isSubmitted" : "notSubmitted",
                   |  "tasks" : {
                   |    "task1" : "completed",
                   |    "task2" : "in-progress",
                   |    "task3" : "not-started",
                   |    "task4" : "cannot-start-yet"
                   |  },
                   |  "createdAt" : {
                   |    "$$date" : {
                   |      "$$numberLong" : "1662393524188"
                   |    }
                   |  },
                   |  "lastUpdated" : {
                   |    "$$date" : {
                   |      "$$numberLong" : "1662546803472"
                   |    }
                   |  },
                   |  "departureId": "$departureId",
                   |  "isTransitional": true
                   |}
                   |""".stripMargin)

              val result = json.as[UserAnswers]

              val expectedData = Json.parse("""
                  |{
                  |  "items" : {
                  |    "addAnotherItem" : false,
                  |    "items" : [
                  |      {
                  |        "foo" : "bar"
                  |      },
                  |      {
                  |        "bar" : "baz"
                  |      }
                  |    ]
                  |  },
                  |  "guaranteeDetails" : {
                  |    "addAnotherGuarantee" : true,
                  |    "guaranteeDetails" : [
                  |      {
                  |        "foo" : "bar"
                  |      },
                  |      {
                  |        "bar" : "baz"
                  |      }
                  |    ]
                  |  }
                  |}
                  |""".stripMargin)

              result.metadata.data shouldEqual expectedData
            }

            "items and guarantee details at new path" in {
              val json: JsValue = Json.parse(s"""
                   |{
                   |  "_id" : "$uuid",
                   |  "lrn" : "$lrn",
                   |  "eoriNumber" : "$eoriNumber",
                   |  "data" : "/HrWST8ii7p2rz3lPBjtt/vmbgobSwFE2oliEQIXYgT03g2XBRxcA3QDa1D8cQE0HQ0Y6+LuFnNBKvbUVhE7WaflJ91icPu8AGV4ULi4+8RDI/eOzPGxYu03OSOcgTQnboZLABJMhMvALYetlUefu+H56odxaODcOsX1+c1DNcgxgHhho93j9AfSktwF9tnVoeoB3hX7gUQHJ5+aNMYCf1ZBVVNgzy9mtRIT3ctspAqg1Mzvi+cayyZ5dPuCh4zXIA5hRyOV1lQ5DHyo78JzTCwo/RBrs+bLRPIsg2nSvswxt6Iu4vrW",
                   |  "isSubmitted" : "notSubmitted",
                   |  "tasks" : {
                   |    "task1" : "completed",
                   |    "task2" : "in-progress",
                   |    "task3" : "not-started",
                   |    "task4" : "cannot-start-yet"
                   |  },
                   |  "createdAt" : {
                   |    "$$date" : {
                   |      "$$numberLong" : "1662393524188"
                   |    }
                   |  },
                   |  "lastUpdated" : {
                   |    "$$date" : {
                   |      "$$numberLong" : "1662546803472"
                   |    }
                   |  },
                   |  "departureId": "$departureId",
                   |  "isTransitional": true
                   |}
                   |""".stripMargin)

              val result = json.as[UserAnswers]

              val expectedData = Json.parse("""
                  |{
                  |  "items" : {
                  |    "addAnotherItem" : false,
                  |    "items" : [
                  |      {
                  |        "foo" : "bar"
                  |      },
                  |      {
                  |        "bar" : "baz"
                  |      }
                  |    ]
                  |  },
                  |  "guaranteeDetails" : {
                  |    "addAnotherGuarantee" : true,
                  |    "guaranteeDetails" : [
                  |      {
                  |        "foo" : "bar"
                  |      },
                  |      {
                  |        "bar" : "baz"
                  |      }
                  |    ]
                  |  }
                  |}
                  |""".stripMargin)

              result.metadata.data shouldEqual expectedData
            }
          }

          "write and read correctly" in {
            val result = Json.toJson(userAnswers).as[UserAnswers]
            result shouldEqual userAnswers
          }
        }
      }

      "encryption disabled" must {
        val app = guiceApplicationBuilder()
          .configure("encryption.enabled" -> false)
          .build()

        running(app) {
          val sensitiveFormats                     = app.injector.instanceOf[SensitiveFormats]
          implicit val format: Format[UserAnswers] = UserAnswers.sensitiveFormat(sensitiveFormats)

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "data" : {},
               |  "isSubmitted" : "notSubmitted",
               |  "tasks" : {
               |    "task1" : "completed",
               |    "task2" : "in-progress",
               |    "task3" : "not-started",
               |    "task4" : "cannot-start-yet"
               |  },
               |  "createdAt" : {
               |    "$$date" : {
               |      "$$numberLong" : "1662393524188"
               |    }
               |  },
               |  "lastUpdated" : {
               |    "$$date" : {
               |      "$$numberLong" : "1662546803472"
               |    }
               |  },
               |  "departureId": "$departureId",
               |  "isTransitional": true
               |}
               |""".stripMargin)

          "read correctly" when {
            "data is empty" in {
              val result = json.as[UserAnswers]
              result shouldEqual userAnswers
            }

            "items and guarantee details at old path" in {
              val json: JsValue = Json.parse(s"""
                   |{
                   |  "_id" : "$uuid",
                   |  "lrn" : "$lrn",
                   |  "eoriNumber" : "$eoriNumber",
                   |  "data" : {
                   |    "addAnotherItem" : false,
                   |    "items" : [
                   |      {
                   |        "foo" : "bar"
                   |      },
                   |      {
                   |        "bar" : "baz"
                   |      }
                   |    ],
                   |    "addAnotherGuarantee" : true,
                   |    "guaranteeDetails" : [
                   |      {
                   |        "foo" : "bar"
                   |      },
                   |      {
                   |        "bar" : "baz"
                   |      }
                   |    ]
                   |  },
                   |  "isSubmitted" : "notSubmitted",
                   |  "tasks" : {
                   |    ".items" : "completed",
                   |    ".addAnotherItem" : "completed",
                   |    ".guaranteeDetails" : "completed",
                   |    ".addAnotherGuarantee" : "completed"
                   |  },
                   |  "createdAt" : {
                   |    "$$date" : {
                   |      "$$numberLong" : "1662393524188"
                   |    }
                   |  },
                   |  "lastUpdated" : {
                   |    "$$date" : {
                   |      "$$numberLong" : "1662546803472"
                   |    }
                   |  },
                   |  "departureId": "$departureId",
                   |  "isTransitional": true
                   |}
                   |""".stripMargin)

              val result = json.as[UserAnswers]

              val expectedData = Json.parse("""
                  |{
                  |  "items" : {
                  |    "addAnotherItem" : false,
                  |    "items" : [
                  |      {
                  |        "foo" : "bar"
                  |      },
                  |      {
                  |        "bar" : "baz"
                  |      }
                  |    ]
                  |  },
                  |  "guaranteeDetails" : {
                  |    "addAnotherGuarantee" : true,
                  |    "guaranteeDetails" : [
                  |      {
                  |        "foo" : "bar"
                  |      },
                  |      {
                  |        "bar" : "baz"
                  |      }
                  |    ]
                  |  }
                  |}
                  |""".stripMargin)

              result.metadata.data shouldEqual expectedData

              result.metadata.tasks shouldEqual Map(
                ".items"            -> Status.Completed,
                ".guaranteeDetails" -> Status.Completed
              )
            }

            "items and guarantee details at new path" in {
              val json: JsValue = Json.parse(s"""
                   |{
                   |  "_id" : "$uuid",
                   |  "lrn" : "$lrn",
                   |  "eoriNumber" : "$eoriNumber",
                   |  "data" : {
                   |    "items" : {
                   |      "addAnotherItem" : false,
                   |      "items" : [
                   |        {
                   |          "foo" : "bar"
                   |        },
                   |        {
                   |          "bar" : "baz"
                   |        }
                   |      ]
                   |    },
                   |    "guaranteeDetails" : {
                   |      "addAnotherGuarantee" : true,
                   |      "guaranteeDetails" : [
                   |        {
                   |          "foo" : "bar"
                   |        },
                   |        {
                   |          "bar" : "baz"
                   |        }
                   |      ]
                   |    }
                   |  },
                   |  "isSubmitted" : "notSubmitted",
                   |  "tasks" : {
                   |    "task1" : "completed",
                   |    "task2" : "in-progress",
                   |    "task3" : "not-started",
                   |    "task4" : "cannot-start-yet"
                   |  },
                   |  "createdAt" : {
                   |    "$$date" : {
                   |      "$$numberLong" : "1662393524188"
                   |    }
                   |  },
                   |  "lastUpdated" : {
                   |    "$$date" : {
                   |      "$$numberLong" : "1662546803472"
                   |    }
                   |  },
                   |  "departureId": "$departureId",
                   |  "isTransitional": true
                   |}
                   |""".stripMargin)

              val result = json.as[UserAnswers]

              val expectedData = Json.parse("""
                  |{
                  |  "items" : {
                  |    "addAnotherItem" : false,
                  |    "items" : [
                  |      {
                  |        "foo" : "bar"
                  |      },
                  |      {
                  |        "bar" : "baz"
                  |      }
                  |    ]
                  |  },
                  |  "guaranteeDetails" : {
                  |    "addAnotherGuarantee" : true,
                  |    "guaranteeDetails" : [
                  |      {
                  |        "foo" : "bar"
                  |      },
                  |      {
                  |        "bar" : "baz"
                  |      }
                  |    ]
                  |  }
                  |}
                  |""".stripMargin)

              result.metadata.data shouldEqual expectedData
            }
          }

          "write correctly" in {
            val result = Json.toJson(userAnswers)
            result shouldEqual json
          }
        }
      }
    }
  }
}
