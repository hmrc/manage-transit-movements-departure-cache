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

import play.api.libs.json.{JsObject, Json}

import java.time.{Duration, LocalDateTime}

case class UserAnswersSummary(eoriNumber: String, userAnswers: Seq[UserAnswers], ttlInDays: Int, totalMovements: Int) {

  private def expiresInDays(ttlInDays: Int, createdAt: LocalDateTime): Long =
    Duration.between(LocalDateTime.now(), createdAt.plusDays(ttlInDays)).toDays + 1

  def toHateoas(): JsObject =
    Json.obj(
      "eoriNumber"     -> eoriNumber,
      "totalMovements" -> totalMovements,
      "userAnswers" -> userAnswers.map {
        userAnswer =>
          Json.obj(
            "lrn" -> userAnswer.lrn,
            "_links" -> Json.obj(
              "self" -> Json.obj("href" -> controllers.routes.CacheController.get(userAnswer.lrn).url)
            ),
            "createdAt"     -> userAnswer.createdAt,
            "lastUpdated"   -> userAnswer.lastUpdated,
            "expiresInDays" -> expiresInDays(ttlInDays, userAnswer.createdAt),
            "_id"           -> userAnswer.id
          )
      }
    )

}
