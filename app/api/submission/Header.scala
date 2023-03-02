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

import generated.{CORRELATION_IDENTIFIERSequence, MESSAGE_1Sequence, MESSAGE_FROM_TRADERSequence, MessageType015}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import scala.xml.NamespaceBinding

object Header extends {

  val scope: NamespaceBinding              = scalaxb.toScope(Some("ncts") -> "http://ncts.dgtaxud.ec")
  val formatterNoMillis: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")

  def message: MESSAGE_FROM_TRADERSequence =
    MESSAGE_FROM_TRADERSequence(
      messageSender = Some("NCTS"),
      messagE_1Sequence2 = MESSAGE_1Sequence(
        messageRecipient = "NCTS",
        preparationDateAndTime = DateTime.now().toString(formatterNoMillis),
        messageIdentification = "CC015C" // TODO - check this with API team? What should this be set to?
      )
    )

  def messageType: MessageType015 = MessageType015.fromString("CC015C", scope)

  // TODO - What should this be?
  def correlationIdentifier = CORRELATION_IDENTIFIERSequence(None)

}
