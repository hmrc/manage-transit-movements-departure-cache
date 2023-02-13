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

package submission

import api.submission.TransitOperation
import base.SpecBase
import generated.{Number0, TransitOperationType06}
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}

class TransitOperationSpec extends SpecBase {

  "TransitOperation" when {

    "transform is called" when {

      "will convert to API format" in {

        val json: JsValue = Json.parse(s"""
                                          |{
                                          |    "_id" : "$uuid",
                                          |    "lrn" : "$lrn",
                                          |    "eoriNumber" : "$eoriNumber",
                                          |    "data" : {
                                          |      "preTaskList" : {
                                          |        "officeOfDeparture" : {
                                          |          "id" : "XI000142",
                                          |          "name" : "Belfast EPU",
                                          |          "phoneNumber" : "+44 (0)02896 931537"
                                          |        },
                                          |        "procedureType" : "normal",
                                          |        "declarationType" : "TIR",
                                          |        "tirCarnetReference" : "1234567",
                                          |        "securityDetailsType" : "entrySummaryDeclaration",
                                          |        "detailsConfirmed" : true
                                          |      }
                                          |    },
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
                                          |    "lastUpdated" : {
                                          |        "$$date" : {
                                          |            "$$numberLong" : "1662546803472"
                                          |        }
                                          |    }
                                          |}
                                          |""".stripMargin)

        val uA: UserAnswers = json.as[UserAnswers](UserAnswers.mongoFormat)

        val expected = TransitOperationType06("lrn", "TIR", "A", Some("1234567"), None, "entrySummaryDeclaration", Number0, None, None, Number0, None)

        val converted = TransitOperation.transform(uA)

        converted shouldBe expected

      }

    }

  }
}
