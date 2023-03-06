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
import models.Lock._
import play.api.libs.json.{JsValue, Json}

import java.time.Instant

class LockSpec extends SpecBase {

  private val lock = Lock(
    lrn = lrn,
    eoriNumber = eoriNumber,
    createdAt = Instant.ofEpochMilli(1662393524188L),
    lastUpdated = Instant.ofEpochMilli(1662546803472L),
    sessionId = "AB123"
  )

  "Lock" should {

    val json: JsValue = Json.parse(s"""
         |{
         |    "lrn" : "$lrn",
         |    "eoriNumber" : "$eoriNumber",
         |    "createdAt" : {
         |        "$$date" : {
         |            "$$numberLong" : "1662393524188"
         |        }
         |    },
         |    "lastUpdated" : {
         |        "$$date" : {
         |            "$$numberLong" : "1662546803472"
         |        }
         |    },
         |    "sessionId": "AB123"
         |}
         |""".stripMargin)

    "deserialise to Lock" in {
      val result = json.as[Lock]
      result shouldBe lock
    }

    "serialise to json" in {
      val result = Json.toJson(json)
      result shouldBe json
    }
  }
}
