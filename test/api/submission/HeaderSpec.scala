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

package api.submission

import base.SpecBase
import generated._
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

        val result = Header.message(uA)

        result.messageSender shouldBe uA.eoriNumber
        result.messagE_1Sequence2.messageRecipient shouldBe "NTA.GB"
        result.messagE_1Sequence2.messageIdentification shouldBe "CC015C"
        result.messagE_TYPESequence3.messageType shouldBe CC015C
        result.correlatioN_IDENTIFIERSequence4.correlationIdentifier shouldBe None
      }
    }
  }
}
