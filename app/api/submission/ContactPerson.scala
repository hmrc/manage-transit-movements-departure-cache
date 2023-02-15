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

import generated.{ContactPersonType05, ContactPersonType06}
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

object contactPersonType05 {

  def apply(
    name: String,
    phoneNumber: String
  ): ContactPersonType05 =
    ContactPersonType05(
      name = name,
      phoneNumber = phoneNumber,
      eMailAddress = None
    )

  implicit val reads: Reads[ContactPersonType05] = (
    (__ \ "name").read[String] and
      (__ \ "telephoneNumber").read[String]
  )(contactPersonType05.apply _)

  implicit val optionalReads: Reads[Option[ContactPersonType05]] = (
    (__ \ "name").readNullable[String] and
      (__ \ "telephoneNumber").readNullable[String]
  ).tupled.map {
    case (Some(name), Some(phoneNumber)) => Some(ContactPersonType05(name, phoneNumber))
    case _                               => None
  }
}

object contactPersonType06 {

  def apply(
    name: String,
    phoneNumber: String
  ): ContactPersonType06 =
    ContactPersonType06(
      name = name,
      phoneNumber = phoneNumber,
      eMailAddress = None
    )

  implicit val reads: Reads[ContactPersonType06] = (
    (__ \ "name").read[String] and
      (__ \ "telephoneNumber").read[String]
  )(contactPersonType06.apply _)
}
