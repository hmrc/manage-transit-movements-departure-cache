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

import config.AppConfig
import generated.{CC013C, CC013CType, CC015C, CC015CType, PhaseIDtype}
import models.SubmissionState.{Amendment, GuaranteeAmendment}
import models.{MovementReferenceNumber, UserAnswers}
import scalaxb.DataRecord
import scalaxb.`package`.toXML

import javax.inject.Inject
import scala.xml.{NamespaceBinding, NodeSeq}

class Declaration @Inject() (header: Header)(implicit config: AppConfig) {

  private val scope: NamespaceBinding = scalaxb.toScope(Some("ncts") -> "http://ncts.dgtaxud.ec")

  def transform(uA: UserAnswers, mrn: MovementReferenceNumber): NodeSeq = uA.status match {
    case Amendment          => toXML(IE013(uA, mrn.value, flag = false), s"ncts:${CC013C.toString}", scope)
    case GuaranteeAmendment => toXML(IE013(uA, mrn.value, flag = true), s"ncts:${CC013C.toString}", scope)
    case _                  => toXML(IE015(uA), s"ncts:${CC015C.toString}", scope)
  }

  private def IE015(uA: UserAnswers)(implicit config: AppConfig): CC015CType =
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
      attributes = Map("@PhaseID" -> DataRecord(PhaseIDtype.fromString("NCTS5.0", scope)))
    )

  private def IE013(uA: UserAnswers, mrn: Option[String], flag: Boolean)(implicit config: AppConfig): CC013CType =
    CC013CType(
      messageSequence1 = header.message(uA, CC013C),
      TransitOperation = TransitOperation.transformIE013(uA, mrn, flag),
      Authorisation = Authorisations.transform(uA),
      CustomsOfficeOfDeparture = CustomsOffices.transformOfficeOfDeparture(uA),
      CustomsOfficeOfDestinationDeclared = CustomsOffices.transformOfficeOfDestination(uA),
      CustomsOfficeOfTransitDeclared = CustomsOffices.transformOfficeOfTransit(uA),
      CustomsOfficeOfExitForTransitDeclared = CustomsOffices.transformOfficeOfExit(uA),
      HolderOfTheTransitProcedure = HolderOfTheTransitProcedure.transform(uA),
      Representative = Representative.transform(uA),
      Guarantee = Guarantee.transformIE013(uA),
      Consignment = Consignment.transform(uA),
      attributes = Map("@PhaseID" -> DataRecord(PhaseIDtype.fromString("NCTS5.0", scope)))
    )
}
