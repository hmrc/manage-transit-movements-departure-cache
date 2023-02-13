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
import gettables.{
  ApprovedOperatorGettable,
  BindingItineraryGettable,
  DeclarationTypeGettable,
  LimitDateGettable,
  SecurityDetailsTypeGettable,
  TIRCarnetReferenceGettable
}
import models.UserAnswers

object TransitOperation {

  def transform(uA: UserAnswers): TransitOperationType06 =
    TransitOperationType06(
      LRN = uA.lrn,
      declarationType = uA.get(DeclarationTypeGettable).getOrElse(throw new IllegalArgumentException("declarationType is required")),
      additionalDeclarationType = "A",
      TIRCarnetNumber = uA.get(TIRCarnetReferenceGettable),
      presentationOfTheGoodsDateAndTime = None, // TODO - what is this? Needed?
      security = uA.get(SecurityDetailsTypeGettable).getOrElse(throw new IllegalArgumentException("security is required")),
      reducedDatasetIndicator = ApiXmlHelper.boolToFlag(uA.get(ApprovedOperatorGettable).getOrElse(false)),
      specificCircumstanceIndicator = None, // TODO - what is this? Needed?
      communicationLanguageAtDeparture = None, // TODO - what is this? Needed?
      bindingItinerary = ApiXmlHelper.boolToFlag(uA.get(BindingItineraryGettable).getOrElse(false)),
      limitDate = uA
        .get(LimitDateGettable)
        .map(
          x => ApiXmlHelper.toDate(x.toString)
        )
    )
}
