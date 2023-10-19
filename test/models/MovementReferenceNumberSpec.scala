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

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsString, JsValue, Json}

class MovementReferenceNumberSpec extends AnyFreeSpec with Generators with Matchers with EitherValues {

  "a Local Reference Number" - {

    "must deserialise" in {

      forAll(arbitrary[MovementReferenceNumber]) {
        mrn =>
          val json: JsValue = Json.parse(s"""
               |{
               |    "movementReferenceNumber" : "${mrn.value.get}"
               |}
               |""".stripMargin)
          json.as[MovementReferenceNumber] mustEqual mrn
      }
    }

    "must deserialise when empty" in {

      val mrn           = MovementReferenceNumber(None)
      val json: JsValue = Json.parse(s"""
               |{
               |}
               |""".stripMargin)
      json.as[MovementReferenceNumber] mustEqual mrn

    }

    "must serialise" in {
      forAll(arbitrary[MovementReferenceNumber]) {
        mrn =>
          val jsonExpected: JsValue = Json.parse(s"""
               |{
               |    "movementReferenceNumber" : "${mrn.value.get}"
               |}
               |""".stripMargin)
          Json.toJson(mrn) mustEqual jsonExpected
      }
    }

    "must serialise when empty" in {
      val mrn                   = MovementReferenceNumber(None)
      val jsonExpected: JsValue = Json.parse(s"""
               |{
               |}
               |""".stripMargin)

      Json.toJson(mrn) mustEqual jsonExpected

    }
  }
}
