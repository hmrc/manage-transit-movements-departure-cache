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

package itbase

import controllers.actions.{
  AuthenticateActionProvider,
  AuthenticateAndLockActionProvider,
  FakeAuthenticateActionProvider,
  FakeAuthenticateAndLockActionProvider
}
import models.{Metadata, UserAnswers}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.MongoSupport

import java.time.Instant
import java.util.UUID

trait ItSpecBase extends AnyWordSpec with Matchers with ScalaFutures with OptionValues with GuiceOneServerPerSuite {
  self: MongoSupport =>

  val lrn        = "lrn"
  val eoriNumber = "eori"

  def emptyMetadata: Metadata       = Metadata(lrn, eoriNumber)
  def emptyUserAnswers: UserAnswers = UserAnswers(emptyMetadata, Instant.now(), Instant.now(), UUID.randomUUID())

  val wsClient: WSClient = app.injector.instanceOf[WSClient]
  val baseUrl            = s"http://localhost:$port"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)
      .overrides(bind[MongoComponent].toInstance(mongoComponent))
      .overrides(bind[AuthenticateActionProvider].toInstance(new FakeAuthenticateActionProvider))
      .overrides(bind[AuthenticateAndLockActionProvider].toInstance(new FakeAuthenticateAndLockActionProvider))
      .build()
}
