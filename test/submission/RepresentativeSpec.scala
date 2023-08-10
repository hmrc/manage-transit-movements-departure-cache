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

import api.submission.Representative
import base.SpecBase
import generated._
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}

class RepresentativeSpec extends SpecBase {

  "Guarantee" when {

    "transform is called" must {

      "convert to API format" when {

        "there is a representative" when {

          "there is a contact person" in {

            val json: JsValue = Json.parse(s"""
                |{
                |  "_id" : "$uuid",
                |  "lrn" : "$lrn",
                |  "eoriNumber" : "$eoriNumber",
                |  "data" : {
                |    "traderDetails" : {
                |      "actingAsRepresentative" : true,
                |      "representative" : {
                |        "eori" : "GB123456789000",
                |        "addDetails" : true,
                |        "name" : "Jack",
                |        "telephoneNumber" : "+44 808 157 0192"
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

            val expected = RepresentativeType05(
              identificationNumber = "GB123456789000",
              status = "2",
              ContactPerson = Some(
                ContactPersonType05(
                  name = "Jack",
                  phoneNumber = "+44 808 157 0192",
                  eMailAddress = None
                )
              )
            )

            val converted: Option[RepresentativeType05] = Representative.transform(uA)

            converted.get shouldBe expected
          }

          "there is not a contact person" in {

            val json: JsValue = Json.parse(s"""
                |{
                |  "_id" : "$uuid",
                |  "lrn" : "$lrn",
                |  "eoriNumber" : "$eoriNumber",
                |  "data" : {
                |    "traderDetails" : {
                |      "actingAsRepresentative" : true,
                |      "representative" : {
                |        "eori" : "GB123456789000",
                |        "addDetails" : false
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

            val expected = RepresentativeType05(
              identificationNumber = "GB123456789000",
              status = "2",
              ContactPerson = None
            )

            val converted: Option[RepresentativeType05] = Representative.transform(uA)

            converted.get shouldBe expected
          }
        }

        "there is not a representative" in {

          val json: JsValue = Json.parse(s"""
              |{
              |  "_id" : "$uuid",
              |  "lrn" : "$lrn",
              |  "eoriNumber" : "$eoriNumber",
              |  "data" : {
              |    "traderDetails" : {
              |      "actingAsRepresentative" : false
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

          val converted: Option[RepresentativeType05] = Representative.transform(uA)

          converted shouldBe None
        }
      }
    }
  }
}
