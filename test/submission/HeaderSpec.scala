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
import generated.{CORRELATION_IDENTIFIERSequence, MESSAGE_1Sequence, MESSAGE_FROM_TRADERSequence}

class HeaderSpec extends SpecBase {

  "Conversions" when {

    "message is called" when {

      "will convert to API format" in {

        val converted = Header.message

        val expected = MESSAGE_FROM_TRADERSequence(
          messageSender = Some("NCTS"),
          messagE_1Sequence2 = MESSAGE_1Sequence(
            messageRecipient = "NCTS",
            preparationDateAndTime = converted.messagE_1Sequence2.preparationDateAndTime,
            messageIdentification = "CC015C"
          )
        )

        converted shouldBe expected

      }

    }

    "messageType is called" when {

      "will convert to API format" in {

        Header.messageType.toString shouldBe "CC015C"

      }

    }

    "correlationIdentifier is called" when {

      "will convert to API format" in {

        Header.correlationIdentifier shouldBe CORRELATION_IDENTIFIERSequence(None)

      }

    }

  }
}
