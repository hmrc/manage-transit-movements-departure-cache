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
import config.Constants.RepresentativeStatusCode.DirectRepresentation
import generated.*
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}

class RepresentativeSpec extends SpecBase with AppWithDefaultMockFixtures {

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
                |  "isSubmitted" : "notSubmitted",
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
                |  "createdAt" : "2022-09-05T15:58:44.188Z",
                |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
                |  "isTransitional": false
                |}
                |""".stripMargin)

            val uA: UserAnswers = json.as[UserAnswers]

            val expected = RepresentativeType06(
              identificationNumber = "GB123456789000",
              status = DirectRepresentation,
              ContactPerson = Some(
                ContactPersonType03(
                  name = "Jack",
                  phoneNumber = "+44 808 157 0192",
                  eMailAddress = None
                )
              )
            )

            val converted: Option[RepresentativeType06] = Representative.transform(uA)

            converted.get shouldEqual expected
          }

          "there is not a contact person" in {

            val json: JsValue = Json.parse(s"""
                |{
                |  "_id" : "$uuid",
                |  "lrn" : "$lrn",
                |  "eoriNumber" : "$eoriNumber",
                |  "isSubmitted" : "notSubmitted",
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
                |  "createdAt" : "2022-09-05T15:58:44.188Z",
                |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
                |  "isTransitional": false
                |}
                |""".stripMargin)

            val uA: UserAnswers = json.as[UserAnswers]

            val expected = RepresentativeType06(
              identificationNumber = "GB123456789000",
              status = DirectRepresentation,
              ContactPerson = None
            )

            val converted: Option[RepresentativeType06] = Representative.transform(uA)

            converted.get shouldEqual expected
          }
        }

        "there is not a representative" in {

          val json: JsValue = Json.parse(s"""
              |{
              |  "_id" : "$uuid",
              |  "lrn" : "$lrn",
              |  "eoriNumber" : "$eoriNumber",
              |  "isSubmitted" : "notSubmitted",
              |  "data" : {
              |    "traderDetails" : {
              |      "actingAsRepresentative" : false
              |    }
              |  },
              |  "tasks" : {},
              |  "createdAt" : "2022-09-05T15:58:44.188Z",
              |  "lastUpdated" : "2022-09-07T10:33:23.472Z",
              |  "isTransitional": false
              |}
              |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers]

          val converted: Option[RepresentativeType06] = Representative.transform(uA)

          converted shouldEqual None
        }
      }
    }
  }
}
