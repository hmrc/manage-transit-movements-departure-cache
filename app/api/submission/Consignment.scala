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

import generated._
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

object Consignment {

  def transform(uA: UserAnswers): ConsignmentType20 =
    uA.data.as[ConsignmentType20](consignmentType20.reads)
}

object consignmentType20 {

  implicit val reads: Reads[ConsignmentType20] = (
    (preRequisitesPath \ "countryOfDispatch" \ "code").readNullable[String] and
      (routeDetailsPath \ "routing" \ "countryOfDestination" \ "code").readNullable[String] and
      (preRequisitesPath \ "containerIndicator").readNullable[Boolean] and
      (transportDetailsPath \ "inlandMode").readNullable[String] and
      (transportDetailsPath \ "borderModeOfTransport").readNullable[String] and
      (preRequisitesPath \ "uniqueConsignmentReference").readNullable[String] and
      (transportDetailsPath \ "carrierDetails").readNullable[CarrierType04](carrierType04.reads) and
      (consignmentPath \ "consignor").readNullable[ConsignorType07](consignorType07.reads) and
      (consignmentPath \ "consignee").readNullable[ConsigneeType05](consigneeType05.reads) and
      (transportDetailsPath \ "supplyChainActors").readArray[AdditionalSupplyChainActorType](additionalSupplyChainActorType.reads) and
      (routeDetailsPath \ "loading").readNullable[PlaceOfLoadingType03](placeOfLoadingType03.reads) and
      (routeDetailsPath \ "unloading").readNullable[PlaceOfUnloadingType01](placeOfUnloadingType01.reads)
  ).apply {
    (
      countryOfDispatch,
      countryOfDestination,
      containerIndicator,
      inlandModeOfTransport,
      modeOfTransportAtTheBorder,
      referenceNumberUCR,
      Carrier,
      Consignor,
      Consignee,
      AdditionalSupplyChainActor,
      PlaceOfLoading,
      PlaceOfUnloading
    ) =>
      ConsignmentType20(
        countryOfDispatch = countryOfDispatch,
        countryOfDestination = countryOfDestination,
        containerIndicator = containerIndicator,
        inlandModeOfTransport = convertModeOfTransport(inlandModeOfTransport),
        modeOfTransportAtTheBorder = convertModeOfTransport(modeOfTransportAtTheBorder),
        grossMass = 0, // TODO
        referenceNumberUCR = referenceNumberUCR,
        Carrier = Carrier,
        Consignor = Consignor,
        Consignee = Consignee,
        AdditionalSupplyChainActor = AdditionalSupplyChainActor,
        TransportEquipment = Nil, // TODO
        LocationOfGoods = None, // TODO
        DepartureTransportMeans = Nil, // TODO
        CountryOfRoutingOfConsignment = Nil, // TODO
        ActiveBorderTransportMeans = Nil, // TODO
        PlaceOfLoading = PlaceOfLoading,
        PlaceOfUnloading = PlaceOfUnloading,
        PreviousDocument = Nil, // TODO
        SupportingDocument = Nil, // TODO
        TransportDocument = Nil, // TODO
        AdditionalReference = Nil, // TODO
        AdditionalInformation = Nil, // TODO
        TransportCharges = None, // TODO
        HouseConsignment = Nil // TODO
      )
  }

  private val convertModeOfTransport: Option[String] => Option[String] = _ map {
    case "maritime" => "1"
    case "rail"     => "2"
    case "road"     => "3"
    case "air"      => "4"
    case "mail"     => "5"
    case "fixed"    => "7"
    case "waterway" => "8"
    case "unknown"  => "9"
    case _          => throw new Exception("Invalid inland mode of transport value")
  }
}

object carrierType04 {

  implicit val reads: Reads[CarrierType04] = (
    (__ \ "identificationNumber").read[String] and
      (__ \ "contact").readNullable[ContactPersonType05](contactPersonType05.reads)
  )(CarrierType04.apply _)
}

object consignorType07 {

  implicit val reads: Reads[ConsignorType07] = (
    (__ \ "eori").readNullable[String] and
      (__ \ "name").readNullable[String] and
      __.read[Option[AddressType17]](addressType17.optionalReads) and
      (__ \ "contact").readNullable[ContactPersonType05](contactPersonType05.reads)
  )(ConsignorType07.apply _)
}

object consigneeType05 {

  implicit val reads: Reads[ConsigneeType05] = (
    (__ \ "eori").readNullable[String] and
      (__ \ "name").readNullable[String] and
      __.read[Option[AddressType17]](addressType17.optionalReads)
  )(ConsigneeType05.apply _)
}

object additionalSupplyChainActorType {

  def apply(role: String, identificationNumber: String)(
    sequenceNumber: Int
  ): AdditionalSupplyChainActorType =
    AdditionalSupplyChainActorType(sequenceNumber.toString, convertRole(role), identificationNumber)

  def reads(index: Int): Reads[AdditionalSupplyChainActorType] = (
    (__ \ "supplyChainActorType").read[String] and
      (__ \ "identificationNumber").read[String]
  ).tupled.map((additionalSupplyChainActorType.apply _).tupled).map(_(index))

  private val convertRole: String => String = {
    case "consolidator"     => "CS"
    case "freightForwarder" => "FW"
    case "manufacturer"     => "MF"
    case "warehouseKeeper"  => "WH"
    case _                  => throw new Exception("Invalid supply chain actor role value")
  }
}

object transportEquipmentType06 {}

object locationOfGoodsType05 {}

object departureTransportMeansType03 {}

object countryOfRoutingOfConsignmentType01 {}

object activeBorderTransportMeansType02 {}

object placeOfLoadingType03 {

  implicit val reads: Reads[PlaceOfLoadingType03] = (
    (__ \ "unLocode").readNullable[String] and
      (__ \ "additionalInformation" \ "country").readNullable[String] and
      (__ \ "additionalInformation" \ "location").readNullable[String]
  )(PlaceOfLoadingType03.apply _)
}

object placeOfUnloadingType01 {

  implicit val reads: Reads[PlaceOfUnloadingType01] = (
    (__ \ "unLocode").readNullable[String] and
      (__ \ "additionalInformation" \ "country").readNullable[String] and
      (__ \ "additionalInformation" \ "location").readNullable[String]
  )(PlaceOfUnloadingType01.apply _)
}

object previousDocumentType09 {}

object supportingDocumentType05 {}

object transportDocumentType04 {}

object additionalReferenceType06 {}

object additionalInformationType03 {}

object transportChargesType {}

object houseConsignmentType10 {}
