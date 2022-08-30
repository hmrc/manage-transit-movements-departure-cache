/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDateTime
import java.util.UUID

case class UserAnswers(
  lrn: String,
  eoriNumber: String,
  data: JsObject = Json.obj(),
  lastUpdated: LocalDateTime = LocalDateTime.now,
  id: UUID = UUID.randomUUID()
)

object UserAnswers {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[UserAnswers] =
    (
      (__ \ "lrn").read[String] and
        (__ \ "eoriNumber").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.localDateTimeReads) and
        (__ \ "_id").read[UUID]
    )(UserAnswers.apply _)

  implicit lazy val writes: Writes[UserAnswers] =
    (
      (__ \ "lrn").write[String] and
        (__ \ "eoriNumber").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.localDateTimeWrites) and
        (__ \ "_id").write[UUID]
    )(unlift(UserAnswers.unapply))

  implicit lazy val format: Format[UserAnswers] = Format(reads, writes)
}
