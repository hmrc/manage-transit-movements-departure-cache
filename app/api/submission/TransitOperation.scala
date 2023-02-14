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

import api.ApiXmlHelper
import generated.TransitOperationType06
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

import java.time.LocalDate
import javax.xml.datatype.XMLGregorianCalendar

object TransitOperation {

  def transform(uA: UserAnswers): TransitOperationType06 =
    uA.data.as[TransitOperationType06](TransitOperationType06.reads(uA.lrn))

}

object TransitOperationType06 {

  def apply(
    LRN: String,
    declarationType: String,
    additionalDeclarationType: String,
    TIRCarnetNumber: Option[String] = None,
    presentationOfTheGoodsDateAndTime: Option[javax.xml.datatype.XMLGregorianCalendar] = None,
    security: String,
    reducedDatasetIndicator: generated.Flag,
    specificCircumstanceIndicator: Option[String] = None,
    communicationLanguageAtDeparture: Option[String] = None,
    bindingItinerary: generated.Flag,
    limitDate: Option[XMLGregorianCalendar] = None
  ): TransitOperationType06 =
    new TransitOperationType06(
      LRN,
      declarationType,
      additionalDeclarationType,
      TIRCarnetNumber,
      presentationOfTheGoodsDateAndTime,
      security,
      reducedDatasetIndicator,
      specificCircumstanceIndicator,
      communicationLanguageAtDeparture,
      bindingItinerary,
      limitDate
    )

  // TODO - approved operator may be inferred. Do we need some business logic here?
  def reads(lrn: String): Reads[TransitOperationType06] = (
    (__ \ "lrn").readWithDefault(lrn) and
      (__ \ "preTaskList" \ "declarationType").read[String] and
      (__ \ "preTaskList" \ "additionalDeclarationType").readWithDefault("A") and
      (__ \ "preTaskList" \ "tirCarnetReference").readNullable[String] and
      (__ \ "presentationOfTheGoodsDateAndTime")
        .readNullable[LocalDate]
        .map(
          x =>
            x.map(
              y => ApiXmlHelper.toDate(y.toString)
            )
        ) and
      (__ \ "preTaskList" \ "securityDetailsType").read[String].map {
        case "noSecurity"                     => "0"
        case "entrySummaryDeclaration"        => "1"
        case "exitSummaryDeclaration"         => "2"
        case "entryAndExitSummaryDeclaration" => "3"
      } and
      (__ \ "traderDetails" \ "consignment" \ "approvedOperator").readNullable[Boolean].map {
        case Some(value) => ApiXmlHelper.boolToFlag(value)
        case None        => ApiXmlHelper.boolToFlag(false)
      } and
      (__ \ "specificCircumstanceIndicator").readNullable[String] and
      (__ \ "communicationLanguageAtDeparture").readNullable[String] and
      (__ \ "routeDetails" \ "routing" \ "bindingItinerary").readNullable[Boolean].map {
        case Some(value) => ApiXmlHelper.boolToFlag(value)
        case None        => ApiXmlHelper.boolToFlag(false)
      } and
      (__ \ "transportDetails" \ "authorisationsAndLimit" \ "limit" \ "limitDate")
        .readNullable[LocalDate]
        .map(
          x =>
            x.map(
              y => ApiXmlHelper.toDate(y.toString)
            )
        )
  )(TransitOperationType06.apply _)
}
