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

import api.submission.Header
import base.SpecBase
import generated.{CC015C, MESSAGESequence}
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}

class HeaderSpec extends SpecBase {

  "Conversions" when {

    "message is called" when {

      val json: JsValue = Json.parse(s"""
          |{
          |  "_id" : "$uuid",
          |  "lrn" : "$lrn",
          |  "eoriNumber" : "$eoriNumber",
          |  "isSubmitted" : "notSubmitted",
          |  "data" : {
          |    "preTaskList" : {
          |      "officeOfDeparture" : {
          |        "id" : "GB000011",
          |        "name" : "Birmingham Airport",
          |        "phoneNumber" : "+44 (0)121 781 7850"
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

      "will convert to API format" in {

        val converted = Header.message(uA)

        val expected = MESSAGESequence(
          messageSender = "NCTS",
          messageRecipient = "NTA.GB",
          preparationDateAndTime = converted.preparationDateAndTime,
          messageIdentification = "CC015C",
          messageType = CC015C,
          correlationIdentifier = None
        )

        converted shouldBe expected

      }

    }

    "messageType is called" when {

      "will convert to API format" in {

        Header.messageType.toString shouldBe "CC015C"

      }

    }

  }
}
