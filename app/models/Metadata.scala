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

import models.SubmissionState.NotSubmitted
import play.api.libs.json.{Format, JsObject, Json, Writes}

case class Metadata(
  lrn: String,
  eoriNumber: String,
  data: JsObject,
  tasks: Map[String, Status.Value],
  isSubmitted: Option[SubmissionState] = Some(NotSubmitted),
  resubmittedLrn: Option[String] = None
)

object Metadata {

  def apply(lrn: String, eoriNumber: String): Metadata = Metadata(lrn, eoriNumber, Json.obj(), Map())

  def apply(lrn: String, eoriNumber: String, resubmittedLrn: Option[String]): Metadata =
    Metadata(lrn, eoriNumber, Json.obj(), Map(), Some(NotSubmitted), resubmittedLrn)

  implicit val format: Format[Metadata] = Json.format[Metadata]

  val linkedLrnWrites: Writes[Metadata] = Writes {
    metaData => Json.obj("resubmittedLrn" -> metaData.resubmittedLrn, "isSubmitted" -> metaData.isSubmitted)
  }
}
