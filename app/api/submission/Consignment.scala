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
      inlandModeReads.map(Some(_)).map(convertModeOfTransport) and
      borderModeOfTransportReads.map(convertModeOfTransport) and
      (preRequisitesPath \ "uniqueConsignmentReference").readNullable[String] and
      (transportDetailsPath \ "carrierDetails").readNullable[CarrierType04](carrierType04.reads) and
      (consignmentPath \ "consignor").readNullable[ConsignorType07](consignorType07.reads) and
      (consignmentPath \ "consignee").readNullable[ConsigneeType05](consigneeType05.reads) and
      (transportDetailsPath \ "supplyChainActors").readArray[AdditionalSupplyChainActorType](additionalSupplyChainActorType.reads) and
      equipmentsPath.readArray[TransportEquipmentType06](transportEquipmentType06.reads) and
      (routeDetailsPath \ "locationOfGoods").readNullable[LocationOfGoodsType05](locationOfGoodsType05.reads) and
      (transportDetailsPath \ "transportMeansDeparture").read[DepartureTransportMeansType03](departureTransportMeansType03.reads).map(Seq(_)) and
      (routeDetailsPath \ "routing" \ "countriesOfRouting").readArray[CountryOfRoutingOfConsignmentType01](countryOfRoutingOfConsignmentType01.reads) and
      __.read[Seq[ActiveBorderTransportMeansType02]](activeBorderTransportMeansReads) and
      (routeDetailsPath \ "loading").readNullable[PlaceOfLoadingType03](placeOfLoadingType03.reads) and
      (routeDetailsPath \ "unloading").readNullable[PlaceOfUnloadingType01](placeOfUnloadingType01.reads) and
      (equipmentsAndChargesPath \ "paymentMethod").readNullable[TransportChargesType](transportChargesType.reads) and
      __.read[HouseConsignmentType10](houseConsignmentType10.reads).map(Seq(_))
  ).apply { // TODO - Should be able to change this to `(ConsignmentType20.apply _)` once this is all done
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
      TransportEquipment,
      LocationOfGoods,
      DepartureTransportMeans,
      CountryOfRoutingOfConsignment,
      ActiveBorderTransportMeans,
      PlaceOfLoading,
      PlaceOfUnloading,
      TransportCharges,
      HouseConsignment
    ) =>
      ConsignmentType20(
        countryOfDispatch = countryOfDispatch,
        countryOfDestination = countryOfDestination,
        containerIndicator = containerIndicator,
        inlandModeOfTransport = inlandModeOfTransport,
        modeOfTransportAtTheBorder = modeOfTransportAtTheBorder,
        grossMass = 1d, // TODO
        referenceNumberUCR = referenceNumberUCR,
        Carrier = Carrier,
        Consignor = Consignor,
        Consignee = Consignee,
        AdditionalSupplyChainActor = AdditionalSupplyChainActor,
        TransportEquipment = TransportEquipment,
        LocationOfGoods = LocationOfGoods,
        DepartureTransportMeans = DepartureTransportMeans,
        CountryOfRoutingOfConsignment = CountryOfRoutingOfConsignment,
        ActiveBorderTransportMeans = ActiveBorderTransportMeans,
        PlaceOfLoading = PlaceOfLoading,
        PlaceOfUnloading = PlaceOfUnloading,
        PreviousDocument = Nil, // TODO
        SupportingDocument = Nil, // TODO
        TransportDocument = Nil, // TODO
        AdditionalReference = Nil, // TODO
        AdditionalInformation = Nil, // TODO
        TransportCharges = TransportCharges,
        HouseConsignment = HouseConsignment
      )
  }

  def activeBorderTransportMeansReads: Reads[Seq[ActiveBorderTransportMeansType02]] =
    borderModeOfTransportReads flatMap {
      case Some(modeOfTransportAtTheBorder) =>
        (transportDetailsPath \ "transportMeansActiveList").readArray[ActiveBorderTransportMeansType02](
          activeBorderTransportMeansType02.reads(_, modeOfTransportAtTheBorder)
        )
      case _ =>
        Nil
    }

  private lazy val convertModeOfTransport: Option[String] => Option[String] = _ map {
    case "maritime" => "1"
    case "rail"     => "2"
    case "road"     => "3"
    case "air"      => "4"
    case "mail"     => "5"
    case "fixed"    => "7"
    case "waterway" => "8"
    case "unknown"  => "9"
    case _          => throw new Exception("Invalid mode of transport value")
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

  def reads(index: Int): Reads[AdditionalSupplyChainActorType] = (
    (index.toString: Reads[String]) and
      (__ \ "supplyChainActorType").read[String].map(convertRole) and
      (__ \ "identificationNumber").read[String]
  )(AdditionalSupplyChainActorType.apply _)

  private lazy val convertRole: String => String = {
    case "consolidator"     => "CS"
    case "freightForwarder" => "FW"
    case "manufacturer"     => "MF"
    case "warehouseKeeper"  => "WH"
    case _                  => throw new Exception("Invalid supply chain actor role value")
  }
}

