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

import api.submission.Authorisations
import base.SpecBase
import generated._
import models.UserAnswers
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsValue, Json}

class AuthorisationsSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val procedureTypeGen = Gen.oneOf("normal", "simplified")
  private val inlandModeGen    = Gen.oneOf("maritime", "rail", "road", "air", "mail", "fixed", "waterway", "unknown")

  "Authorisations" when {

    "transform is called" must {

      "convert to API format" when {

        "reduced dataset indicator is true" when {

          "maritime/rail/air inland mode" in {

            val inlandModeGen = Gen.oneOf("maritime", "rail", "air")

            forAll(inlandModeGen, procedureTypeGen) {
              (inlandMode, procedureType) =>
                val json: JsValue = Json.parse(s"""
                    |{
                    |  "_id" : "$uuid",
                    |  "lrn" : "$lrn",
                    |  "eoriNumber" : "$eoriNumber",
                    |  "data" : {
                    |    "preTaskList" : {
                    |      "procedureType" : "$procedureType"
                    |    },
                    |    "traderDetails" : {
                    |      "consignment" : {
                    |        "approvedOperator" : true
                    |      }
                    |    },
                    |    "transportDetails" : {
                    |      "inlandMode" : "$inlandMode",
                    |      "authorisationsAndLimit" : {
                    |        "authorisations" : [
                    |          {
                    |            "authorisationReferenceNumber" : "TRD1"
                    |          },
                    |          {
                    |            "authorisationType" : "SSE",
                    |            "authorisationReferenceNumber" : "SSE1"
                    |          },
                    |          {
                    |            "authorisationType" : "ACR",
                    |            "authorisationReferenceNumber" : "ACR1"
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
                  AuthorisationType03(
                    sequenceNumber = "0",
                    typeValue = "C524",
                    referenceNumber = "TRD1"
                  ),
                  AuthorisationType03(
                    sequenceNumber = "1",
                    typeValue = "C523",
                    referenceNumber = "SSE1"
                  ),
                  AuthorisationType03(
                    sequenceNumber = "2",
                    typeValue = "C521",
                    referenceNumber = "ACR1"
                  )
                )

                val converted: Seq[AuthorisationType03] = Authorisations.transform(uA)

                converted shouldBe expected
            }
          }

          "road/mail/fixed/waterway/unknown inland mode" when {

            val inlandModeGen = Gen.oneOf("road", "mail", "fixed", "waterway", "unknown")

            "simplified procedure type" in {

              forAll(inlandModeGen) {
                inlandMode =>
                  val json: JsValue = Json.parse(s"""
                       |{
                       |  "_id" : "$uuid",
                       |  "lrn" : "$lrn",
                       |  "eoriNumber" : "$eoriNumber",
                       |  "data" : {
                       |    "preTaskList" : {
                       |      "procedureType" : "simplified"
                       |    },
                       |    "traderDetails" : {
                       |      "consignment" : {
                       |        "approvedOperator" : true
                       |      }
                       |    },
                       |    "transportDetails" : {
                       |      "inlandMode" : "$inlandMode",
                       |      "authorisationsAndLimit" : {
                       |        "authorisations" : [
                       |          {
                       |            "authorisationReferenceNumber" : "ACR1"
                       |          },
                       |          {
                       |            "authorisationType" : "SSE",
                       |            "authorisationReferenceNumber" : "SSE1"
                       |          },
                       |          {
                       |            "authorisationType" : "TRD",
                       |            "authorisationReferenceNumber" : "TRD1"
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
                    AuthorisationType03(
                      sequenceNumber = "0",
                      typeValue = "C521",
                      referenceNumber = "ACR1"
                    ),
                    AuthorisationType03(
                      sequenceNumber = "1",
                      typeValue = "C523",
                      referenceNumber = "SSE1"
                    ),
                    AuthorisationType03(
                      sequenceNumber = "2",
                      typeValue = "C524",
                      referenceNumber = "TRD1"
                    )
                  )

                  val converted: Seq[AuthorisationType03] = Authorisations.transform(uA)

                  converted shouldBe expected
              }
            }

            "normal procedure type" in {

              forAll(inlandModeGen) {
                inlandMode =>
                  val json: JsValue = Json.parse(s"""
                       |{
                       |  "_id" : "$uuid",
                       |  "lrn" : "$lrn",
                       |  "eoriNumber" : "$eoriNumber",
                       |  "data" : {
                       |    "preTaskList" : {
                       |      "procedureType" : "normal"
                       |    },
                       |    "traderDetails" : {
                       |      "consignment" : {
                       |        "approvedOperator" : true
                       |      }
                       |    },
                       |    "transportDetails" : {
                       |      "inlandMode" : "$inlandMode",
                       |      "authorisationsAndLimit" : {
                       |        "authorisations" : [
                       |          {
                       |            "authorisationType" : "ACR",
                       |            "authorisationReferenceNumber" : "ACR1"
                       |          },
                       |          {
                       |            "authorisationType" : "SSE",
                       |            "authorisationReferenceNumber" : "SSE1"
                       |          },
                       |          {
                       |            "authorisationType" : "TRD",
                       |            "authorisationReferenceNumber" : "TRD1"
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
                    AuthorisationType03(
                      sequenceNumber = "0",
                      typeValue = "C521",
                      referenceNumber = "ACR1"
                    ),
                    AuthorisationType03(
                      sequenceNumber = "1",
                      typeValue = "C523",
                      referenceNumber = "SSE1"
                    ),
                    AuthorisationType03(
                      sequenceNumber = "2",
                      typeValue = "C524",
                      referenceNumber = "TRD1"
                    )
                  )

                  val converted: Seq[AuthorisationType03] = Authorisations.transform(uA)

                  converted shouldBe expected
              }
            }
          }
        }

        "reduced dataset indicator is false" in {

          val inlandModeGen = Gen.oneOf("maritime", "rail", "road", "air", "mail", "fixed", "waterway", "unknown")

          forAll(inlandModeGen, procedureTypeGen) {
            (inlandMode, procedureType) =>
              val json: JsValue = Json.parse(s"""
                  |{
                  |  "_id" : "$uuid",
                  |  "lrn" : "$lrn",
                  |  "eoriNumber" : "$eoriNumber",
                  |  "data" : {
                  |    "preTaskList" : {
                  |      "procedureType" : "$procedureType"
                  |    },
                  |    "traderDetails" : {
                  |      "consignment" : {
                  |        "approvedOperator" : false
                  |      }
                  |    },
                  |    "transportDetails" : {
                  |      "inlandMode" : "$inlandMode",
                  |      "authorisationsAndLimit" : {
                  |        "authorisations" : [
                  |          {
                  |            "authorisationType" : "ACR",
                  |            "authorisationReferenceNumber" : "ACR1"
                  |          },
                  |          {
                  |            "authorisationType" : "SSE",
                  |            "authorisationReferenceNumber" : "SSE1"
                  |          },
                  |          {
                  |            "authorisationType" : "TRD",
                  |            "authorisationReferenceNumber" : "TRD1"
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
                AuthorisationType03(
                  sequenceNumber = "0",
                  typeValue = "C521",
                  referenceNumber = "ACR1"
                ),
                AuthorisationType03(
                  sequenceNumber = "1",
                  typeValue = "C523",
                  referenceNumber = "SSE1"
                ),
                AuthorisationType03(
                  sequenceNumber = "2",
                  typeValue = "C524",
                  referenceNumber = "TRD1"
                )
              )

              val converted: Seq[AuthorisationType03] = Authorisations.transform(uA)

              converted shouldBe expected
          }
        }

        "reduced dataset indicator is undefined" in {

          forAll(inlandModeGen, procedureTypeGen) {
            (inlandMode, procedureType) =>
              val json: JsValue = Json.parse(s"""
                  |{
                  |  "_id" : "$uuid",
                  |  "lrn" : "$lrn",
                  |  "eoriNumber" : "$eoriNumber",
                  |  "data" : {
                  |    "preTaskList" : {
                  |      "procedureType" : "$procedureType"
                  |    },
                  |    "traderDetails" : {
                  |      "consignment" : {}
                  |    },
                  |    "transportDetails" : {
                  |      "inlandMode" : "$inlandMode",
                  |      "authorisationsAndLimit" : {
                  |        "authorisations" : [
                  |          {
                  |            "authorisationType" : "ACR",
                  |            "authorisationReferenceNumber" : "ACR1"
                  |          },
                  |          {
                  |            "authorisationType" : "SSE",
                  |            "authorisationReferenceNumber" : "SSE1"
                  |          },
                  |          {
                  |            "authorisationType" : "TRD",
                  |            "authorisationReferenceNumber" : "TRD1"
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
                AuthorisationType03(
                  sequenceNumber = "0",
                  typeValue = "C521",
                  referenceNumber = "ACR1"
                ),
                AuthorisationType03(
                  sequenceNumber = "1",
                  typeValue = "C523",
                  referenceNumber = "SSE1"
                ),
                AuthorisationType03(
                  sequenceNumber = "2",
                  typeValue = "C524",
                  referenceNumber = "TRD1"
                )
              )

              val converted: Seq[AuthorisationType03] = Authorisations.transform(uA)

              converted shouldBe expected
          }
        }
      }
    }
  }
}
