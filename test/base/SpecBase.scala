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

import models.UserAnswers
import org.mockito.Mockito.reset
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.CacheRepository

trait SpecBase extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach with OptionValues with EitherValues {

  val lrn        = "lrn"
  val eoriNumber = "eori"

  val emptyUserAnswers: UserAnswers = UserAnswers(lrn, eoriNumber)

  val mockCacheRepository: CacheRepository = mock[CacheRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheRepository)
  }

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

}
