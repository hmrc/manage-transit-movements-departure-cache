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

import play.api.libs.json.{JsValue, Json, Writes}

sealed trait AuditType {
  val name: String
  val lrn: String
  val eoriNumber: String

  val channel: String = "web"

  def toJson: JsValue = Json.obj(
    "channel" -> channel,
    "detail" -> Json.obj(
      "lrn"        -> lrn,
      "eoriNumber" -> eoriNumber
    )
  )

  override def toString: String = name
}

object AuditType {

  sealed trait SubmissionAuditType extends AuditType {
    val userAnswers: UserAnswers
    override val lrn: String        = userAnswers.lrn
    override val eoriNumber: String = userAnswers.eoriNumber
    val status: Int

    override def toJson: JsValue = Json.obj(
      "channel" -> channel,
      "status"  -> status,
      "detail"  -> Json.toJson(userAnswers)
    )
  }

  case class DepartureDraftStarted(lrn: String, eoriNumber: String) extends AuditType {
    override val name: String = DepartureDraftStarted.name
  }

  object DepartureDraftStarted {
    val name: String = "DepartureDraftStarted"
  }

  case class DepartureDraftDeleted(lrn: String, eoriNumber: String) extends AuditType {
    override val name: String = DepartureDraftDeleted.name
  }

  object DepartureDraftDeleted {
    val name: String = "DepartureDraftDeleted"
  }

  case class DeclarationData(userAnswers: UserAnswers, status: Int) extends SubmissionAuditType {
    override val name: String = DeclarationData.name
  }

  object DeclarationData {
    val name: String = "DeclarationData"
  }

  case class DeclarationAmendment(userAnswers: UserAnswers, status: Int) extends SubmissionAuditType {
    override val name: String = DeclarationAmendment.name
  }

  object DeclarationAmendment {
    val name: String = "DeclarationAmendment"
  }

  implicit val writes: Writes[AuditType] = Writes(_.toJson)
}