object transportEquipmentType06 {

  def apply(
    sequenceNumber: String,
    containerIdentificationNumber: Option[String],
    Seal: Seq[SealType05],
    GoodsReference: Seq[GoodsReferenceType02]
  ): TransportEquipmentType06 =
    TransportEquipmentType06(sequenceNumber, containerIdentificationNumber, Seal.length, Seal, GoodsReference)

  def reads(index: Int): Reads[TransportEquipmentType06] = (
    (index.toString: Reads[String]) and
      (__ \ "containerIdentificationNumber").readNullable[String] and
      (__ \ "seals").readArray[SealType05](sealType05.reads) and
      (__ \ "itemNumbers").readArray[GoodsReferenceType02](goodsReferenceType02.reads)
  )(transportEquipmentType06.apply _)
}

object sealType05 {

  def reads(index: Int): Reads[SealType05] = (
    (index.toString: Reads[String]) and
      (__ \ "identificationNumber").read[String]
  )(SealType05.apply _)
}

object goodsReferenceType02 {

  def reads(index: Int): Reads[GoodsReferenceType02] = (
    (index.toString: Reads[String]) and
      (__ \ "itemNumber").read[String].map(BigInt(_))
  )(GoodsReferenceType02.apply _)
}

object locationOfGoodsType05 {

  implicit val reads: Reads[LocationOfGoodsType05] = (
    (__ \ "typeOfLocation").read[String].map(convertTypeOfLocation) and
      (__ \ "qualifierOfIdentification").read[String].map(convertQualifierOfIdentification) and
      (__ \ "identifier" \ "authorisationNumber").readNullable[String] and
      (__ \ "identifier" \ "additionalIdentifier").readNullable[String] and
      (__ \ "identifier" \ "unLocode").readNullable[String] and
      (__ \ "identifier" \ "customsOffice").readNullable[CustomsOfficeType02](customsOfficeType02.reads) and
      (__ \ "identifier" \ "coordinates").readNullable[GNSSType](gnssType.reads) and
      (__ \ "identifier" \ "eori").readNullable[EconomicOperatorType03](economicOperatorType03.reads) and
      (__ \ "identifier").read[Option[AddressType14]](addressType14.optionalReads) and
      (__ \ "identifier" \ "postalCode").readNullable[PostcodeAddressType02](postcodeAddressType02.reads) and
      (__ \ "contact").readNullable[ContactPersonType06](contactPersonType06.reads)
  )(LocationOfGoodsType05.apply _)

  private lazy val convertTypeOfLocation: String => String = {
    case "designatedLocation" => "A"
    case "authorisedPlace"    => "B"
    case "approvedPlace"      => "C"
    case "other"              => "D"
    case _                    => throw new Exception("Invalid type of location value")
  }

