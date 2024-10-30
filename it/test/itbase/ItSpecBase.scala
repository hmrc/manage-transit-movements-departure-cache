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

import controllers.actions._
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier

trait ItSpecBase extends AnyWordSpec with Matchers with ScalaFutures with OptionValues with GuiceOneServerPerSuite with IntegrationPatience {

  val departureId = "departureId123"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def guiceApplicationBuilder(): GuiceApplicationBuilder =
    GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)
      .configure("microservice.services.features.isTransitional" -> true)
      .overrides(bind[AuthenticateActionProvider].toInstance(new FakeAuthenticateActionProvider))
      .overrides(bind[AuthenticateAndLockActionProvider].toInstance(new FakeAuthenticateAndLockActionProvider))

  final override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()
}
