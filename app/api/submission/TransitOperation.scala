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

import generated.{TransitOperationType04, TransitOperationType06}
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads

import java.time.LocalDate

object TransitOperation {

  def transform(uA: UserAnswers): TransitOperationType06 =
    uA.metadata.data.as[TransitOperationType06](transitOperationType06.reads(uA.lrn))

  def transformIE013(uA: UserAnswers, mrn: Option[String], flag: Boolean): TransitOperationType04 =
    uA.metadata.data.as[TransitOperationType04](transitOperationType04.reads(uA.lrn, mrn, flag))
}

object transitOperationType04 {

  def reads(lrn: String, mrn: Option[String], flag: Boolean): Reads[TransitOperationType04] = (
    (preTaskListPath \ "declarationType" \ "code").read[String] and
      (preTaskListPath \ "additionalDeclarationType" \ "code").read[String] and
      (preTaskListPath \ "tirCarnetReference").readNullable[String] and
      (preTaskListPath \ "securityDetailsType" \ "code").read[String] and
      reducedDatasetIndicatorReads and
      (routeDetailsPath \ "specificCircumstanceIndicator" \ "code").readNullable[String] and
      (routeDetailsPath \ "routing" \ "bindingItinerary").readWithDefault[Boolean](false) and
      (transportDetailsPath \ "authorisationsAndLimit" \ "limit" \ "limitDate").readNullable[LocalDate]
  ).apply {
    (declarationType,
     additionalDeclarationType,
     TIRCarnetNumber,
     security,
     reducedDatasetIndicator,
     specificCircumstanceIndicator,
     bindingItinerary,
     limitDate
    ) =>
      TransitOperationType04(
        LRN = if (mrn.isDefined) None else Some(lrn),
        MRN = mrn,
        declarationType = declarationType,
        additionalDeclarationType = additionalDeclarationType,
        TIRCarnetNumber = TIRCarnetNumber,
        presentationOfTheGoodsDateAndTime = None, // TODO - do we collect this?
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator,
        specificCircumstanceIndicator = specificCircumstanceIndicator,
        communicationLanguageAtDeparture = None, // TODO - do we collect this?
        bindingItinerary = bindingItinerary,
        amendmentTypeFlag = flag,
        limitDate = limitDate
      )
  }
}

object transitOperationType06 {

  def reads(lrn: String): Reads[TransitOperationType06] = (
    (preTaskListPath \ "declarationType" \ "code").read[String] and
      (preTaskListPath \ "additionalDeclarationType" \ "code").read[String] and
      (preTaskListPath \ "tirCarnetReference").readNullable[String] and
      (preTaskListPath \ "securityDetailsType" \ "code").read[String] and
      reducedDatasetIndicatorReads and
      (routeDetailsPath \ "specificCircumstanceIndicator" \ "code").readNullable[String] and
      (routeDetailsPath \ "routing" \ "bindingItinerary").readWithDefault[Boolean](false) and
      (transportDetailsPath \ "authorisationsAndLimit" \ "limit" \ "limitDate").readNullable[LocalDate]
  ).apply {
    (declarationType,
     additionalDeclarationType,
     TIRCarnetNumber,
     security,
     reducedDatasetIndicator,
     specificCircumstanceIndicator,
     bindingItinerary,
     limitDate
    ) =>
      TransitOperationType06(
        LRN = lrn,
        declarationType = declarationType,
        additionalDeclarationType = additionalDeclarationType,
        TIRCarnetNumber = TIRCarnetNumber,
        presentationOfTheGoodsDateAndTime = None, // TODO - do we collect this?
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator,
        specificCircumstanceIndicator = specificCircumstanceIndicator,
        communicationLanguageAtDeparture = None, // TODO - do we collect this?
        bindingItinerary = bindingItinerary,
        limitDate = limitDate
      )
  }
}
