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
import java.util.UUID

final case class UserAnswers(
  metadata: Metadata,
  createdAt: Instant,
  lastUpdated: Instant,
  id: UUID
) {

  val lrn: String        = metadata.lrn
  val eoriNumber: String = metadata.eoriNumber

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(path)).reads(metadata.data).getOrElse(None)
}

object UserAnswers {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[UserAnswers]   = customReads(implicitly)
  implicit lazy val writes: Writes[UserAnswers] = customWrites(implicitly)

  private def customReads(implicit instantReads: Reads[Instant]): Reads[UserAnswers] = (
    __.read[Metadata] and
      (__ \ "createdAt").read[Instant] and
      (__ \ "lastUpdated").read[Instant] and
      (__ \ "_id").read[UUID]
  )(UserAnswers.apply _)

  private def customWrites(implicit instantWrites: Writes[Instant]): Writes[UserAnswers] = (
    __.write[Metadata] and
      (__ \ "createdAt").write[Instant] and
      (__ \ "lastUpdated").write[Instant] and
      (__ \ "_id").write[UUID]
  )(unlift(UserAnswers.unapply))

  lazy val mongoFormat: Format[UserAnswers] = Format(
    customReads(MongoJavatimeFormats.instantReads),
    customWrites(MongoJavatimeFormats.instantWrites)
  )
}
