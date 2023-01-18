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

package api

import Gettables._
import generated._
import models.UserAnswers

object Conversions {

  def transitOperation(userAnswers: UserAnswers): Either[String, TransitOperationType06] = {
    // TODO - bindingItinerary is asked but never stored
    val additionalDeclarationType = "A"
    val bindingItinerary          = true

    for {
      declarationTypePage     <- userAnswers.getAsEither(DeclarationTypeGettable)
      tirCarnetReferencePage  <- userAnswers.getOptional(TIRCarnetReferenceGettable)
      securityDetailsTypePage <- userAnswers.getAsEither(SecurityDetailsTypeGettable)
      reducedDatasetInd       <- userAnswers.getAsEither(ApprovedOperatorGettable)
    } yield TransitOperationType06(
      userAnswers.lrn,
      declarationTypePage,
      additionalDeclarationType,
      tirCarnetReferencePage,
      None, // TODO - presentationOfTheGoodsDateAndTime
      securityDetailsTypePage,
      ApiXmlHelpers.boolToFlag(reducedDatasetInd),
      None, // TODO - specificCircumstanceIndicator
      None, // TODO - communicationLanguageAtDeparture
      ApiXmlHelpers.boolToFlag(bindingItinerary),
      None // TODO - limitDate
    )
  }

}
