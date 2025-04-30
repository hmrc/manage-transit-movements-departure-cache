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
import play.api.libs.json.Reads

object TransitOperation {

  def transform(uA: UserAnswers): TransitOperationType03 =
    uA.metadata.data.as[TransitOperationType03](transitOperationType03.reads(uA.lrn))

  def transform(uA: UserAnswers, mrn: Option[String], amendmentTypeFlag: Boolean): TransitOperationType02 =
    uA.metadata.data.as[TransitOperationType02](transitOperationType02.reads(uA.lrn, mrn, amendmentTypeFlag))
}

object transitOperationType02 {

  def reads(lrn: String, mrn: Option[String], amendmentTypeFlag: Boolean): Reads[TransitOperationType02] =
    CommonTransitOperation.reads.map(
      readsData =>
        TransitOperationType02(
          LRN = if (mrn.isDefined) None else Some(lrn),
          MRN = mrn,
          declarationType = readsData.declarationType,
          additionalDeclarationType = readsData.additionalDeclarationType,
          TIRCarnetNumber = readsData.TIRCarnetNumber,
          presentationOfTheGoodsDateAndTime = None,
          security = readsData.security,
          reducedDatasetIndicator = readsData.reducedDatasetIndicator,
          specificCircumstanceIndicator = readsData.specificCircumstanceIndicator,
          communicationLanguageAtDeparture = None,
          bindingItinerary = readsData.bindingItinerary,
          amendmentTypeFlag = amendmentTypeFlag,
          limitDate = readsData.limitDate
        )
    )
}

object transitOperationType03 {

  def reads(lrn: String): Reads[TransitOperationType03] =
    CommonTransitOperation.reads.map(
      readsData =>
        TransitOperationType03(
          LRN = lrn,
          declarationType = readsData.declarationType,
          additionalDeclarationType = readsData.additionalDeclarationType,
          TIRCarnetNumber = readsData.TIRCarnetNumber,
          presentationOfTheGoodsDateAndTime = None,
          security = readsData.security,
          reducedDatasetIndicator = readsData.reducedDatasetIndicator,
          specificCircumstanceIndicator = readsData.specificCircumstanceIndicator,
          communicationLanguageAtDeparture = None,
          bindingItinerary = readsData.bindingItinerary,
          limitDate = readsData.limitDate
        )
    )
}
