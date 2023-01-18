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

package api

import generated.{Number0, Number1}
import models.DateTime
import java.util.GregorianCalendar
import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}

object ApiXmlHelpers {

  // TODO - Need formatters for these dates to match schema regex
  def dateToXMLGregorian(dateTime: DateTime): Option[XMLGregorianCalendar] =
    Some(
      DatatypeFactory
        .newInstance()
        .newXMLGregorianCalendar(
          new GregorianCalendar(
            dateTime.date.getYear,
            dateTime.date.getMonthValue,
            dateTime.date.getDayOfMonth
          )
        )
    )

  def dateTimeToXMLGregorian(dateTime: DateTime): Option[XMLGregorianCalendar] =
    Some(
      DatatypeFactory
        .newInstance()
        .newXMLGregorianCalendar(
          new GregorianCalendar(
            dateTime.date.getYear,
            dateTime.date.getMonthValue,
            dateTime.date.getDayOfMonth,
            dateTime.time.getHour,
            dateTime.time.getMinute,
            dateTime.time.getSecond
          )
        )
    )

  def boolToFlag(x: Boolean) = x match {
    case true => Number1
    case _    => Number0
  }
}
