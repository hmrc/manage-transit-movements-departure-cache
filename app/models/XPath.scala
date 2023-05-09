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

import models.Task._
import play.api.libs.json.{__, Reads}

import scala.util.Try

case class XPath(value: String) {

  private val sections = Seq(
    "TransitOperation",
    "Authorisation",
    "CustomsOfficeOfDeparture",
    "CustomsOfficeOfDestinationDeclared",
    "CustomsOfficeOfTransitDeclared",
    "CustomsOfficeOfExitForTransitDeclared",
    "HolderOfTheTransitProcedure",
    "Representative",
    "Guarantee",
    "Consignment"
  )

  def isAmendable: Boolean = {
    val regex = "^/CC015C/(.+)$".r
    value match {
      case regex(section) if sections.exists(section.startsWith) => true
      case _                                                     => false
    }
  }

  def taskError: Option[(String, Status.Value)] =
    this.task match {
      case Some(task) => Some((task.taskName, Status(Status.Error.id)))
      case _          => None
    }

  def task: Option[Task] = {
    val pf: PartialFunction[String, Task] = _.replace("/CC015C/", "") match {
      case x if x.matches("^TransitOperation/declarationType$")                                                            => PreTaskList
      case x if x.matches("^TransitOperation/TIRCarnetNumber")                                                             => PreTaskList
      case x if x.matches("^TransitOperation/security")                                                                    => PreTaskList
      case x if x.matches("^TransitOperation/bindingItinerary$")                                                           => RouteDetails
      case x if x.matches("^TransitOperation/reducedDatasetIndicator$")                                                    => TraderDetails
      case x if x.matches("^TransitOperation/limitDate$")                                                                  => TransportDetails
      case x if x.matches("^Authorisation(.+)$")                                                                           => TransportDetails
      case x if x.matches("^CustomsOfficeOfDeparture(.+)$")                                                                => PreTaskList
      case x if x.matches("^CustomsOfficeOfDestinationDeclared(.+)$")                                                      => RouteDetails
      case x if x.matches("^CustomsOfficeOfTransitDeclared(.+)$")                                                          => RouteDetails
      case x if x.matches("^CustomsOfficeOfExitForTransitDeclared(.+)$")                                                   => RouteDetails
      case x if x.matches("^HolderOfTheTransitProcedure(.+)$")                                                             => TraderDetails
      case x if x.matches("^Representative(.+)$")                                                                          => TraderDetails
      case x if x.matches("^Guarantee(.+)$")                                                                               => GuaranteeDetails
      case x if x.matches("^Consignment/Carrier(.+)$")                                                                     => TransportDetails
      case x if x.matches("^Consignment/AdditionalSupplyChainActor(.+)$")                                                  => TransportDetails
      case x if x.matches("^Consignment/DepartureTransportMeans(.+)$")                                                     => TransportDetails
      case x if x.matches("^Consignment/Consignor(.+)$")                                                                   => TraderDetails
      case x if x.matches("^Consignment/Consignee(.+)$")                                                                   => TraderDetails
      case x if x.matches("^Consignment/Consignee(.+)$")                                                                   => TraderDetails
      case x if x.matches("^Consignment/LocationOfGoods(.+)$")                                                             => RouteDetails
      case x if x.matches("^Consignment/TransportEquipment(.+)$")                                                          => TransportDetails
      case x if x.matches("^Consignment/ActiveBorderTransportMeans(.+)$")                                                  => TransportDetails
      case x if x.matches("^Consignment/PlaceOfLoading(.+)$")                                                              => RouteDetails
      case x if x.matches("^Consignment/PlaceOfUnloading(.+)$")                                                            => RouteDetails
      case x if x.matches("^Consignment/PreviousDocument(.+)$")                                                            => Documents
      case x if x.matches("^Consignment/SupportingDocument(.+)$")                                                          => Documents
      case x if x.matches("^Consignment/TransportDocument(.+)$")                                                           => Documents
      case x if x.matches("^Consignment/HouseConsignment(\\[[\\d]*])/ConsingmentItem(\\[[\\d]*])/PreviousDocument(.+)$")   => Documents
      case x if x.matches("^Consignment/HouseConsignment(\\[[\\d]*])/ConsingmentItem(\\[[\\d]*])/SupportingDocument(.+)$") => Documents
      case x if x.matches("^Consignment/HouseConsignment(\\[[\\d]*])/ConsingmentItem(\\[[\\d]*])/TransportDocument(.+)$")  => Documents
      case x if x.matches("^Consignment/HouseConsignment(\\[[\\d]*])/ConsingmentItem(.+)$")                                => Items
      case x if x.matches("^Consignment/countryOfDispatch$")                                                               => TransportDetails
      case x if x.matches("^Consignment/countryOfDestination$")                                                            => RouteDetails
      case x if x.matches("^Consignment/containerIndicator$")                                                              => TransportDetails
      case x if x.matches("^Consignment/inlandModeOfTransport$")                                                           => TransportDetails
      case x if x.matches("^Consignment/modeOfTransportAtTheBorder$")                                                      => TransportDetails
      case x if x.matches("^Consignment/referenceNumberUCR$")                                                              => TransportDetails
      case x if x.matches("^Consignment/CountryOfRoutingOfConsignment(.+)$")                                               => RouteDetails
      case x if x.matches("^Consignment/TransportCharges(.+)$")                                                            => TransportDetails

    }

    Try(pf.apply(value)).toOption
  }

}

object XPath {

  implicit val reads: Reads[XPath] = __.read[String].map(XPath(_))
}
