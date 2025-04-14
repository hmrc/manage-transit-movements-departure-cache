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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.AuditType.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.CacheRepository
import services.{AuditService, MetricsService, SessionService}

import scala.concurrent.Future

class SessionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private lazy val mockCacheRepository = mock[CacheRepository]
  private lazy val mockAuditService    = mock[AuditService]
  private lazy val mockMetricsService  = mock[MetricsService]
  private lazy val mockSessionService  = mock[SessionService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[CacheRepository].toInstance(mockCacheRepository),
        bind[SessionService].toInstance(mockSessionService),
        bind[AuditService].toInstance(mockAuditService),
        bind[MetricsService].toInstance(mockMetricsService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheRepository)
    reset(mockAuditService)
    reset(mockMetricsService)
    reset(mockSessionService)
  }

  "delete" should {

    "return 200" when {
      "deletion was successful" in {
        when(mockSessionService.deleteUserAnswersAndLocks(any(), any())).thenReturn(Future.successful(()))

        val request = FakeRequest(DELETE, routes.SessionController.delete(lrn).url)

        val result = route(app, request).value

        status(result) shouldEqual OK

        val auditType = DepartureDraftDeleted(lrn, eoriNumber)

        verify(mockSessionService).deleteUserAnswersAndLocks(eqTo(eoriNumber), eqTo(lrn))
        verify(mockAuditService).audit(eqTo(auditType))(any())
        verify(mockMetricsService).increment(eqTo(auditType))
      }
    }

    "return 500" when {
      "deletion was unsuccessful" in {
        when(mockSessionService.deleteUserAnswersAndLocks(any(), any())).thenReturn(Future.failed(new Throwable()))

        val request = FakeRequest(DELETE, routes.SessionController.delete(lrn).url)

        val result = route(app, request).value

        status(result) shouldEqual INTERNAL_SERVER_ERROR

        verify(mockSessionService).deleteUserAnswersAndLocks(eqTo(eoriNumber), eqTo(lrn))
        verifyNoInteractions(mockAuditService)
        verifyNoInteractions(mockMetricsService)
      }
    }
  }
}
