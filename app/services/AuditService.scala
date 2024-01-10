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

package services

import models.{AuditType, UserAnswers}
import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuditService @Inject() (auditConnector: AuditConnector)(implicit ec: ExecutionContext) {

  def audit(auditType: AuditType, lrn: String, eoriNumber: String)(implicit hc: HeaderCarrier): Unit = {
    val json = Json.obj(
      "lrn"        -> lrn,
      "eoriNumber" -> eoriNumber
    )
    auditConnector.sendExplicitAudit(auditType.name, toJson(json))
  }

  def audit(auditType: AuditType, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Unit =
    auditConnector.sendExplicitAudit(auditType.name, toJson(userAnswers))

  private def toJson[T](details: T)(implicit writes: Writes[T]): JsValue = Json.obj(
    "channel" -> "web",
    "detail"  -> Json.toJson(details)
  )
}
