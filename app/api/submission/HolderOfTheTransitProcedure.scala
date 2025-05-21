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
import models.UserAnswers
import play.api.libs.functional.syntax.*
import play.api.libs.json.{__, JsObject, Reads}

object HolderOfTheTransitProcedure {

  def transform(uA: UserAnswers): HolderOfTheTransitProcedureType23 = uA
    .get[JsObject](traderDetailsPath \ "holderOfTransit")
    .getOrElse(throw new Exception("Json did not contain holder of transit answers"))
    .as[HolderOfTheTransitProcedureType23](holderOfTheTransitProcedureType23.reads)
}

object holderOfTheTransitProcedureType23 {

  implicit val reads: Reads[HolderOfTheTransitProcedureType23] = (
    (__ \ "eori").readNullable[String] and
      (__ \ "tirIdentification").readNullable[String] and
      (__ \ "name").readNullable[String] and
      __.read[Option[AddressType14]](addressType14.optionalReads) and
      (__ \ "contact").readNullable[ContactPersonType03](contactPersonType03.reads)
  )(HolderOfTheTransitProcedureType23.apply)
}
