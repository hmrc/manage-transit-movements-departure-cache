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
import scalaxb.XMLCalendar

class TransitOperationSpec extends SpecBase {

  "TransitOperation" when {

    "transform is called" when {

      "no security type" must {

        "convert to API format" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "notSubmitted",
               |  "data" : {
               |    "preTaskList" : {
               |      "officeOfDeparture" : {
               |        "id" : "XI000142",
               |        "name" : "Belfast EPU",
               |        "phoneNumber" : "+44 (0)02896 931537"
               |      },
               |      "procedureType" : "normal",
               |      "declarationType" : "TIR",
               |      "tirCarnetReference" : "1234567",
               |      "securityDetailsType" : "noSecurity",
               |      "detailsConfirmed" : true
               |    },
               |    "routeDetails" : {
               |      "specificCircumstanceIndicator" : {
               |        "code" : "A20",
               |        "description" : "Express consignments in the context of exit summary declarations"
               |      }
               |    },
               |    "transportDetails" : {
               |      "authorisationsAndLimit" : {
               |        "limit": {
               |          "limitDate": "2022-07-15"
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

          val expected =
            TransitOperationType06(
              LRN = lrn,
              declarationType = "TIR",
              additionalDeclarationType = "A",
              TIRCarnetNumber = Some("1234567"),
              presentationOfTheGoodsDateAndTime = None,
              security = "0",
              reducedDatasetIndicator = Number0,
              specificCircumstanceIndicator = Some("A20"),
              communicationLanguageAtDeparture = None,
              bindingItinerary = Number0,
              limitDate = Some(XMLCalendar("2022-07-15"))
            )

          val converted = TransitOperation.transform(uA)

          converted shouldBe expected
        }

      }

      "for security type entrySummaryDeclaration" must {

        "convert to API format" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "notSubmitted",
               |  "data" : {
               |    "preTaskList" : {
               |      "officeOfDeparture" : {
               |        "id" : "XI000142",
               |        "name" : "Belfast EPU",
               |        "phoneNumber" : "+44 (0)02896 931537"
               |      },
               |      "procedureType" : "normal",
               |      "declarationType" : "TIR",
               |      "tirCarnetReference" : "1234567",
               |      "securityDetailsType" : "entrySummaryDeclaration",
               |      "detailsConfirmed" : true
               |    },
               |    "transportDetails" : {
               |      "authorisationsAndLimit" : {
               |        "limit": {
               |          "limitDate": "2022-07-15"
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

          val expected =
            TransitOperationType06(
              LRN = lrn,
              declarationType = "TIR",
              additionalDeclarationType = "A",
              TIRCarnetNumber = Some("1234567"),
              presentationOfTheGoodsDateAndTime = None,
              security = "1",
              reducedDatasetIndicator = Number0,
              specificCircumstanceIndicator = None,
              communicationLanguageAtDeparture = None,
              bindingItinerary = Number0,
              limitDate = Some(XMLCalendar("2022-07-15"))
            )

          val converted = TransitOperation.transform(uA)

          converted shouldBe expected
        }

      }

      "for security type exitSummaryDeclaration" must {

        "convert to API format" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "notSubmitted",
               |  "data" : {
               |    "preTaskList" : {
               |      "officeOfDeparture" : {
               |        "id" : "XI000142",
               |        "name" : "Belfast EPU",
               |        "phoneNumber" : "+44 (0)02896 931537"
               |      },
               |      "procedureType" : "normal",
               |      "declarationType" : "TIR",
               |      "tirCarnetReference" : "1234567",
               |      "securityDetailsType" : "exitSummaryDeclaration",
               |      "detailsConfirmed" : true
               |    },
               |    "transportDetails" : {
               |      "authorisationsAndLimit" : {
               |        "limit": {
               |          "limitDate": "2022-07-15"
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

          val expected =
            TransitOperationType06(
              LRN = lrn,
              declarationType = "TIR",
              additionalDeclarationType = "A",
              TIRCarnetNumber = Some("1234567"),
              presentationOfTheGoodsDateAndTime = None,
              security = "2",
              reducedDatasetIndicator = Number0,
              specificCircumstanceIndicator = None,
              communicationLanguageAtDeparture = None,
              bindingItinerary = Number0,
              limitDate = Some(XMLCalendar("2022-07-15"))
            )

          val converted = TransitOperation.transform(uA)

          converted shouldBe expected
        }

      }

      "for security type entryAndExitSummaryDeclaration" must {

        "convert to API format" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "notSubmitted",
               |  "data" : {
               |    "preTaskList" : {
               |      "officeOfDeparture" : {
               |        "id" : "XI000142",
               |        "name" : "Belfast EPU",
               |        "phoneNumber" : "+44 (0)02896 931537"
               |      },
               |      "procedureType" : "normal",
               |      "declarationType" : "TIR",
               |      "tirCarnetReference" : "1234567",
               |      "securityDetailsType" : "entryAndExitSummaryDeclaration",
               |      "detailsConfirmed" : true
               |    },
               |    "transportDetails" : {
               |      "authorisationsAndLimit" : {
               |        "limit": {
               |          "limitDate": "2022-07-15"
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

          val expected =
            TransitOperationType06(
              LRN = lrn,
              declarationType = "TIR",
              additionalDeclarationType = "A",
              TIRCarnetNumber = Some("1234567"),
              presentationOfTheGoodsDateAndTime = None,
              security = "3",
              reducedDatasetIndicator = Number0,
              specificCircumstanceIndicator = None,
              communicationLanguageAtDeparture = None,
              bindingItinerary = Number0,
              limitDate = Some(XMLCalendar("2022-07-15"))
            )

          val converted = TransitOperation.transform(uA)

          converted shouldBe expected
        }

      }

      "with no authorisations" must {

        "convert to API format" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "notSubmitted",
               |  "data" : {
               |    "preTaskList" : {
               |      "officeOfDeparture" : {
               |        "id" : "XI000142",
               |        "name" : "Belfast EPU",
               |        "phoneNumber" : "+44 (0)02896 931537"
               |      },
               |      "procedureType" : "normal",
               |      "declarationType" : "TIR",
               |      "tirCarnetReference" : "1234567",
               |      "securityDetailsType" : "entrySummaryDeclaration",
               |      "detailsConfirmed" : true
               |    },
               |    "transportDetails" : {}
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

          val expected =
            TransitOperationType06(
              LRN = lrn,
              declarationType = "TIR",
              additionalDeclarationType = "A",
              TIRCarnetNumber = Some("1234567"),
              presentationOfTheGoodsDateAndTime = None,
              security = "1",
              reducedDatasetIndicator = Number0,
              specificCircumstanceIndicator = None,
              communicationLanguageAtDeparture = None,
              bindingItinerary = Number0,
              limitDate = None
            )

          val converted = TransitOperation.transform(uA)

          converted shouldBe expected
        }
      }

    }
  }
}
