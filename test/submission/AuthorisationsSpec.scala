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
            |  "data" : {
            |    "transportDetails" : {
            |      "authorisationsAndLimit" : {
            |        "authorisations" : [
            |          {
            |            "inferredAuthorisationType" : "TRD",
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
            sequenceNumber = "1",
            typeValue = "C524",
            referenceNumber = "TRD1"
          ),
          AuthorisationType03(
            sequenceNumber = "2",
            typeValue = "C523",
            referenceNumber = "SSE1"
          ),
          AuthorisationType03(
            sequenceNumber = "3",
            typeValue = "C521",
            referenceNumber = "ACR1"
          )
        )

        val converted: Seq[AuthorisationType03] = Authorisations.transform(uA)

        converted shouldBe expected
      }
    }
  }
}
