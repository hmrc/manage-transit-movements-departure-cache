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

import play.api.libs.json._
import utils.Format._
import java.time.format.DateTimeParseException
import java.time.{LocalDate, LocalDateTime, LocalTime}

case class DateTime(date: LocalDate, time: LocalTime) {
  def toLocalDateTime: LocalDateTime = LocalDateTime.of(date, time)
}

object DateTime {

  def apply(localDateTime: LocalDateTime): DateTime = {
    val date = localDateTime.toLocalDate
    val time = localDateTime.toLocalTime
    DateTime(date, time)
  }

  implicit val writes: Writes[DateTime] = (x: DateTime) => Json.toJson(x.toLocalDateTime.toIE015Format)

  implicit val reads: Reads[DateTime] = (json: JsValue) => {
    json.validate[String].flatMap {
      x =>
        try JsSuccess(x.parseWithIE015Format.toDateTime)
        catch {
          case exception: DateTimeParseException =>
            JsError(s"Failed to parse $json to LocalDateTime with the following exception: $exception")
        }
    }
  }
}
