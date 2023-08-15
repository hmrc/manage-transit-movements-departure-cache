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

import generated.{CC015C, MESSAGESequence, MessageType015}
import models.UserAnswers
import play.api.libs.json.JsSuccess

import java.time.LocalDateTime
import scala.xml.NamespaceBinding

object Header extends {

  val scope: NamespaceBinding = scalaxb.toScope(Some("ncts") -> "http://ncts.dgtaxud.ec")

  def message(uA: UserAnswers): MESSAGESequence =
    uA.metadata.data.validate((preTaskListPath \ "officeOfDeparture" \ "id").read[String].map(_.take(2))) match {
      case JsSuccess(officeOfDepartureCountryCode, _) =>
        MESSAGESequence(
          messageSender = "NCTS",
          messageRecipient = s"NTA.$officeOfDepartureCountryCode",
          preparationDateAndTime = LocalDateTime.now(),
          messageIdentification = "CC015C", // TODO is this correct due to below
          messageType = CC015C,
          correlationIdentifier = None // TODO what is this
        )
      case _ => throw new Exception("Json did not contain office of departure ID")
    }

  def messageType: MessageType015 = MessageType015.fromString("CC015C", scope)

}
