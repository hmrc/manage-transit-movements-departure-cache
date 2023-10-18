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

package api.submission

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}
import scalaxb.XMLCalendar

class CustomsOfficesSpec extends SpecBase with AppWithDefaultMockFixtures {

  "CustomsOffices" must {

    "convert to API format" when {

      "transformOfficeOfDeparture is called" in {

        val json: JsValue = Json.parse(s"""
            |{
            |  "_id" : "$uuid",
            |  "lrn" : "$lrn",
            |  "eoriNumber" : "$eoriNumber",
            |  "isSubmitted" : "notSubmitted",
            |  "data" : {
            |    "preTaskList" : {
            |      "officeOfDeparture" : {
            |        "id" : "GB000011",
            |        "name" : "Birmingham Airport",
            |        "phoneNumber" : "+44 (0)121 781 7850"
            |      },
            |      "procedureType" : "normal",
            |      "declarationType" : {
            |        "code" : "T1"
            |      },
            |      "securityDetailsType" : {
            |        "code" : "0"
            |      },
            |      "detailsConfirmed" : true
            |    }
            |  },
            |  "tasks" : {},
            |  "createdAt" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662393524188"
            |    }
            |  },
            |  "lastUpdated" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662546803472"
            |    }
            |  }
            |}
            |""".stripMargin)

        val uA: UserAnswers = json.as[UserAnswers](UserAnswers.mongoFormat)

        val expected = CustomsOfficeOfDepartureType03("GB000011")

        val converted: CustomsOfficeOfDepartureType03 = CustomsOffices.transformOfficeOfDeparture(uA)

        converted shouldBe expected
      }

      "transformOfficeOfDestination is called" in {

        val json: JsValue = Json.parse(s"""
            |{
            |  "_id" : "$uuid",
            |  "lrn" : "$lrn",
            |  "eoriNumber" : "$eoriNumber",
            |  "isSubmitted" : "notSubmitted",
            |  "data" : {
            |    "routeDetails" : {
            |      "routing" : {
            |        "countryOfDestination" : {
            |          "code" : "IT",
            |          "description" : "Italy"
            |        },
            |        "officeOfDestination" : {
            |          "id" : "IT018101",
            |          "name" : "Aeroporto Bari - Palese",
            |          "phoneNumber" : "0039 0805316196"
            |        }
            |      }
            |    }
            |  },
            |  "tasks" : {},
            |  "createdAt" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662393524188"
            |    }
            |  },
            |  "lastUpdated" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662546803472"
            |    }
            |  }
            |}
            |""".stripMargin)

        val uA: UserAnswers = json.as[UserAnswers](UserAnswers.mongoFormat)

        val expected = CustomsOfficeOfDestinationDeclaredType01("IT018101")

        val converted: CustomsOfficeOfDestinationDeclaredType01 = CustomsOffices.transformOfficeOfDestination(uA)

        converted shouldBe expected
      }

      "transformOfficeOfTransit is called" in {

        val json: JsValue = Json.parse(s"""
            |{
            |  "_id" : "$uuid",
            |  "lrn" : "$lrn",
            |  "eoriNumber" : "$eoriNumber",
            |  "isSubmitted" : "notSubmitted",
            |  "data" : {
            |    "routeDetails" : {
            |      "transit" : {
            |        "officesOfTransit" : [
            |          {
            |            "officeOfTransitCountry" : {
            |              "code" : "IT",
            |              "description" : "Italy"
            |            },
            |            "officeOfTransit" : {
            |              "id" : "IT057101",
            |              "name" : "AEROPORTO A. VESPUCCI",
            |              "phoneNumber" : "0039 0553061629"
            |            },
            |            "addOfficeOfTransitETAYesNo" : true,
            |            "arrivalDateTime" : "2023-02-23T08:45:00"
            |          },
            |          {
            |            "officeOfTransitCountry" : {
            |              "code" : "AT",
            |              "description" : "Austria"
            |            },
            |            "officeOfTransit" : {
            |              "id" : "AT330400",
            |              "name" : "Flughafen Wien Cargo Center Nord",
            |              "phoneNumber" : "+43 50 233 563"
            |            },
            |            "addOfficeOfTransitETAYesNo" : false
            |          }
            |        ]
            |      }
            |    }
            |  },
            |  "tasks" : {},
            |  "createdAt" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662393524188"
            |    }
            |  },
            |  "lastUpdated" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662546803472"
            |    }
            |  }
            |}
            |""".stripMargin)

        val uA: UserAnswers = json.as[UserAnswers](UserAnswers.mongoFormat)

        val expected = Seq(
          CustomsOfficeOfTransitDeclaredType04(
            sequenceNumber = "1",
            referenceNumber = "IT057101",
            arrivalDateAndTimeEstimated = Some(XMLCalendar("2023-02-23T08:45:00"))
          ),
          CustomsOfficeOfTransitDeclaredType04(
            sequenceNumber = "2",
            referenceNumber = "AT330400",
            arrivalDateAndTimeEstimated = None
          )
        )

        val converted: Seq[CustomsOfficeOfTransitDeclaredType04] = CustomsOffices.transformOfficeOfTransit(uA)

        converted shouldBe expected
      }

      "transformOfficeOfExit is called" in {

        val json: JsValue = Json.parse(s"""
            |{
            |  "_id" : "$uuid",
            |  "lrn" : "$lrn",
            |  "eoriNumber" : "$eoriNumber",
            |  "isSubmitted" : "notSubmitted",
            |  "data" : {
            |    "routeDetails" : {
            |      "exit" : {
            |        "officesOfExit" : [
            |          {
            |            "officeOfExitCountry" : {
            |              "code" : "IT",
            |              "description" : "Italy"
            |            },
            |            "officeOfExit" : {
            |              "id" : "IT057101",
            |              "name" : "AEROPORTO A. VESPUCCI",
            |              "phoneNumber" : "0039 0553061629"
            |            }
            |          },
            |          {
            |            "officeOfExitCountry" : {
            |              "code" : "AT",
            |              "description" : "Austria"
            |            },
            |            "officeOfExit" : {
            |              "id" : "AT330400",
            |              "name" : "Flughafen Wien Cargo Center Nord",
            |              "phoneNumber" : "+43 50 233 563"
            |            }
            |          }
            |        ]
            |      }
            |    }
            |  },
            |  "tasks" : {},
            |  "createdAt" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662393524188"
            |    }
            |  },
            |  "lastUpdated" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662546803472"
            |    }
            |  }
            |}
            |""".stripMargin)

        val uA: UserAnswers = json.as[UserAnswers](UserAnswers.mongoFormat)

        val expected = Seq(
          CustomsOfficeOfExitForTransitDeclaredType02(
            sequenceNumber = "1",
            referenceNumber = "IT057101"
          ),
          CustomsOfficeOfExitForTransitDeclaredType02(
            sequenceNumber = "2",
            referenceNumber = "AT330400"
          )
        )

        val converted: Seq[CustomsOfficeOfExitForTransitDeclaredType02] = CustomsOffices.transformOfficeOfExit(uA)

        converted shouldBe expected
      }
    }
  }
}
