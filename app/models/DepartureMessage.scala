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

import play.api.http.Status.OK
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import java.time.Instant

case class DepartureMessage(id: String, `type`: String, received: Instant)

object DepartureMessage {

  implicit val reads: Reads[DepartureMessage] = (
    (__ \ "id").read[String] and
      (__ \ "type").read[String] and
      (__ \ "received").read[Instant]
  )(DepartureMessage.apply _)
}

sealed trait Message {
  val body: Body
}

object Message {

  implicit def httpReads[T <: Message](implicit reads: Reads[T]): HttpReads[T] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "body")
            .validate[T]
            .getOrElse(
              throw new IllegalStateException("[Message][httpReads] Message could not be parsed")
            )
        case e =>
          throw new IllegalStateException(s"[Message][httpReads] Error: $e")
      }
    }
}

case class IE056Message(body: IE056Body) extends Message

object IE056Message {

  implicit val reads: Reads[IE056Message] =
    (__ \ "n1:CC056C").read[IE056Body].map(IE056Message(_))
}

sealed trait Body

case class IE056Body(functionalErrors: Seq[FunctionalError]) extends Body

object IE056Body {

  implicit val reads: Reads[IE056Body] =
    (__ \ "FunctionalError").readWithDefault[Seq[FunctionalError]](Nil).map(IE056Body(_))
}
