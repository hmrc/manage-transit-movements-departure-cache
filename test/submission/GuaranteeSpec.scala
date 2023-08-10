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

import api.submission.Guarantee
import base.SpecBase
import generated._
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}

class GuaranteeSpec extends SpecBase {

  "Guarantee" when {

    "transform is called" must {

      "convert to API format" in {

        val json: JsValue = Json.parse(s"""
            |{
            |  "_id" : "$uuid",
            |  "lrn" : "$lrn",
            |  "eoriNumber" : "$eoriNumber",
            |  "data" : {
            |    "guaranteeDetails" : [
            |      {
            |        "guaranteeType" : "1",
            |        "otherReference" : "otherRefNo1",
            |        "referenceNumber" : "refNo1",
            |        "accessCode" : "1234",
            |        "liabilityAmount" : 1000,
            |        "currency" : {
            |          "currency" : "GBP",
            |          "description" : "Sterling"
            |        }
            |      },
            |      {
            |        "guaranteeType" : "A",
            |        "otherReference" : "otherRefNo2",
            |        "referenceNumber" : "refNo2",
            |        "accessCode" : "5678",
            |        "liabilityAmount" : 2000,
            |        "currency" : {
            |          "currency" : "EUR",
            |          "description" : "Euro"
            |        }
            |      }
            |    ]
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
          GuaranteeType02(
            sequenceNumber = "1",
            guaranteeType = "1",
            otherGuaranteeReference = Some("otherRefNo1"),
            GuaranteeReference = Seq(
              GuaranteeReferenceType03(
                sequenceNumber = "1",
                GRN = Some("refNo1"),
                accessCode = Some("1234"),
                amountToBeCovered = Some(1000),
                currency = Some("GBP")
              )
            )
          ),
          GuaranteeType02(
            sequenceNumber = "2",
            guaranteeType = "A",
            otherGuaranteeReference = Some("otherRefNo2"),
            GuaranteeReference = Seq(
              GuaranteeReferenceType03(
                sequenceNumber = "2",
                GRN = Some("refNo2"),
                accessCode = Some("5678"),
                amountToBeCovered = Some(2000),
                currency = Some("EUR")
              )
            )
          )
        )

        val converted: Seq[GuaranteeType02] = Guarantee.transform(uA)

        converted shouldBe expected
      }
    }
  }
}
