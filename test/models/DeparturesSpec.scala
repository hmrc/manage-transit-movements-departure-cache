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
import play.api.libs.json.{JsValue, Json}

class DeparturesSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must deserialise" in {

    forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
      (departureId1, departureId2, lrn1, lrn2) =>
        val json: JsValue = Json.parse(s"""
             |{
             |  "_links": {
             |    "self": {
             |      "href": "/customs/transits/movements/departures"
             |    }
             |  },
             |  "departures": [
             |    {
             |      "_links": {
             |        "self": {
             |          "href": "/customs/transits/movements/departures/63651574c3447b12"
             |        },
             |        "messages": {
             |          "href": "/customs/transits/movements/departures/63651574c3447b12/messages"
             |        }
             |      },
             |      "id": "$departureId1",
             |      "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
             |      "localReferenceNumber": "$lrn1",
             |      "created": "2022-11-04T13:36:52.332Z",
             |      "updated": "2022-11-04T13:36:52.332Z",
             |      "enrollmentEORINumber": "9999912345",
             |      "movementEORINumber": "GB1234567890"
             |    },
             |    {
             |      "_links": {
             |        "self": {
             |          "href": "/customs/transits/movements/departures/6365135ba5e821ee"
             |        },
             |        "messages": {
             |          "href": "/customs/transits/movements/departures/6365135ba5e821ee/messages"
             |        }
             |      },
             |      "id": "$departureId2",
             |      "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
             |      "localReferenceNumber": "$lrn2",
             |      "created": "2022-11-04T13:27:55.522Z",
             |      "updated": "2022-11-04T13:27:55.522Z",
             |      "enrollmentEORINumber": "9999912345",
             |      "movementEORINumber": "GB1234567890"
             |    }
             |  ]
             |}
             |""".stripMargin)

        val result = json.validate[Departures].asOpt.value
        result shouldBe Departures(Seq(Departure(departureId1, lrn1), Departure(departureId2, lrn2)))
    }
  }

}
