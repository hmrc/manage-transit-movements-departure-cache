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

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.UUID

final case class UserAnswers(
  lrn: String,
  eoriNumber: String,
  data: JsObject,
  tasks: Map[String, Status.Value],
  createdAt: Instant,
  lastUpdated: Instant,
  id: UUID
) {

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(path)).reads(data).getOrElse(None)

}

object UserAnswers {

  import play.api.libs.functional.syntax._

  // TODO - this is for backwards compatibility. Can be removed when all frontends using Instant
  lazy val oldReads: Reads[Instant] =
    implicitly[Reads[LocalDateTime]].map(_.toInstant(ZoneOffset.UTC))

  implicit lazy val reads: Reads[UserAnswers]   = customReads(implicitly[Reads[Instant]] orElse oldReads)
  implicit lazy val writes: Writes[UserAnswers] = customWrites(implicitly)

  private def customReads(implicit instantReads: Reads[Instant]): Reads[UserAnswers] = (
    (__ \ "lrn").read[String] and
      (__ \ "eoriNumber").read[String] and
      (__ \ "data").read[JsObject] and
      (__ \ "tasks").read[Map[String, Status.Value]] and
      (__ \ "createdAt").read[Instant] and
      (__ \ "lastUpdated").read[Instant] and
      (__ \ "_id").read[UUID]
  )(UserAnswers.apply _)

  private def customWrites(implicit instantWrites: Writes[Instant]): Writes[UserAnswers] = (
    (__ \ "lrn").write[String] and
      (__ \ "eoriNumber").write[String] and
      (__ \ "data").write[JsObject] and
      (__ \ "tasks").write[Map[String, Status.Value]] and
      (__ \ "createdAt").write[Instant] and
      (__ \ "lastUpdated").write[Instant] and
      (__ \ "_id").write[UUID]
  )(unlift(UserAnswers.unapply))

  lazy val mongoFormat: Format[UserAnswers] = Format(
    customReads(MongoJavatimeFormats.instantReads),
    customWrites(MongoJavatimeFormats.instantWrites)
  )
}
