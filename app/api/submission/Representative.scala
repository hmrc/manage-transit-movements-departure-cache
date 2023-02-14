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

import generated.{ContactPersonType05, RepresentativeType05}
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, JsObject, Reads}

object Representative {

  def transform(uA: UserAnswers): Option[RepresentativeType05] =
    uA
      .get[JsObject](traderDetailsPath \ "representative")
      .readValueAs[RepresentativeType05](RepresentativeType05.reads)

}

object RepresentativeType05 {

  implicit val reads: Reads[RepresentativeType05] = (
    (__ \ "eori").read[String] and
      __.read[Option[ContactPersonType05]](ContactPersonType05.reads)
  ).apply {
    (identificationNumber, ContactPerson) =>
      new RepresentativeType05(
        identificationNumber = identificationNumber,
        status = "2",
        ContactPerson = ContactPerson
      )
  }
}

object ContactPersonType05 {

  implicit val reads: Reads[Option[ContactPersonType05]] = (
    (__ \ "name").readNullable[String] and
      (__ \ "telephoneNumber").readNullable[String]
  ).tupled.map {
    case (Some(name), Some(phoneNumber)) =>
      Some(
        new ContactPersonType05(
          name = name,
          phoneNumber = phoneNumber,
          eMailAddress = None
        )
      )
    case _ => None
  }
}
