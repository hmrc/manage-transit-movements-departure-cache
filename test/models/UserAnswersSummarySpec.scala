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
import services.DateTimeService

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.util.UUID

class UserAnswersSummarySpec extends SpecBase {

  private val dateTimeService = app.injector.instanceOf[DateTimeService]

  "toHateoas" must {

    "turn an UserAnswersSummary to hateos json object" in {

      val now = Instant.now(clock)
      val id1 = UUID.randomUUID()
      val id2 = UUID.randomUUID()
      val id3 = UUID.randomUUID()

      val userAnswers1 = UserAnswers(Metadata("AB123", eoriNumber), now, now, id1, SubmissionState.NotSubmitted, isTransitional = false)
      val userAnswers2 = UserAnswers(Metadata("CD123", eoriNumber), now.minus(1, DAYS), now.minus(1, DAYS), id2, SubmissionState.Submitted)
      val userAnswers3 = UserAnswers(Metadata("EF123", eoriNumber), now, now, id3, SubmissionState.NotSubmitted)

      val userAnswersSummary = UserAnswersSummary(eoriNumber, Seq(userAnswers1, userAnswers2, userAnswers3), 3, 3)

      val expectedResult =
        Json.obj(
          "eoriNumber"             -> eoriNumber,
          "totalMovements"         -> 3,
          "totalMatchingMovements" -> 3,
          "userAnswers" -> Json.arr(
            Json.obj(
              "lrn" -> "AB123",
              "_links" -> Json.obj(
                "self" -> Json.obj("href" -> controllers.routes.CacheController.get("AB123").url)
              ),
              "createdAt"      -> now,
              "lastUpdated"    -> now,
              "expiresInDays"  -> 30,
              "_id"            -> id1,
              "isSubmitted"    -> "notSubmitted",
              "isTransitional" -> false
            ),
            Json.obj(
              "lrn" -> "CD123",
              "_links" -> Json.obj(
                "self" -> Json.obj("href" -> controllers.routes.CacheController.get("CD123").url)
              ),
              "createdAt"      -> now.minus(1, DAYS),
              "lastUpdated"    -> now.minus(1, DAYS),
              "expiresInDays"  -> 29,
              "_id"            -> id2,
              "isSubmitted"    -> "submitted",
              "isTransitional" -> true
            ),
            Json.obj(
              "lrn" -> "EF123",
              "_links" -> Json.obj(
                "self" -> Json.obj("href" -> controllers.routes.CacheController.get("EF123").url)
              ),
              "createdAt"      -> now,
              "lastUpdated"    -> now,
              "expiresInDays"  -> 30,
              "_id"            -> id3,
              "isSubmitted"    -> "notSubmitted",
              "isTransitional" -> true
            )
          )
        )

      userAnswersSummary.toHateoas(dateTimeService.expiresInDays) shouldBe expectedResult
    }
  }
}
