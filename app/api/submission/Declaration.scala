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
import models.SubmissionState.{Amendment, GuaranteeAmendment}
import models.{MovementReferenceNumber, UserAnswers, Version}
import scalaxb.DataRecord
import scalaxb.`package`.toXML

import javax.inject.Inject
import scala.xml.{NamespaceBinding, NodeSeq}

class Declaration @Inject() (header: Header) {

  private val scope: NamespaceBinding = scalaxb.toScope(Some("ncts") -> "http://ncts.dgtaxud.ec")

  def transform(uA: UserAnswers, mrn: MovementReferenceNumber, version: Version): NodeSeq =
    uA.metadata.isSubmitted match {
      case Amendment          => toXML(IE013(uA, mrn.value, amendmentTypeFlag = false, version), s"ncts:$CC013C", scope)
      case GuaranteeAmendment => toXML(IE013(uA, mrn.value, amendmentTypeFlag = true, version), s"ncts:$CC013C", scope)
      case _                  => toXML(IE015(uA, version), s"ncts:${CC015C.toString}", scope)
    }

  private def IE015(uA: UserAnswers, version: Version): CC015CType =
    CC015CType(
      messageSequence1 = header.message(uA, CC015C),
      TransitOperation = TransitOperation.transform(uA),
      Authorisation = Authorisations.transform(uA),
      CustomsOfficeOfDeparture = CustomsOffices.transformOfficeOfDeparture(uA),
      CustomsOfficeOfDestinationDeclared = CustomsOffices.transformOfficeOfDestination(uA),
      CustomsOfficeOfTransitDeclared = CustomsOffices.transformOfficeOfTransit(uA),
      CustomsOfficeOfExitForTransitDeclared = CustomsOffices.transformOfficeOfExit(uA),
      HolderOfTheTransitProcedure = HolderOfTheTransitProcedure.transform(uA),
      Representative = Representative.transform(uA),
      Guarantee = Guarantee.transform(uA),
      Consignment = Consignment.transform(uA),
      attributes = attributes(version)
    )

  private def IE013(uA: UserAnswers, mrn: Option[String], amendmentTypeFlag: Boolean, version: Version): CC013CType =
    CC013CType(
      messageSequence1 = header.message(uA, CC013C),
      TransitOperation = TransitOperation.transform(uA, mrn, amendmentTypeFlag),
      Authorisation = Authorisations.transform(uA),
      CustomsOfficeOfDeparture = CustomsOffices.transformOfficeOfDeparture(uA),
      CustomsOfficeOfDestinationDeclared = CustomsOffices.transformOfficeOfDestination(uA),
      CustomsOfficeOfTransitDeclared = CustomsOffices.transformOfficeOfTransit(uA),
      CustomsOfficeOfExitForTransitDeclared = CustomsOffices.transformOfficeOfExit(uA),
      HolderOfTheTransitProcedure = HolderOfTheTransitProcedure.transform(uA),
      Representative = Representative.transform(uA),
      Guarantee = Guarantee.transform(uA),
      Consignment = Consignment.transform(uA),
      attributes = attributes(version)
    )

  def attributes(version: Version): Map[String, DataRecord[?]] =
    Map("@PhaseID" -> DataRecord(PhaseIDtype.fromString(version.id.toString, scope)))
}
