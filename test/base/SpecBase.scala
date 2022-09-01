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

package base

import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.CacheRepository
import repositories.CacheRepository.CacheRepositoryProvider

trait SpecBase extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach with OptionValues with EitherValues {

  val lrn  = "lrn"
  val eori = "eori"

  val emptyUserAnswers: UserAnswers = UserAnswers(lrn, eori)

  val mockCacheRepositoryProvider: CacheRepositoryProvider = mock[CacheRepositoryProvider]
  val mockCacheRepository: CacheRepository                 = mock[CacheRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheRepositoryProvider, mockCacheRepository)
    when(mockCacheRepositoryProvider.apply(any())).thenReturn(mockCacheRepository)
  }

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

}
