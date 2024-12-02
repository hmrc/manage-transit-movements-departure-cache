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
import org.scalacheck.Arbitrary.arbitrary
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

        result.get.shouldBe(expectedResult)
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

        result.get.shouldBe(expectedResult)
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
            |  "invalidDataItem" : "Holder of the transit procedure: Identification number",
            |  "invalidAnswer" : "GB635733627000"
            |}
            |""".stripMargin)

        result.shouldBe(expectedResult)
      }
    }
  }

  "invalidDataItem" when {
    "/CC015C/HolderOfTheTransitProcedure/identificationNumber" must {
      "return Holder of the transit procedure identification number" in {
        forAll(arbitrary[FunctionalError]) {
          functionalError =>
            val errorPointer   = XPath("/CC015C/HolderOfTheTransitProcedure/identificationNumber")
            val result         = functionalError.copy(errorPointer = errorPointer).invalidDataItem
            val expectedResult = "Holder of the transit procedure: Identification number"
            result.shouldBe(expectedResult)
        }
      }
    }

    "/CC015C/Consignment/HouseConsignment[50]/ConsignmentItem[10]/consignor" must {
      "return Consignment item 10: Consignor" in {
        forAll(arbitrary[FunctionalError]) {
          functionalError =>
            val errorPointer   = XPath("/CC015C/Consignment/HouseConsignment[50]/ConsignmentItem[10]/consignor")
            val result         = functionalError.copy(errorPointer = errorPointer).invalidDataItem
            val expectedResult = "Consignment item 10: Consignor"
            result.shouldBe(expectedResult)
        }
      }
    }

    "/CC015C/Consignment/HouseConsignment[5]/ConsignmentItem[10]/SupportingDocument[22]/type" must {
      "return Consignment item 10: Supporting document 22: Type" in {
        forAll(arbitrary[FunctionalError]) {
          functionalError =>
            val errorPointer   = XPath("/CC015C/Consignment/HouseConsignment[5]/ConsignmentItem[10]/SupportingDocument[22]/type")
            val result         = functionalError.copy(errorPointer = errorPointer).invalidDataItem
            val expectedResult = "Consignment item 10: Supporting document 22: Type"
            result.shouldBe(expectedResult)
        }
      }
    }

    "//Consignment/LocationOfGoods" must {
      "return Location of goods" in {
        forAll(arbitrary[FunctionalError]) {
          functionalError =>
            val errorPointer   = XPath("//Consignment/LocationOfGoods")
            val result         = functionalError.copy(errorPointer = errorPointer).invalidDataItem
            val expectedResult = "Location of goods"
            result.shouldBe(expectedResult)
        }
      }
    }

    "/CC015C/Consignment/referenceNumberUCR" must {
      "return Reference number UCR" in {
        forAll(arbitrary[FunctionalError]) {
          functionalError =>
            val errorPointer   = XPath("/CC015C/Consignment/referenceNumberUCR")
            val result         = functionalError.copy(errorPointer = errorPointer).invalidDataItem
            val expectedResult = "Reference number UCR"
            result.shouldBe(expectedResult)
        }
      }
    }
  }
}