  private lazy val convertQualifierOfIdentification: String => String = {
    case "postalCode"              => "T"
    case "unlocode"                => "U"
    case "customsOfficeIdentifier" => "V"
    case "coordinates"             => "W"
    case "eoriNumber"              => "X"
    case "authorisationNumber"     => "Y"
    case "address"                 => "Z"
    case _                         => throw new Exception("Invalid qualifier of identification value")
  }
}

object customsOfficeType02 {

  implicit val reads: Reads[CustomsOfficeType02] =
    (__ \ "id").read[String].map(CustomsOfficeType02)
}

object gnssType {

  implicit val reads: Reads[GNSSType] = (
    (__ \ "latitude").read[String] and
      (__ \ "longitude").read[String]
  )(GNSSType.apply _)
}

object economicOperatorType03 {

  implicit val reads: Reads[EconomicOperatorType03] =
    __.read[String].map(EconomicOperatorType03)
}

object transportMeans {

  lazy val convertTypeOfIdentification: Option[String] => Option[String] = _ map {
    case "imoShipIdNumber"        => "10"
    case "seaGoingVessel"         => "11"
    case "wagonNumber"            => "20"
    case "trainNumber"            => "21"
    case "regNumberRoadVehicle"   => "30"
    case "regNumberRoadTrailer"   => "31"
    case "iataFlightNumber"       => "40"
    case "regNumberAircraft"      => "41"
    case "europeanVesselIdNumber" => "80"
    case "inlandWaterwaysVehicle" => "81"
    case "unknown"                => "99"
    case _                        => throw new Exception("Invalid type of identification value")
  }
}

object departureTransportMeansType03 {
  import transportMeans._

  implicit val reads: Reads[DepartureTransportMeansType03] = (
    ("0": Reads[String]) and
      (__ \ "identification").readNullable[String].map(convertTypeOfIdentification) and
      (__ \ "meansIdentificationNumber").readNullable[String] and
      (__ \ "vehicleCountry" \ "code").readNullable[String]
  )(DepartureTransportMeansType03.apply _)
}

object countryOfRoutingOfConsignmentType01 {

  def reads(index: Int): Reads[CountryOfRoutingOfConsignmentType01] = (
    (index.toString: Reads[String]) and
      (__ \ "countryOfRouting" \ "code").read[String]
  )(CountryOfRoutingOfConsignmentType01.apply _)
}

object activeBorderTransportMeansType02 {
  import transportMeans._

  def reads(index: Int, borderModeOfTransport: String): Reads[ActiveBorderTransportMeansType02] = (
    (index.toString: Reads[String]) and
      (__ \ "customsOfficeActiveBorder" \ "id").readNullable[String] and
      __.read[Option[String]](typeOfIdentificationReads(index, borderModeOfTransport)).map(convertTypeOfIdentification) and
      (__ \ "identificationNumber").readNullable[String] and
      (__ \ "nationality" \ "code").readNullable[String] and
      (__ \ "conveyanceReferenceNumber").readNullable[String]
  )(ActiveBorderTransportMeansType02.apply _)

  private def typeOfIdentificationReads(index: Int, borderModeOfTransport: String): Reads[Option[String]] =
    (index, borderModeOfTransport) match {
      case (0, "rail") => Some("trainNumber")
      case (0, "road") => Some("regNumberRoadVehicle")
      case _           => (__ \ "identification").readNullable[String]
    }
}

object placeOfLoadingType03 {

  implicit val reads: Reads[PlaceOfLoadingType03] = (
    (__ \ "unLocode").readNullable[String] and
      (__ \ "additionalInformation" \ "country" \ "code").readNullable[String] and
      (__ \ "additionalInformation" \ "location").readNullable[String]
  )(PlaceOfLoadingType03.apply _)
}

object placeOfUnloadingType01 {

