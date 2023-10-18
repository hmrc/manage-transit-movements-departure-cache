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

import play.api.libs.json.Reads
import play.api.libs.functional.syntax._
import java.time.LocalDate

case class CommonTransitOperation(
  declarationType: String,
  additionalDeclarationType: String,
  TIRCarnetNumber: Option[String],
  security: String,
  reducedDatasetIndicator: Boolean,
  specificCircumstanceIndicator: Option[String],
  bindingItinerary: Boolean,
  limitDate: Option[LocalDate]
)

object CommonTransitOperation {

  val reads: Reads[CommonTransitOperation] = (
    (preTaskListPath \ "declarationType" \ "code").read[String] and
      (preTaskListPath \ "additionalDeclarationType" \ "code").read[String] and
      (preTaskListPath \ "tirCarnetReference").readNullable[String] and
      (preTaskListPath \ "securityDetailsType" \ "code").read[String] and
      reducedDatasetIndicatorReads and
      (routeDetailsPath \ "specificCircumstanceIndicator" \ "code").readNullable[String] and
      (routeDetailsPath \ "routing" \ "bindingItinerary").readWithDefault[Boolean](false) and
      (transportDetailsPath \ "authorisationsAndLimit" \ "limit" \ "limitDate").readNullable[LocalDate]
  )(CommonTransitOperation.apply _)
}
