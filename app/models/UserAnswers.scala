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

import config.AppConfig
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import utils.TTLUtils

import java.time.{Clock, Instant}
import java.util.UUID

final case class UserAnswers(
  metadata: Metadata,
  createdAt: Instant,
  lastUpdated: Instant,
  id: UUID,
  status: SubmissionState,
  departureId: Option[String] = None
) {

  val lrn: String        = metadata.lrn
  val eoriNumber: String = metadata.eoriNumber

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(path)).reads(metadata.data).getOrElse(None)

  def expiryInDays(implicit clock: Clock, appConfig: AppConfig): Long =
    TTLUtils.expiresInDays(createdAt)

  def updateTasks(tasks: Map[String, Status.Value]): UserAnswers =
    this.copy(metadata = metadata.updateTasks(tasks))
}

object UserAnswers {

  import play.api.libs.functional.syntax._

  implicit def reads: Reads[UserAnswers]   = customReads(implicitly, implicitly)
  implicit def writes: Writes[UserAnswers] = customWrites(implicitly, implicitly)

  private def customReads(implicit instantReads: Reads[Instant], metaDataReads: Reads[Metadata]): Reads[UserAnswers] =
    (
      __.read[Metadata] and
        (__ \ "createdAt").read[Instant] and
        (__ \ "lastUpdated").read[Instant] and
        (__ \ "_id").read[UUID] and
        (__ \ "isSubmitted").read[SubmissionState] and
        (__ \ "departureId").readNullable[String]
    )(UserAnswers.apply _)

  private def customWrites(implicit instantWrites: Writes[Instant], metaDataWrites: Writes[Metadata]): Writes[UserAnswers] =
    (
      __.write[Metadata] and
        (__ \ "createdAt").write[Instant] and
        (__ \ "lastUpdated").write[Instant] and
        (__ \ "_id").write[UUID] and
        (__ \ "isSubmitted").write[SubmissionState] and
        (__ \ "departureId").writeNullable[String]
    )(unlift(UserAnswers.unapply))

  def mongoFormat(implicit sensitiveFormats: SensitiveFormats): Format[UserAnswers] =
    Format(
      customReads(MongoJavatimeFormats.instantReads, Metadata.sensitiveReads),
      customWrites(MongoJavatimeFormats.instantWrites, Metadata.sensitiveWrites)
    )

}
