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
import cats.data.NonEmptyList
import models.Rejection.BusinessRejectionType.{AmendmentRejection, DeclarationRejection}
import models.Rejection.{IE055Rejection, IE056Rejection}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, Json}

class RejectionSpec extends SpecBase with ScalaCheckPropertyChecks {

  "Rejection" should {

    "deserialise" when {

      "IE055 rejection" in {
        forAll(Gen.alphaNumStr) {
          departureId =>
            val json = Json.parse(s"""
                 |{
                 |  "departureId" : "$departureId",
                 |  "type" : "IE055"
                 |}
                 |""".stripMargin)

            val result = json.validate[Rejection].get

            result shouldBe IE055Rejection(departureId)
        }
      }

      "IE056 rejection" when {

        "013 business rejection type" in {
          forAll(Gen.alphaNumStr) {
            departureId =>
              val json = Json.parse(s"""
                   |{
                   |  "departureId" : "$departureId",
                   |  "type" : "IE056",
                   |  "businessRejectionType" : "013",
                   |  "errorPointers" : [
                   |    "foo",
                   |    "bar"
                   |  ]
                   |}
                   |""".stripMargin)

              val result = json.validate[Rejection].get

              result shouldBe IE056Rejection(
                departureId,
                AmendmentRejection,
                NonEmptyList.of(
                  XPath("foo"),
                  XPath("bar")
                )
              )
          }
        }

        "015 business rejection type" in {
          forAll(Gen.alphaNumStr) {
            departureId =>
              val json = Json.parse(s"""
                   |{
                   |  "departureId" : "$departureId",
                   |  "type" : "IE056",
                   |  "businessRejectionType" : "015",
                   |  "errorPointers" : [
                   |    "foo",
                   |    "bar"
                   |  ]
                   |}
                   |""".stripMargin)

              val result = json.validate[Rejection].get

              result shouldBe IE056Rejection(
                departureId,
                DeclarationRejection,
                NonEmptyList.of(
                  XPath("foo"),
                  XPath("bar")
                )
              )
          }
        }
      }
    }

    "fail to deserialise" when {
      "rejection type is not recognised" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (departureId, invalidRejectionType) =>
            val json = Json.parse(s"""
                 |{
                 |  "departureId" : "$departureId",
                 |  "type" : "$invalidRejectionType",
                 |  "businessRejectionType" : "015",
                 |  "errorPointers" : [
                 |    "foo",
                 |    "bar"
                 |  ]
                 |}
                 |""".stripMargin)

            val result = json.validate[Rejection]

            result shouldBe a[JsError]
        }
      }

      "business rejection type is not recognised" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (departureId, invalidBusinessRejectionTypeType) =>
            val json = Json.parse(s"""
                 |{
                 |  "departureId" : "$departureId",
                 |  "type" : "IE056",
                 |  "businessRejectionType" : "$invalidBusinessRejectionTypeType",
                 |  "errorPointers" : [
                 |    "foo",
                 |    "bar"
                 |  ]
                 |}
                 |""".stripMargin)

            val result = json.validate[Rejection]

            result shouldBe a[JsError]
        }
      }

      "error pointers is empty" in {
        forAll(Gen.alphaNumStr, Gen.oneOf("013", "015")) {
          (departureId, businessRejectionType) =>
            val json = Json.parse(s"""
                 |{
                 |  "departureId" : "$departureId",
                 |  "type" : "IE056",
                 |  "businessRejectionType" : "$businessRejectionType",
                 |  "errorPointers" : []
                 |}
                 |""".stripMargin)

            val result = json.validate[Rejection]

            result shouldBe a[JsError]
        }
      }
    }
  }
}
