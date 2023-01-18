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
import queries.Gettable
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDateTime
import java.util.UUID

final case class UserAnswers(
  lrn: String,
  eoriNumber: String,
  data: JsObject = Json.obj(),
  createdAt: LocalDateTime = LocalDateTime.now,
  lastUpdated: LocalDateTime = LocalDateTime.now,
  id: UUID = UUID.randomUUID()
) {

  def getOptional[A](gettable: Gettable[A])(implicit reads: Reads[A]): Either[String, Option[A]] =
    Reads
      .optionNoError(Reads.at(gettable.path))
      .reads(data)
      .asOpt
      .toRight(
        "Something went wrong"
      )

  def getAsEither[A](gettable: Gettable[A])(implicit reads: Reads[A]): Either[String, A] =
    Reads
      .optionNoError(Reads.at(gettable.path))
      .reads(data)
      .getOrElse(None)
      .toRight(
        "Something went wrong"
      )
}

object UserAnswers {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[UserAnswers]   = customReads(implicitly)
  implicit lazy val writes: Writes[UserAnswers] = customWrites(implicitly)

  private def customReads(implicit localDateTimeReads: Reads[LocalDateTime]): Reads[UserAnswers] = (
    (__ \ "lrn").read[String] and
      (__ \ "eoriNumber").read[String] and
      (__ \ "data").read[JsObject] and
      (__ \ "createdAt").read[LocalDateTime] and
      (__ \ "lastUpdated").read[LocalDateTime] and
      (__ \ "_id").read[UUID]
  )(UserAnswers.apply _)

  private def customWrites(implicit localDateTimeWrites: Writes[LocalDateTime]): Writes[UserAnswers] = (
    (__ \ "lrn").write[String] and
      (__ \ "eoriNumber").write[String] and
      (__ \ "data").write[JsObject] and
      (__ \ "createdAt").write[LocalDateTime] and
      (__ \ "lastUpdated").write[LocalDateTime] and
      (__ \ "_id").write[UUID]
  )(unlift(UserAnswers.unapply))

  lazy val mongoFormat: Format[UserAnswers] = Format(
    customReads(MongoJavatimeFormats.localDateTimeReads),
    customWrites(MongoJavatimeFormats.localDateTimeWrites)
  )
}
