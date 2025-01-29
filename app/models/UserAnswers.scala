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
  id: UUID,
  departureId: Option[String] = None,
  isTransitional: Boolean = false
) {

  val lrn: String        = metadata.lrn
  val eoriNumber: String = metadata.eoriNumber

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(path)).reads(metadata.data).getOrElse(None)

  def updateTasks(tasks: Map[String, Status.Value]): UserAnswers =
    this.copy(metadata = metadata.updateTasks(tasks))

  def updateStatus(status: SubmissionState): UserAnswers =
    this.copy(metadata = metadata.updateStatus(status))

  def updateDepartureId(departureId: String): UserAnswers =
    this.copy(departureId = Some(departureId))

  def updateLrn(lrn: String): UserAnswers =
    this.copy(metadata = metadata.updateLrn(lrn))
}

object UserAnswers {

  import play.api.libs.functional.syntax._

  implicit val nonSensitiveFormat: Format[UserAnswers] =
    Format(
      reads(implicitly, implicitly),
      writes(implicitly, implicitly)
    )

  def sensitiveFormat(implicit sensitiveFormats: SensitiveFormats): Format[UserAnswers] =
    Format(
      reads(MongoJavatimeFormats.instantReads, Metadata.sensitiveReads),
      writes(MongoJavatimeFormats.instantWrites, Metadata.sensitiveWrites)
    )

  private def reads(implicit instantReads: Reads[Instant], metaDataReads: Reads[Metadata]): Reads[UserAnswers] =
    (
      __.read[Metadata] and
        (__ \ "createdAt").read[Instant] and
        (__ \ "lastUpdated").read[Instant] and
        (__ \ "_id").read[UUID] and
        (__ \ "departureId").readNullable[String] and
        (__ \ "isTransitional").readWithDefault[Boolean](false)
    )(UserAnswers.apply)

  private def writes(implicit instantWrites: Writes[Instant], metaDataWrites: Writes[Metadata]): Writes[UserAnswers] =
    (
      __.write[Metadata] and
        (__ \ "createdAt").write[Instant] and
        (__ \ "lastUpdated").write[Instant] and
        (__ \ "_id").write[UUID] and
        (__ \ "departureId").writeNullable[String] and
        (__ \ "isTransitional").write[Boolean]
    )(
      ua => Tuple.fromProductTyped(ua)
    )

}
