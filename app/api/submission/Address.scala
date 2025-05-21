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

package api.submission

import generated.*
import play.api.libs.functional.syntax.*
import play.api.libs.json.{__, Reads}

object addressType {

  def optionalReads[T](apply: (String, Option[String], String, String) => T): Reads[Option[T]] = (
    (__ \ "address" \ "numberAndStreet").readNullable[String] and
      (__ \ "address" \ "postalCode").readNullable[String] and
      (__ \ "address" \ "city").readNullable[String] and
      (__ \ "country" \ "code").readNullable[String]
  ).tupled.map {
    case (Some(streetAndNumber), postcode, Some(city), Some(country)) =>
      Some(apply(streetAndNumber, postcode, city, country))
    case _ => None
  }
}

object addressType06 {

  implicit val optionalReads: Reads[Option[AddressType06]] =
    addressType.optionalReads(AddressType06.apply)
}

object addressType14 {

  implicit val optionalReads: Reads[Option[AddressType14]] =
    addressType.optionalReads(AddressType14.apply)
}
