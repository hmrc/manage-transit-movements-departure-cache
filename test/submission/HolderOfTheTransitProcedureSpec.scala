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

import api.submission.HolderOfTheTransitProcedure
import base.SpecBase
import generated._
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}

class HolderOfTheTransitProcedureSpec extends SpecBase {

  "HolderOfTheTransitProcedure" when {

    "transform is called" must {

      "convert to API format" when {

        "there is an eori and a contact person" in {

          val json: JsValue = Json.parse(s"""
              |{
              |  "_id" : "$uuid",
              |  "lrn" : "$lrn",
              |  "eoriNumber" : "$eoriNumber",
              |  "isSubmitted" : false,
              |  "data" : {
              |    "traderDetails" : {
              |      "holderOfTransit" : {
              |        "eoriYesNo" : true,
              |        "eori" : "GB123456789000",
              |        "name" : "John Doe",
              |        "country" : {
              |          "code" : "GB",
              |          "description" : "United Kingdom"
              |        },
              |        "address" : {
              |          "numberAndStreet" : "21 Test Lane",
              |          "city" : "Newcastle upon Tyne",
              |          "postalCode" : "NE1 1NE"
              |        },
              |        "addContact" : true,
              |        "contact" : {
              |          "name" : "Joe Bloggs",
              |          "telephoneNumber" : "+44 808 157 0192"
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

          val expected = HolderOfTheTransitProcedureType14(
            identificationNumber = Some("GB123456789000"),
            TIRHolderIdentificationNumber = None,
            name = Some("John Doe"),
            Address = Some(
              AddressType17(
                streetAndNumber = "21 Test Lane",
                postcode = Some("NE1 1NE"),
                city = "Newcastle upon Tyne",
                country = "GB"
              )
            ),
            ContactPerson = Some(
              ContactPersonType05(
                name = "Joe Bloggs",
                phoneNumber = "+44 808 157 0192",
                eMailAddress = None
              )
            )
          )

          val converted: HolderOfTheTransitProcedureType14 = HolderOfTheTransitProcedure.transform(uA)

          converted shouldBe expected
        }

        "there is a TIR id, no address and not a contact person" in {

          val json: JsValue = Json.parse(s"""
              |{
              |  "_id" : "$uuid",
              |  "lrn" : "$lrn",
              |  "eoriNumber" : "$eoriNumber",
              |  "isSubmitted" : false,
              |  "data" : {
              |    "traderDetails" : {
              |      "holderOfTransit" : {
              |        "tirIdentificationYesNo" : true,
              |        "tirIdentification" : "AAA/999/99999",
              |        "name" : "John Doe",
              |        "country" : {
              |          "code" : "GB",
              |          "description" : "United Kingdom"
              |        },
              |        "addContact" : false
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

          val expected = HolderOfTheTransitProcedureType14(
            identificationNumber = None,
            TIRHolderIdentificationNumber = Some("AAA/999/99999"),
            name = Some("John Doe"),
            Address = None,
            ContactPerson = None
          )

          val converted: HolderOfTheTransitProcedureType14 = HolderOfTheTransitProcedure.transform(uA)

          converted shouldBe expected
        }
      }

      "throw an exception" when {

        "there is no holder of transit" in {

          val json: JsValue = Json.parse(s"""
              |{
              |  "_id" : "$uuid",
              |  "lrn" : "$lrn",
              |  "eoriNumber" : "$eoriNumber",
              |  "isSubmitted" : false,
              |  "data" : {
              |    "traderDetails" : {}
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

          a[Exception] must be thrownBy HolderOfTheTransitProcedure.transform(uA)
        }
      }
    }
  }
}
