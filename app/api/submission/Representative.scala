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

import config.Constants.RepresentativeStatusCode.DirectRepresentation
import generated.*
import models.UserAnswers
import play.api.libs.functional.syntax.*
import play.api.libs.json.{__, JsObject, Reads}

object Representative {

  def transform(uA: UserAnswers): Option[RepresentativeType06] = uA
    .get[JsObject](traderDetailsPath \ "representative")
    .readValueAs[RepresentativeType06](representativeType06.reads)
}

object representativeType06 {

  implicit val reads: Reads[RepresentativeType06] = (
    (__ \ "eori").read[String] and
      __.read[Option[ContactPersonType03]](contactPersonType03.optionalReads)
  ).apply {
    (identificationNumber, ContactPerson) =>
      RepresentativeType06(
        identificationNumber = identificationNumber,
        status = DirectRepresentation,
        ContactPerson = ContactPerson
      )
  }
}
