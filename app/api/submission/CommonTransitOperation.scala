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