  implicit val reads: Reads[PlaceOfUnloadingType01] = (
    (__ \ "unLocode").readNullable[String] and
      (__ \ "additionalInformation" \ "country" \ "code").readNullable[String] and
      (__ \ "additionalInformation" \ "location").readNullable[String]
  )(PlaceOfUnloadingType01.apply _)
}

object previousDocumentType09 {}

object supportingDocumentType05 {}

object transportDocumentType04 {}

object additionalReferenceType06 {}

object additionalInformationType03 {}

object transportChargesType {

  implicit val reads: Reads[TransportChargesType] =
    __.read[String].map(convertMethodOfPayment).map(TransportChargesType)

  private lazy val convertMethodOfPayment: String => String = {
    case "cash"                     => "A"
    case "creditCard"               => "B"
    case "cheque"                   => "C"
    case "electronicCreditTransfer" => "D"
    case "accountHolderWithCarrier" => "H"
    case "notPrePaid"               => "Y"
    case "other"                    => "Z"
    case _                          => throw new Exception("Invalid method of payment value")
  }
}

object houseConsignmentType10 {

  // TODO - Should be able to change this to `(HouseConsignmentType10.apply _)` once this is all done
  implicit val reads: Reads[HouseConsignmentType10] =
    itemsPath.readArray[ConsignmentItemType09](consignmentItemType09.reads).map {
      ConsignmentItem =>
        HouseConsignmentType10(
          sequenceNumber = "0",
          countryOfDispatch = None, // TODO
          grossMass = 1, // TODO
          referenceNumberUCR = None, // TODO
          Consignor = None, // TODO
          Consignee = None, // TODO
          AdditionalSupplyChainActor = Nil, // TODO
          DepartureTransportMeans = Nil, // TODO
          PreviousDocument = Nil, // TODO
          SupportingDocument = Nil, // TODO
          TransportDocument = Nil, // TODO
          AdditionalReference = Nil, // TODO
          AdditionalInformation = Nil, // TODO
          TransportCharges = None, // TODO
          ConsignmentItem = ConsignmentItem
        )
    }
}

object consignmentItemType09 {

  def reads(index: Int): Reads[ConsignmentItemType09] = (
    (index.toString: Reads[String]) and
      (index: Reads[Int]) and
      (__ \ "declarationType").readNullable[String] and
      (__ \ "countryOfDispatch" \ "code").readNullable[String] and
      (__ \ "countryOfDestination" \ "code").readNullable[String] and
      __.read[CommodityType06](commodityType06.reads)
  ).apply { // TODO - Should be able to change this to `(ConsignmentItemType09.apply _)` once this is all done
    (
      goodsItemNumber,
      declarationGoodsItemNumber,
      declarationType,
      countryOfDispatch,
      countryOfDestination,
      Commodity
    ) =>
      ConsignmentItemType09(
        goodsItemNumber = goodsItemNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber,
        declarationType = declarationType,
        countryOfDispatch = countryOfDispatch,
        countryOfDestination = countryOfDestination,
        referenceNumberUCR = None, // TODO
        Consignee = None, // TODO
        AdditionalSupplyChainActor = Nil, // TODO
        Commodity = Commodity,
        Packaging = Nil, // TODO
        PreviousDocument = Nil, // TODO
        SupportingDocument = Nil, // TODO
        TransportDocument = Nil, // TODO
        AdditionalReference = Nil, // TODO
        AdditionalInformation = Nil, // TODO
        TransportCharges = None // TODO
      )
  }
}

object commodityType06 {

  implicit val reads: Reads[CommodityType06] = (__ \ "description").read[String].map {
    descriptionOfGoods =>
      CommodityType06(
        descriptionOfGoods = descriptionOfGoods,
        cusCode = None, // TODO
        CommodityCode = None, // TODO
        DangerousGoods = Nil, // TODO
        GoodsMeasure = None // TODO
      )
  }
}
