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

import generated.{AddressType12, AddressType14, AddressType17, PostcodeAddressType02}
import play.api.libs.functional.syntax._
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

object addressType14 {

  implicit val optionalReads: Reads[Option[AddressType14]] =
    addressType.optionalReads(AddressType14)
}

object addressType17 {

  implicit val optionalReads: Reads[Option[AddressType17]] =
    addressType.optionalReads(AddressType17)
}

object postcodeAddressType02 {

  implicit val reads: Reads[PostcodeAddressType02] = (
    (__ \ "streetNumber").readNullable[String] and
      (__ \ "postalCode").read[String] and
      (__ \ "country" \ "code").read[String]
  )(PostcodeAddressType02.apply _)
}

object addressType12 {

  implicit val optionalReads: Reads[Option[AddressType12]] =
    addressType.optionalReads(AddressType12)

  implicit class RichAddressType12(value: AddressType12) {

    def asAddressType17: AddressType17 = AddressType17(
      streetAndNumber = value.streetAndNumber,
      postcode = value.postcode,
      city = value.city,
      country = value.country
    )
  }
}
