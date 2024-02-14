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

import generated._
import models.UserAnswers
import play.api.libs.json.JsSuccess
import services.MessageIdentificationService

import java.time.LocalDateTime
import javax.inject.Inject

class Header @Inject() (messageIdentificationService: MessageIdentificationService) extends {

  def message(uA: UserAnswers, messageType: MessageTypes): MESSAGESequence =
    uA.metadata.data.validate((preTaskListPath \ "officeOfDeparture" \ "id").read[String].map(_.take(2))) match {
      case JsSuccess(officeOfDepartureCountryCode, _) =>
        MESSAGESequence(
          messageSender = uA.eoriNumber,
          messagE_1Sequence2 = MESSAGE_1Sequence(
            messageRecipient = s"NTA.$officeOfDepartureCountryCode",
            preparationDateAndTime = LocalDateTime.now(),
            messageIdentification = messageIdentificationService.randomIdentifier
          ),
          messagE_TYPESequence3 = MESSAGE_TYPESequence(
            messageType = messageType
          ),
          correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
            correlationIdentifier = None
          )
        )
      case _ => throw new Exception("Json did not contain office of departure ID")
    }
}
