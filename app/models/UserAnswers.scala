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

import play.api.libs.json.*
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

  import play.api.libs.functional.syntax.*

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

  // TODO - 30 days after deployment of CTCP-6442, this logic can be removed
  private def updateMetadata(metadata: Metadata): Metadata = {
    def update(path: String): Reads[JsObject] = for {
      pick <- (__ \ path).readNullable[JsValue]
      update <- pick match {
        case Some(arr: JsArray) =>
          __.json.update(
            (__ \ path).json.prune andThen
              (__ \ path \ path).json.put(arr)
          )
        case _ =>
          __.json.pick[JsObject]
      }
    } yield update

    metadata.data
      .transform(update("items") andThen update("guaranteeDetails"))
      .map {
        data =>
          metadata.copy(
            data = data,
            tasks = metadata.tasks.removed(".addAnotherItem").removed(".addAnotherGuarantee")
          )
      }
      .getOrElse(metadata)
  }

  private def reads(implicit instantReads: Reads[Instant], metaDataReads: Reads[Metadata]): Reads[UserAnswers] =
    (
      __.read[Metadata].map(updateMetadata) and
        (__ \ "createdAt").read[Instant] and
        (__ \ "lastUpdated").read[Instant] and
        (__ \ "_id").read[UUID] and
        (__ \ "departureId").readNullable[String] and
        (__ \ "isTransitional").readWithDefault[Boolean](true)
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
