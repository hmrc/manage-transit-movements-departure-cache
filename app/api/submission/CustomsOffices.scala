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

import generated._
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, JsArray, JsObject, Reads}

object CustomsOffices {

  def transformOfficeOfDeparture(uA: UserAnswers): CustomsOfficeOfDepartureType03 = uA
    .get[JsObject](preTaskListPath)
    .getOrElse(throw new Exception("Json did not contain pre-task-list answers"))
    .as[CustomsOfficeOfDepartureType03](customsOfficeOfDepartureType03.reads)

  def transformOfficeOfDestination(uA: UserAnswers): CustomsOfficeOfDestinationDeclaredType01 = uA
    .get[JsObject](routeDetailsPath \ "routing")
    .getOrElse(throw new Exception("Json did not contain pre-task-list answers"))
    .as[CustomsOfficeOfDestinationDeclaredType01](customsOfficeOfDestinationDeclaredType01.reads)

  def transformOfficeOfTransit(uA: UserAnswers): Seq[CustomsOfficeOfTransitDeclaredType04] = uA
    .get[JsArray](routeDetailsPath \ "transit" \ "officesOfTransit")
    .readValuesAs[CustomsOfficeOfTransitDeclaredType04](customsOfficeOfTransitDeclaredType04.reads)

  def transformOfficeOfExit(uA: UserAnswers): Seq[CustomsOfficeOfExitForTransitDeclaredType02] = uA
    .get[JsArray](routeDetailsPath \ "exit" \ "officesOfExit")
    .readValuesAs[CustomsOfficeOfExitForTransitDeclaredType02](customsOfficeOfExitForTransitDeclaredType02.reads)
}

object customsOfficeOfDepartureType03 {

  implicit val reads: Reads[CustomsOfficeOfDepartureType03] =
    (__ \ "officeOfDeparture" \ "id").read[String].map(CustomsOfficeOfDepartureType03.apply)
}

object customsOfficeOfDestinationDeclaredType01 {

  implicit val reads: Reads[CustomsOfficeOfDestinationDeclaredType01] =
    (__ \ "officeOfDestination" \ "id").read[String].map(CustomsOfficeOfDestinationDeclaredType01.apply)
}

object customsOfficeOfTransitDeclaredType04 {

  def reads(index: Int): Reads[CustomsOfficeOfTransitDeclaredType04] = (
    Reads.pure[BigInt](index) and
      (__ \ "officeOfTransit" \ "id").read[String] and
      (__ \ "arrivalDateTime").readNullable[String].map(stringToXMLGregorianCalendar)
  )(CustomsOfficeOfTransitDeclaredType04.apply)
}

object customsOfficeOfExitForTransitDeclaredType02 {

  def reads(index: Int): Reads[CustomsOfficeOfExitForTransitDeclaredType02] = (
    Reads.pure[BigInt](index) and
      (__ \ "officeOfExit" \ "id").read[String]
  )(CustomsOfficeOfExitForTransitDeclaredType02.apply)
}
