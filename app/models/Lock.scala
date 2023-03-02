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

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

final case class Lock(
  sessionId: String,
  eoriNumber: String,
  lrn: String,
  createdAt: Instant,
  lastUpdated: Instant
)

object Lock {

  import play.api.libs.functional.syntax._

  implicit val reads: Reads[Lock] =
    (
      (__ \ "sessionId").read[String] and
        (__ \ "eoriNumber").read[String] and
        (__ \ "lrn").read[String] and
        (__ \ "createdAt").read(MongoJavatimeFormats.instantReads) and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantReads)
    )(Lock.apply _)

  implicit val writes: Writes[Lock] =
    (
      (__ \ "sessionId").write[String] and
        (__ \ "eoriNumber").write[String] and
        (__ \ "lrn").write[String] and
        (__ \ "createdAt").write(MongoJavatimeFormats.instantWrites) and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantWrites)
    )(unlift(Lock.unapply))

  implicit lazy val format: Format[Lock] = Format(reads, writes)
}
