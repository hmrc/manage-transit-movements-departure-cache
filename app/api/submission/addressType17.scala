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

import generated.AddressType17
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

object addressType17 {

  implicit val optionalReads: Reads[Option[AddressType17]] = (
    (__ \ "address" \ "numberAndStreet").readNullable[String] and
      (__ \ "address" \ "postalCode").readNullable[String] and
      (__ \ "address" \ "city").readNullable[String] and
      (__ \ "country" \ "code").readNullable[String]
  ).tupled.map {
    case (Some(streetAndNumber), postcode, Some(city), Some(country)) =>
      Some(AddressType17(streetAndNumber, postcode, city, country))
    case _ => None
  }
}
