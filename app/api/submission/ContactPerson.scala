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
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

object contactPerson {

  val name: String            = "name"
  val telephoneNumber: String = "telephoneNumber"

  def reads[T](apply: (String, String, Option[String]) => T): Reads[T] = (
    (__ \ name).read[String] and
      (__ \ telephoneNumber).read[String] and
      Reads.pure[Option[String]](None)
  )(apply)
}

object contactPersonType03 {
  import contactPerson._

  implicit val reads: Reads[ContactPersonType03] =
    contactPerson.reads(ContactPersonType03.apply)

  implicit val optionalReads: Reads[Option[ContactPersonType03]] = (
    (__ \ name).readNullable[String] and
      (__ \ telephoneNumber).readNullable[String]
  ).tupled.map {
    case (Some(name), Some(phoneNumber)) => Some(ContactPersonType03(name, phoneNumber, None))
    case _                               => None
  }
}

object contactPersonType01 {

  implicit val reads: Reads[ContactPersonType01] =
    contactPerson.reads(ContactPersonType01.apply)
}
