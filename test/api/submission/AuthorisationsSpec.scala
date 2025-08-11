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

import base.SpecBase
import generated.*
import models.UserAnswers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsValue, Json}

class AuthorisationsSpec extends SpecBase with ScalaCheckPropertyChecks {

  "Authorisations" when {

    "transform is called" must {

      "convert to API format" in {
        val json: JsValue = Json.parse(s"""
            |{
            |  "_id" : "$uuid",
            |  "lrn" : "$lrn",
            |  "eoriNumber" : "$eoriNumber",
            |  "isSubmitted" : "notSubmitted",
            |  "data" : {
            |    "transportDetails" : {
            |      "authorisationsAndLimit" : {
            |        "authorisations" : [
            |          {
            |            "inferredAuthorisationType" : {
            |              "code": "C524",
            |              "description": "TRD - Authorisation to use transit declaration with a reduced dataset (Column 9e, Annex A of Delegated Regulation (EU) 2015/2446)"
            |            },
            |            "authorisationReferenceNumber" : "TRD1"
            |          },
            |          {
            |            "authorisationType" : {
            |              "code": "C523",
            |              "description": "SSE - Authorisation for the use of seals of a special type (Column 9d, Annex A of Delegated Regulation (EU) 2015/2446)"
            |            },
            |            "authorisationReferenceNumber" : "SSE1"
            |          },
            |          {
            |            "authorisationType" : {
            |              "code": "C521",
            |              "description": "ACR - Authorisation for the status of authorised consignor for Union transit (Column 9b, Annex A of Delegated Regulation (EU) 2015/2446)"
            |            },
            |            "authorisationReferenceNumber" : "ACR1"
            |          }
            |        ]
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

        val expected = Seq(
          AuthorisationType02(
            sequenceNumber = 1,
            typeValue = "C524",
            referenceNumber = "TRD1"
          ),
          AuthorisationType02(
            sequenceNumber = 2,
            typeValue = "C523",
            referenceNumber = "SSE1"
          ),
          AuthorisationType02(
            sequenceNumber = 3,
            typeValue = "C521",
            referenceNumber = "ACR1"
          )
        )

        val converted: Seq[AuthorisationType02] = Authorisations.transform(uA)

        converted shouldEqual expected
      }
    }
  }
}
