/*
 * Copyright 2022 HM Revenue & Customs
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

import models.UserAnswers
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import repositories.CacheRepository
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

trait ItSpecBase
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with GuiceOneServerPerSuite {

  override protected def repository: CacheRepository = app.injector.instanceOf[CacheRepository]

  val lrn  = "lrn"
  val eori = "eori"

  val emptyUserAnswers: UserAnswers = UserAnswers(lrn, eori)

  val wsClient: WSClient = app.injector.instanceOf[WSClient]
  val baseUrl            = s"http://localhost:$port"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)
      .overrides(bind[MongoComponent].toInstance(mongoComponent))
      .build()
}
