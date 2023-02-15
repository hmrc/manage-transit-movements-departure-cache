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

import generated.{AddressType17, ContactPersonType05, HolderOfTheTransitProcedureType14}
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, JsObject, Reads}

object HolderOfTheTransitProcedure {

  def transform(uA: UserAnswers): HolderOfTheTransitProcedureType14 =
    uA
      .get[JsObject](traderDetailsPath \ "holderOfTransit")
      .getOrElse(throw new Exception("User answers did not contain a holder of transit"))
      .as[HolderOfTheTransitProcedureType14](holderOfTheTransitProcedureType14.reads)
}

object holderOfTheTransitProcedureType14 {

  implicit val reads: Reads[HolderOfTheTransitProcedureType14] = (
    (__ \ "eori").readNullable[String] and
      (__ \ "tirIdentification").readNullable[String] and
      (__ \ "name").readNullable[String] and
      __.read[Option[AddressType17]](addressType17.optionalReads) and
      (__ \ "contact").readNullable[ContactPersonType05](contactPersonType05.reads)
  )(HolderOfTheTransitProcedureType14.apply _)
}
