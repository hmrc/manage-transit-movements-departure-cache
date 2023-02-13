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
import play.api.libs.json.{JsValue, Json}

class AuthorisationsSpec extends SpecBase {

  "Authorisations" when {

    "transform is called" must {

      "convert to API format" in {

        val json: JsValue = Json.parse(s"""
             |{
             |  "_id" : "$uuid",
             |  "lrn" : "$lrn",
             |  "eoriNumber" : "$eoriNumber",
             |  "data" : {
             |    "transportDetails" : {
             |      "authorisationsAndLimit" : {
             |        "limit": {
             |          "limitDate": "2023-01-26T15:39:32.578+0000"
             |        },
             |        "authorisations" : [
             |          {
             |            "authorisationType" : "FOO",
             |            "authorisationReferenceNumber" : "TRD123"
             |          },
             |          {
             |            "authorisationType" : "BAR",
             |            "authorisationReferenceNumber" : "TRD223"
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
          AuthorisationType03("0", "FOO", "TRD123"),
          AuthorisationType03("1", "BAR", "TRD223")
        )

        val converted: Seq[AuthorisationType03] = Authorisations.transform(uA)

        converted shouldBe expected

      }
    }
  }
}
