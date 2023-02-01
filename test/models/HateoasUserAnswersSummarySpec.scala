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

package models

import base.SpecBase
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.util.UUID

class HateoasUserAnswersSummarySpec extends SpecBase {

  "apply" must {

    "turn a list of user answers into a HateoasMessageSummary" in {

      val dateNow = LocalDateTime.now()
      val id1     = UUID.randomUUID()
      val id2     = UUID.randomUUID()

      val userAnswers1 = UserAnswers("AB123", eoriNumber, Json.obj(), Status.Draft, Map(), dateNow, dateNow, id1)
      val userAnswers2 = UserAnswers("CD123", eoriNumber, Json.obj(), Status.Draft, Map(), dateNow.minusDays(1), dateNow.minusDays(1), id2)

      val expectedResult =
        Json.obj(
          "eoriNumber" -> eoriNumber,
          "userAnswers" -> Json.arr(
            Json.obj(
              "lrn" -> "AB123",
              "_links" -> Json.obj(
                "self" -> Json.obj("href" -> controllers.routes.CacheController.get("AB123").url)
              ),
              "createdAt"   -> dateNow,
              "lastUpdated" -> dateNow,
              "_id"         -> id1
            ),
            Json.obj(
              "lrn" -> "CD123",
              "_links" -> Json.obj(
                "self" -> Json.obj("href" -> controllers.routes.CacheController.get("CD123").url)
              ),
              "createdAt"   -> dateNow.minusDays(1),
              "lastUpdated" -> dateNow.minusDays(1),
              "_id"         -> id2
            )
          )
        )

      HateoasUserAnswersSummary(eoriNumber, Seq(userAnswers1, userAnswers2)) shouldBe expectedResult

    }
  }
}
