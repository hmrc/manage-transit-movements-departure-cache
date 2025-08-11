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

import models.{Metadata, SubmissionState, UserAnswers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import java.util.UUID

trait SpecBase extends AnyWordSpec with Matchers with MockitoSugar with OptionValues with EitherValues with ScalaFutures {

  val lrn        = "lrn"
  val eoriNumber = "eori"
  val uuid       = "2e8ede47-dbfb-44ea-a1e3-6c57b1fe6fe2"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val emptyMetadata: Metadata                      = Metadata(lrn, eoriNumber, SubmissionState.NotSubmitted)
  val emptyUserAnswers: UserAnswers                = UserAnswers(emptyMetadata, Instant.now(), Instant.now(), UUID.randomUUID())
  val departureId                                  = "departureId123"
  val emptyUserAnswersWithDepartureId: UserAnswers = emptyUserAnswers.copy(departureId = Some(departureId))

  def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "")

}
