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

import api.submission.Header.scope
import generated.{CC015CType, PhaseIDtype}
import models.UserAnswers
import scalaxb.DataRecord
import scalaxb.`package`.toXML

import scala.xml.NodeSeq

object Declaration {

  def transform(uA: UserAnswers): CC015CType =
    CC015CType(
      messagE_FROM_TRADERSequence1 = Header.message(uA),
      messageType = Header.messageType,
      correlatioN_IDENTIFIERSequence3 = Header.correlationIdentifier,
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

  def transformToXML(ua: UserAnswers): NodeSeq =
    toXML[CC015CType](transform(ua), "ncts:CC015C", scope)

}
