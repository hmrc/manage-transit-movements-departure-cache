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

import generated.TransitOperationType06
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsSuccess, Reads}

import java.time.LocalDate

object TransitOperation {

  def transform(uA: UserAnswers): TransitOperationType06 = uA.data.validate(TransitOperationType06.reads(uA.lrn)) match {
    case JsSuccess(value, _) => value
    case JsError(errors)     => ???
  }
}

object TransitOperationType06 {

  def reads(lrn: String): Reads[TransitOperationType06] = (
    (preTaskListPath \ "declarationType").read[String] and
      (preTaskListPath \ "tirCarnetReference").readNullable[String] and
      (preTaskListPath \ "securityDetailsType").read[String] and
      (traderDetailsPath \ "consignment" \ "approvedOperator").readWithDefault[Boolean](false) and
      (routeDetailsPath \ "routing" \ "bindingItinerary").readWithDefault[Boolean](false) and
      (transportDetailsPath \ "authorisationsAndLimit" \ "limit" \ "limitDate").readNullable[LocalDate]
  ).apply {
    (declarationType, TIRCarnetNumber, security, reducedDatasetIndicator, bindingItinerary, limitDate) =>
      new TransitOperationType06(
        LRN = lrn,
        declarationType = declarationType,
        additionalDeclarationType = "A",
        TIRCarnetNumber = TIRCarnetNumber,
        presentationOfTheGoodsDateAndTime = None,
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator,
        specificCircumstanceIndicator = None,
        communicationLanguageAtDeparture = None,
        bindingItinerary = bindingItinerary,
        limitDate = limitDate
      )
  }
}
