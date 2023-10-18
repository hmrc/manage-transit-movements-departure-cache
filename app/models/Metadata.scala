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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

case class Metadata(
  lrn: String,
  eoriNumber: String,
  data: JsObject,
  tasks: Map[String, Status.Value]
) {

  val encryptedData: SensitiveString = SensitiveString(Json.stringify(data))

  def updateTasks(tasks: Map[String, Status.Value]): Metadata =
    this.copy(tasks = tasks)
}

object Metadata {

  def apply(lrn: String, eoriNumber: String): Metadata =
    Metadata(lrn, eoriNumber, Json.obj(), Map())

  implicit val format: Format[Metadata] = Json.format[Metadata]

  def mongoReads(implicit sensitiveStringReads: Reads[SensitiveString]): Reads[Metadata] =
    (
      (__ \ "lrn").read[String] and
        (__ \ "eoriNumber").read[String] and
        (__ \ "data").read[SensitiveString] and
        (__ \ "tasks").read[Map[String, Status.Value]]
    ).apply {
      (lrn, eoriNumber, data, tasks) =>
        println(data.decryptedValue)
        Metadata(lrn, eoriNumber, Json.parse(data.decryptedValue).as[JsObject], tasks)
    }

  def mongoWrites(implicit sensitiveStringWrites: Writes[SensitiveString]): Writes[Metadata] =
    (
      (__ \ "lrn").write[String] and
        (__ \ "eoriNumber").write[String] and
        (__ \ "data").write[SensitiveString] and
        (__ \ "tasks").write[Map[String, Status.Value]]
    ).apply {
      metadata =>
        (
          metadata.lrn,
          metadata.eoriNumber,
          metadata.encryptedData,
          metadata.tasks
        )
    }
}
