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

package models

import base.SpecBase
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.JsString

class XPathSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must deserialise" in {
    forAll(Gen.alphaNumStr) {
      xPath =>
        val json   = JsString(xPath)
        val result = json.as[XPath]
        result shouldBe XPath(xPath)
    }
  }

  "isAmendable" must {

    "return true" when {
      "xPath is prepended with /CC015C/TransitOperation" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/TransitOperation" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/Authorisation" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/Authorisation" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/CustomsOfficeOfDeparture" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/CustomsOfficeOfDeparture" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/CustomsOfficeOfDestinationDeclared" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/CustomsOfficeOfDestinationDeclared" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/CustomsOfficeOfTransitDeclared" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/CustomsOfficeOfTransitDeclared" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/CustomsOfficeOfExitForTransitDeclared" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/CustomsOfficeOfExitForTransitDeclared" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/HolderOfTheTransitProcedure" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/HolderOfTheTransitProcedure" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/Representative" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/Representative" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/Guarantee" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/Guarantee" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/Consignment" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/Consignment" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }
    }

    "return false" when {
      "xPath is prepended with CC015C but doesn't have a recognised section" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/" + subPath
            XPath(xPath).isAmendable shouldBe false
        }
      }

      "xPath is anything else" in {
        forAll(Gen.alphaNumStr) {
          xPath =>
            XPath(xPath).isAmendable shouldBe false
        }
      }
    }
  }
}
