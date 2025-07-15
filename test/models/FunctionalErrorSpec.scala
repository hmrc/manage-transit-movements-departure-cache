/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import base.SpecBase
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class FunctionalErrorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "reads" should {
    "deserialise" when {
      "originalAttributeValue defined" in {
        val json = Json.parse("""
            |{
            |  "errorPointer": "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            |  "errorCode": "12",
            |  "errorReason": "BR20004",
            |  "originalAttributeValue": "GB635733627000"
            |}
            |""".stripMargin)

        val result = json.validate[FunctionalError]

        val expectedResult = FunctionalError(
          errorPointer = XPath("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
          errorCode = "12",
          errorReason = "BR20004",
          originalAttributeValue = Some("GB635733627000")
        )

        result.get.shouldEqual(expectedResult)
      }

      "originalAttributeValue undefined" in {
        val json = Json.parse("""
            |{
            |  "errorPointer": "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            |  "errorCode": "12",
            |  "errorReason": "BR20005"
            |}
            |""".stripMargin)

        val result = json.validate[FunctionalError]

        val expectedResult = FunctionalError(
          errorPointer = XPath("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
          errorCode = "12",
          errorReason = "BR20005",
          originalAttributeValue = None
        )

        result.get.shouldEqual(expectedResult)
      }
    }
  }

  "writes" should {
    "serialise" when {
      "options defined" in {
        val functionalError = FunctionalError(
          errorPointer = XPath("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
          errorCode = "12",
          errorReason = "BR20004",
          originalAttributeValue = Some("GB635733627000")
        )

        val result = Json.toJson(functionalError)

        val expectedResult = Json.parse("""
            |{
            |  "error" : "12",
            |  "businessRuleId" : "BR20004",
            |  "section" : "Trader details",
            |  "invalidDataItem" : "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            |  "invalidAnswer" : "GB635733627000"
            |}
            |""".stripMargin)

        result.shouldEqual(expectedResult)
      }
    }
  }
}
