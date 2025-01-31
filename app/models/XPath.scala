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

package models

import models.Task.*
import play.api.libs.json.{__, Reads}

import scala.util.Try

case class XPath(value: String) {

  def isAmendable: Boolean = this.task.isDefined

  def taskError: Option[(String, Status.Value)] =
    this.task.map(_.taskError)

  def task: Option[Task] = {
    val traderDetails: PartialFunction[String, Task] = {
      case x if x.matches("^(.*)/TransitOperation/reducedDatasetIndicator$") => TraderDetails
      case x if x.matches("^(.*)/HolderOfTheTransitProcedure(.*)$")          => TraderDetails
      case x if x.matches("^(.*)/Representative(.*)$")                       => TraderDetails
      case x if x.matches("^(.*)/Consignment/Consignor(.*)$")                => TraderDetails
      case x if x.matches("^(.*)/Consignment/Consignee(.*)$")                => TraderDetails
    }

    val routeDetails: PartialFunction[String, Task] = {
      case x if x.matches("^(.*)/TransitOperation/bindingItinerary$")             => RouteDetails
      case x if x.matches("^(.*)/CustomsOfficeOfDestinationDeclared(.*)$")        => RouteDetails
      case x if x.matches("^(.*)/CustomsOfficeOfTransitDeclared(.*)$")            => RouteDetails
      case x if x.matches("^(.*)/CustomsOfficeOfExitForTransitDeclared(.*)$")     => RouteDetails
      case x if x.matches("^(.*)/Consignment/LocationOfGoods(.*)$")               => RouteDetails
      case x if x.matches("^(.*)/Consignment/PlaceOfLoading(.*)$")                => RouteDetails
      case x if x.matches("^(.*)/Consignment/PlaceOfUnloading(.*)$")              => RouteDetails
      case x if x.matches("^(.*)/Consignment/CountryOfRoutingOfConsignment(.*)$") => RouteDetails
    }

    val transportDetails: PartialFunction[String, Task] = {
      case x if x.matches("^(.*)/TransitOperation/limitDate$")                 => TransportDetails
      case x if x.matches("^(.*)/Authorisation(.*)$")                          => TransportDetails
      case x if x.matches("^(.*)/Consignment/Carrier(.*)$")                    => TransportDetails
      case x if x.matches("^(.*)/Consignment/AdditionalSupplyChainActor(.*)$") => TransportDetails
      case x if x.matches("^(.*)/Consignment/DepartureTransportMeans(.*)$")    => TransportDetails
      case x if x.matches("^(.*)/Consignment/TransportEquipment(.*)$")         => TransportDetails
      case x if x.matches("^(.*)/Consignment/ActiveBorderTransportMeans(.*)$") => TransportDetails
      case x if x.matches("^(.*)/Consignment/AdditionalReference(.*)$")        => TransportDetails
      case x if x.matches("^(.*)/Consignment/AdditionalInformation(.*)$")      => TransportDetails
      case x if x.matches("^(.*)/Consignment/countryOfDispatch$")              => TransportDetails
      case x if x.matches("^(.*)/Consignment/countryOfDestination$")           => TransportDetails
      case x if x.matches("^(.*)/Consignment/containerIndicator$")             => TransportDetails
      case x if x.matches("^(.*)/Consignment/inlandModeOfTransport$")          => TransportDetails
      case x if x.matches("^(.*)/Consignment/modeOfTransportAtTheBorder$")     => TransportDetails
      case x if x.matches("^(.*)/Consignment/referenceNumberUCR$")             => TransportDetails
      case x if x.matches("^(.*)/Consignment/TransportCharges(.*)$")           => TransportDetails
    }

    val documents: PartialFunction[String, Task] = {
      case x if x.matches("^(.*)/Consignment/PreviousDocument(.*)$")           => Documents
      case x if x.matches("^(.*)/Consignment/SupportingDocument(.*)$")         => Documents
      case x if x.matches("^(.*)/Consignment/TransportDocument(.*)$")          => Documents
      case x if x.matches("^(.*)/ConsignmentItem(.*)/PreviousDocument(.*)$")   => Documents
      case x if x.matches("^(.*)/ConsignmentItem(.*)/SupportingDocument(.*)$") => Documents
      case x if x.matches("^(.*)/ConsignmentItem(.*)/TransportDocument(.*)$")  => Documents
    }

    val items: PartialFunction[String, Task] = {
      case x if x.matches("^(.*)/GoodsReference(.*)$")  => Items
      case x if x.matches("^(.*)/ConsignmentItem(.*)$") => Items
    }

    val guaranteeDetails: PartialFunction[String, Task] = {
      case x if x.matches("^(.*)/Guarantee(.*)$") => GuaranteeDetails
    }

    val pf: PartialFunction[String, Task] =
      documents orElse
        items orElse
        traderDetails orElse
        routeDetails orElse
        transportDetails orElse
        guaranteeDetails

    Try(pf.apply(value)).toOption
  }

}

object XPath {

  implicit val reads: Reads[XPath] = __.read[String].map(XPath(_))
}
