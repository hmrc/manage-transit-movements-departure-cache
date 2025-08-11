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

import base.SpecBase
import models.AuditType.{DeclarationData, DepartureDraftStarted}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.verify
import play.api.libs.json.Json
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends SpecBase {

  private val mockAuditConnector = mock[AuditConnector]

  "audit" must {
    "audit event" when {
      "DeclarationData" in {
        val service = new AuditService(mockAuditConnector)

        val userAnswers = emptyUserAnswers
        service.audit(DeclarationData, userAnswers)

        val expectedDetail = Json.parse(s"""
            |{
            |  "channel" : "web",
            |  "detail" : ${Json.toJson(userAnswers)}
            |}
            |""".stripMargin)

        verify(mockAuditConnector).sendExplicitAudit(eqTo(DeclarationData.name), eqTo(expectedDetail))(any(), any(), any())
      }

      "DepartureDraftStarted" in {
        val service = new AuditService(mockAuditConnector)

        val lrn        = "12345"
        val eoriNumber = "67890"
        service.audit(DepartureDraftStarted, lrn, eoriNumber)

        val expectedDetail = Json.parse(s"""
             |{
             |  "channel" : "web",
             |  "detail" : {
             |    "lrn" : "$lrn",
             |    "eoriNumber" : "$eoriNumber"
             |  }
             |}
             |""".stripMargin)

        verify(mockAuditConnector).sendExplicitAudit(eqTo(DepartureDraftStarted.name), eqTo(expectedDetail))(any(), any(), any())
      }
    }
  }

}
