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
import generated.*
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}

class GuaranteeSpec extends SpecBase with AppWithDefaultMockFixtures {

  "Guarantee" when {

    "transform is called" must {

      "convert to API format" when {

        "one instance of guarantee types" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "notSubmitted",
               |  "data" : {
               |  "guaranteeDetails" : {
               |      "guaranteeDetails" : [
               |        {
               |          "guaranteeType" : {
               |            "code": "1"
               |          },
               |          "otherReference" : "otherRefNo1",
               |          "referenceNumber" : "refNo1",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {
               |            "code": "A"
               |          },
               |          "otherReference" : "otherRefNo2",
               |          "referenceNumber" : "refNo2",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "Euro"
               |          }
               |        }
               |      ]
               |    }
               |  },
               |  "tasks" : {},
               |  "createdAt" : "2022-09-05T15:58:44.188Z",
               |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
               |  "isTransitional": false
               |}
               |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers]

          val expected = Seq(
            GuaranteeType05(
              sequenceNumber = 1,
              guaranteeType = "1",
              otherGuaranteeReference = Some("otherRefNo1"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = Some("refNo1"),
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                )
              )
            ),
            GuaranteeType05(
              sequenceNumber = 2,
              guaranteeType = "A",
              otherGuaranteeReference = Some("otherRefNo2"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = Some("refNo2"),
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            )
          )

          val converted: Seq[GuaranteeType05] = Guarantee.transform(uA)

