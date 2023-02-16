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

package base

import config.AppConfig
import models.UserAnswers
import org.mockito.Mockito.reset
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.{CacheRepository, DefaultLockRepository}
import uk.gov.hmrc.http.HeaderNames

import java.time.{Clock, LocalDateTime}
import java.util.UUID

trait SpecBase extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach with OptionValues with EitherValues with AppWithDefaultMockFixtures {

  val lrn        = "lrn"
  val eoriNumber = "eori"

  val emptyUserAnswers: UserAnswers = UserAnswers(lrn, eoriNumber, Json.obj(), Map(), LocalDateTime.now(), LocalDateTime.now(), UUID.randomUUID())

  val mockCacheRepository: CacheRepository      = mock[CacheRepository]
  val mockLockRepository: DefaultLockRepository = mock[DefaultLockRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheRepository)
    reset(mockLockRepository)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[CacheRepository].toInstance(mockCacheRepository),
        bind[DefaultLockRepository].toInstance(mockLockRepository)
      )

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  val appConfig: AppConfig  = app.injector.instanceOf[AppConfig]
  implicit val clock: Clock = app.injector.instanceOf[Clock]

}
