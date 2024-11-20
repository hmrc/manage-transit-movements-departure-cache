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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class Metadata(
  lrn: String,
  eoriNumber: String,
  isSubmitted: SubmissionState = SubmissionState.NotSubmitted,
  data: JsObject = Json.obj(),
  tasks: Map[String, Status.Value] = Map()
) {

  def updateTasks(tasks: Map[String, Status.Value]): Metadata =
    this.copy(tasks = tasks)

  def updateStatus(status: SubmissionState): Metadata =
    this.copy(isSubmitted = status)

  def updateLrn(lrn: String): Metadata =
    this.copy(lrn = lrn)
}

object Metadata {

  implicit val nonSensitiveReads: Reads[Metadata] = Json.reads[Metadata]

  implicit val nonSensitiveWrites: Writes[Metadata] = Json.writes[Metadata]

  def sensitiveReads(implicit sensitiveFormats: SensitiveFormats): Reads[Metadata] =
    (
      (__ \ "lrn").read[String] and
        (__ \ "eoriNumber").read[String] and
        (__ \ "isSubmitted").read[SubmissionState] and
        (__ \ "data").read[JsObject](sensitiveFormats.jsObjectReads) and
        (__ \ "tasks").read[Map[String, Status.Value]]
    )(Metadata.apply)

  def sensitiveWrites(implicit sensitiveFormats: SensitiveFormats): Writes[Metadata] =
    (
      (__ \ "lrn").write[String] and
        (__ \ "eoriNumber").write[String] and
        (__ \ "isSubmitted").write[SubmissionState] and
        (__ \ "data").write[JsObject](sensitiveFormats.jsObjectWrites) and
        (__ \ "tasks").write[Map[String, Status.Value]]
    )(
      md => Tuple.fromProductTyped(md)
    )
}