          converted shouldEqual expected
        }

        "multiple instances of guarantee types with different other references" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "notSubmitted",
               |  "data" : {
               |    "guaranteeDetails" : {
               |      "guaranteeDetails" : [
               |        {
               |          "guaranteeType" : {
               |            "code": "3"
               |          },
               |          "otherReferenceYesNo" : true,
               |          "otherReference" : "3_1",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {
               |            "code": "3"
               |          },
               |          "otherReferenceYesNo" : true,
               |          "otherReference" : "3_2",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "EUR"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {
               |            "code": "8"
               |          },
               |          "otherReference" : "8_1",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {
               |            "code": "8"
               |          },
               |          "otherReference" : "8_2",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "Euro"
               |          }
               |        }
               |      ]
               |    }
               |  },
               |  "tasks" : {},
               |  "createdAt" : "2022-09-05T15:58:44.188Z",
               |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
               |  "isTransitional": false
               |}
               |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers]

          val expected = Seq(
            GuaranteeType05(
              sequenceNumber = 1,
              guaranteeType = "3",
              otherGuaranteeReference = Some("3_1"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                )
              )
            ),
            GuaranteeType05(
              sequenceNumber = 2,
              guaranteeType = "3",
              otherGuaranteeReference = Some("3_2"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            ),
            GuaranteeType05(
              sequenceNumber = 3,
              guaranteeType = "8",
              otherGuaranteeReference = Some("8_1"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                )
              )
            ),
            GuaranteeType05(
              sequenceNumber = 4,
              guaranteeType = "8",
              otherGuaranteeReference = Some("8_2"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            )
          )

          val converted: Seq[GuaranteeType05] = Guarantee.transform(uA)

          converted shouldEqual expected
        }

        "multiple instances of guarantee types with same other references" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "notSubmitted",
               |  "data" : {
               |    "guaranteeDetails" : {
               |      "guaranteeDetails" : [
               |        {
               |          "guaranteeType" : {
               |            "code": "3"
               |          },
               |          "otherReferenceYesNo" : true,
               |          "otherReference" : "3",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {
               |            "code": "3"
               |          },
               |          "otherReferenceYesNo" : true,
               |          "otherReference" : "3",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "EUR"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {
               |            "code": "8"
               |          },
               |          "otherReference" : "8",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {
               |            "code": "8"
               |          },
               |          "otherReference" : "8",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "Euro"
               |          }
               |        }
               |      ]
               |    }
               |  },
               |  "tasks" : {},
               |  "createdAt" : "2022-09-05T15:58:44.188Z",
               |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
               |  "isTransitional": false
               |}
               |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers]

          val expected = Seq(
            GuaranteeType05(
              sequenceNumber = 1,
              guaranteeType = "3",
              otherGuaranteeReference = Some("3"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                ),
                GuaranteeReferenceType03(
                  sequenceNumber = 2,
                  GRN = None,
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            ),
            GuaranteeType05(
              sequenceNumber = 2,
              guaranteeType = "8",
              otherGuaranteeReference = Some("8"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                ),
                GuaranteeReferenceType03(
                  sequenceNumber = 2,
                  GRN = None,
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            )
          )

          val converted: Seq[GuaranteeType05] = Guarantee.transform(uA)

          converted shouldEqual expected
        }

        "multiple instances of guarantee types with no other references" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "notSubmitted",
               |  "data" : {
               |    "guaranteeDetails" : {
               |      "guaranteeDetails" : [
               |        {
               |          "guaranteeType" : {
               |            "code": "3"
               |          },
               |          "otherReferenceYesNo" : false
               |        },
               |        {
               |          "guaranteeType" : {
               |            "code": "3"
               |          },
               |          "otherReferenceYesNo" : false
               |        },
               |        {
               |          "guaranteeType" : {
               |            "code": "0"
               |          },
               |          "referenceNumber" : "0_1",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {
               |            "code": "0"
               |          },
               |          "referenceNumber" : "0_2",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "Euro"
               |          }
               |        }
               |      ]
               |    }
               |  },
               |  "tasks" : {},
               |  "createdAt" : "2022-09-05T15:58:44.188Z",
               |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
               |  "isTransitional": false
               |}
               |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers]

          val expected = Seq(
            GuaranteeType05(
              sequenceNumber = 1,
              guaranteeType = "3",
              otherGuaranteeReference = None,
              GuaranteeReference = Nil
            ),
            GuaranteeType05(
              sequenceNumber = 2,
              guaranteeType = "0",
              otherGuaranteeReference = None,
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = Some("0_1"),
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                ),
                GuaranteeReferenceType03(
                  sequenceNumber = 2,
                  GRN = Some("0_2"),
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            )
          )

          val converted: Seq[GuaranteeType05] = Guarantee.transform(uA)

          converted shouldEqual expected
        }
      }
    }

    "transformIE013 is called" must {

      "convert to API format" when {

        "one instance of guarantee types" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "guaranteeAmendment",
               |  "data" : {
               |    "guaranteeDetails" : {
               |      "guaranteeDetails" : [
               |        {
               |          "guaranteeType" : {"code" : "1"},
               |          "otherReference" : "otherRefNo1",
               |          "referenceNumber" : "refNo1",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {"code" : "A"},
               |          "otherReference" : "otherRefNo2",
               |          "referenceNumber" : "refNo2",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "Euro"
               |          }
               |        }
               |      ]
               |    }
               |  },
               |  "tasks" : {},
               |  "createdAt" : "2022-09-05T15:58:44.188Z",
               |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
               |  "isTransitional": false
               |}
               |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers]

          val expected = Seq(
            GuaranteeType05(
              sequenceNumber = 1,
              guaranteeType = "1",
              otherGuaranteeReference = Some("otherRefNo1"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = Some("refNo1"),
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                )
              )
            ),
            GuaranteeType05(
              sequenceNumber = 2,
              guaranteeType = "A",
              otherGuaranteeReference = Some("otherRefNo2"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = Some("refNo2"),
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            )
          )

          val converted: Seq[GuaranteeType05] = Guarantee.transform(uA)

          converted shouldEqual expected
        }

        "multiple instances of guarantee types with different other references" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "guaranteeAmendment",
               |  "data" : {
               |    "guaranteeDetails" : {
               |      "guaranteeDetails" : [
               |        {
               |          "guaranteeType" : {"code" : "3"},
               |          "otherReferenceYesNo" : true,
               |          "otherReference" : "3_1",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {"code" : "3"},
               |          "otherReferenceYesNo" : true,
               |          "otherReference" : "3_2",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "EUR"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {"code" : "8"},
               |          "otherReference" : "8_1",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {"code" : "8"},
               |          "otherReference" : "8_2",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "Euro"
               |          }
               |        }
               |      ]
               |    }
               |  },
               |  "tasks" : {},
               |  "createdAt" : "2022-09-05T15:58:44.188Z",
               |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
               |  "isTransitional": false
               |}
               |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers]

          val expected = Seq(
            GuaranteeType05(
              sequenceNumber = 1,
              guaranteeType = "3",
              otherGuaranteeReference = Some("3_1"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                )
              )
            ),
            GuaranteeType05(
              sequenceNumber = 2,
              guaranteeType = "3",
              otherGuaranteeReference = Some("3_2"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            ),
            GuaranteeType05(
              sequenceNumber = 3,
              guaranteeType = "8",
              otherGuaranteeReference = Some("8_1"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                )
              )
            ),
            GuaranteeType05(
              sequenceNumber = 4,
              guaranteeType = "8",
              otherGuaranteeReference = Some("8_2"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            )
          )

          val converted: Seq[GuaranteeType05] = Guarantee.transform(uA)

          converted shouldEqual expected
        }

        "multiple instances of guarantee types with same other references" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "guaranteeAmendment",
               |  "data" : {
               |    "guaranteeDetails" : {
               |      "guaranteeDetails" : [
               |        {
               |          "guaranteeType" : {"code" : "3"},
               |          "otherReferenceYesNo" : true,
               |          "otherReference" : "3",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {"code" : "3"},
               |          "otherReferenceYesNo" : true,
               |          "otherReference" : "3",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "EUR"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {"code" : "8"},
               |          "otherReference" : "8",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {"code" : "8"},
               |          "otherReference" : "8",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "Euro"
               |          }
               |        }
               |      ]
               |    }
               |  },
               |  "tasks" : {},
               |  "createdAt" : "2022-09-05T15:58:44.188Z",
               |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
               |  "isTransitional": false
               |}
               |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers]

          val expected = Seq(
            GuaranteeType05(
              sequenceNumber = 1,
              guaranteeType = "3",
              otherGuaranteeReference = Some("3"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                ),
                GuaranteeReferenceType03(
                  sequenceNumber = 2,
                  GRN = None,
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            ),
            GuaranteeType05(
              sequenceNumber = 2,
              guaranteeType = "8",
              otherGuaranteeReference = Some("8"),
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = None,
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                ),
                GuaranteeReferenceType03(
                  sequenceNumber = 2,
                  GRN = None,
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            )
          )

          val converted: Seq[GuaranteeType05] = Guarantee.transform(uA)

          converted shouldEqual expected
        }

        "multiple instances of guarantee types with no other references" in {

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$uuid",
               |  "lrn" : "$lrn",
               |  "eoriNumber" : "$eoriNumber",
               |  "isSubmitted" : "guaranteeAmendment",
               |  "data" : {
               |    "guaranteeDetails" : {
               |      "guaranteeDetails" : [
               |        {
               |          "guaranteeType" : {"code" : "3"},
               |          "otherReferenceYesNo" : false
               |        },
               |        {
               |          "guaranteeType" : {"code" : "3"},
               |          "otherReferenceYesNo" : false
               |        },
               |        {
               |          "guaranteeType" : {"code" : "0"},
               |          "referenceNumber" : "0_1",
               |          "accessCode" : "1234",
               |          "liabilityAmount" : 1000,
               |          "currency" : {
               |            "currency" : "GBP",
               |            "description" : "Sterling"
               |          }
               |        },
               |        {
               |          "guaranteeType" : {"code" : "0"},
               |          "referenceNumber" : "0_2",
               |          "accessCode" : "5678",
               |          "liabilityAmount" : 2000,
               |          "currency" : {
               |            "currency" : "EUR",
               |            "description" : "Euro"
               |          }
               |        }
               |      ]
               |    }
               |  },
               |  "tasks" : {},
               |  "createdAt" : "2022-09-05T15:58:44.188Z",
               |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
               |  "isTransitional": false
               |}
               |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers]

          val expected = Seq(
            GuaranteeType05(
              sequenceNumber = 1,
              guaranteeType = "3",
              otherGuaranteeReference = None,
              GuaranteeReference = Nil
            ),
            GuaranteeType05(
              sequenceNumber = 2,
              guaranteeType = "0",
              otherGuaranteeReference = None,
              GuaranteeReference = Seq(
                GuaranteeReferenceType03(
                  sequenceNumber = 1,
                  GRN = Some("0_1"),
                  accessCode = Some("1234"),
                  amountToBeCovered = 1000,
                  currency = "GBP"
                ),
                GuaranteeReferenceType03(
                  sequenceNumber = 2,
                  GRN = Some("0_2"),
                  accessCode = Some("5678"),
                  amountToBeCovered = 2000,
                  currency = "EUR"
                )
              )
            )
          )

          val converted: Seq[GuaranteeType05] = Guarantee.transform(uA)

          converted shouldEqual expected
        }
      }
    }
  }
}
