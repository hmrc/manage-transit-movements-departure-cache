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

package utils

import models.DateTime

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

object Format {

  private val dateTimeFormatIE015: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH:mm:ss")

  implicit class RichLocalDate(localDate: LocalDate) {
    def formatAsString: String = localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
    def formatForText: String  = localDate.format(DateTimeFormatter.ofPattern("dd MM yyyy"))
  }

  implicit class RichLocalDateTime(localDateTime: LocalDateTime) {
    def toIE015Format: String  = localDateTime.format(dateTimeFormatIE015)
    def toDateTime: DateTime   = DateTime(localDateTime)
    def formatAsString: String = localDateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm"))
  }

  implicit class RichDateTime(dateTime: DateTime) {
    def formatAsString: String = dateTime.toLocalDateTime.formatAsString
  }

  implicit class RichString(string: String) {
    def parseWithIE015Format: LocalDateTime = LocalDateTime.parse(string, dateTimeFormatIE015)
  }
}
