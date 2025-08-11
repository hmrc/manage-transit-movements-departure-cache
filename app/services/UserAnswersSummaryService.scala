/*
 * Copyright 2025 HM Revenue & Customs
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

package services

import models.UserAnswersSummary
import play.api.libs.json.{JsObject, Json}

import javax.inject.Inject

class UserAnswersSummaryService @Inject() (
  dateTimeService: DateTimeService
) {

  def toHateoas(summary: UserAnswersSummary): JsObject =
    Json.obj(
      "eoriNumber"             -> summary.eoriNumber,
      "totalMovements"         -> summary.totalMovements,
      "totalMatchingMovements" -> summary.totalMatchingMovements,
      "userAnswers" -> summary.userAnswers.map {
        userAnswer =>
          Json.obj(
            "lrn" -> userAnswer.lrn,
            "_links" -> Json.obj(
              "self" -> Json.obj("href" -> controllers.routes.CacheController.get(userAnswer.lrn).url)
            ),
            "createdAt"     -> userAnswer.createdAt,
            "lastUpdated"   -> userAnswer.lastUpdated,
            "expiresInDays" -> dateTimeService.expiresInDays(userAnswer.createdAt),
            "_id"           -> userAnswer.id,
            "isSubmitted"   -> userAnswer.metadata.isSubmitted
          )
      }
    )
}
