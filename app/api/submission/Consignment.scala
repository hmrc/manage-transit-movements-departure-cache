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
      equipmentsPath.readArray[TransportEquipmentType06](transportEquipmentType06.reads) and
      (routeDetailsPath \ "locationOfGoods").readNullable[LocationOfGoodsType05](locationOfGoodsType05.reads) and
      (routeDetailsPath \ "routing" \ "countriesOfRouting").readArray[CountryOfRoutingOfConsignmentType01](countryOfRoutingOfConsignmentType01.reads) and
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
      TransportEquipment,
      LocationOfGoods,
      CountryOfRoutingOfConsignment,
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
        TransportEquipment = TransportEquipment,
        LocationOfGoods = LocationOfGoods,
        DepartureTransportMeans = Nil, // TODO
        CountryOfRoutingOfConsignment = CountryOfRoutingOfConsignment,
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

object transportEquipmentType06 {

  def apply(containerIdentificationNumber: Option[String], Seal: Seq[SealType05], GoodsReference: Seq[GoodsReferenceType02])(
    sequenceNumber: Int
  ): TransportEquipmentType06 =
    TransportEquipmentType06(sequenceNumber.toString, containerIdentificationNumber, Seal.length, Seal, GoodsReference)

  def reads(index: Int): Reads[TransportEquipmentType06] = (
    (__ \ "containerIdentificationNumber").readNullable[String] and
      (__ \ "seals").readArray[SealType05](sealType05.reads) and
      (__ \ "itemNumbers").readArray[GoodsReferenceType02](goodsReferenceType02.reads)
  ).tupled.map((transportEquipmentType06.apply _).tupled).map(_(index))
}

object sealType05 {

  def apply(identifier: String)(
    sequenceNumber: Int
  ): SealType05 =
    SealType05(sequenceNumber.toString, identifier)

  def reads(index: Int): Reads[SealType05] =
    (__ \ "identificationNumber").read[String].map(sealType05(_)(index))
}

object goodsReferenceType02 {

  def apply(declarationGoodsItemNumber: String)(
    sequenceNumber: Int
  ): GoodsReferenceType02 =
    GoodsReferenceType02(sequenceNumber.toString, BigInt(declarationGoodsItemNumber))

  def reads(index: Int): Reads[GoodsReferenceType02] =
    (__ \ "itemNumber").read[String].map(goodsReferenceType02(_)(index))
}

object locationOfGoodsType05 {

  def apply(
    typeOfLocation: String,
    qualifierOfIdentification: String,
    authorisationNumber: Option[String],
    additionalIdentifier: Option[String],
    UNLocode: Option[String],
    CustomsOffice: Option[generated.CustomsOfficeType02],
    GNSS: Option[generated.GNSSType],
    EconomicOperator: Option[generated.EconomicOperatorType03],
    Address: Option[generated.AddressType14],
    PostcodeAddress: Option[generated.PostcodeAddressType02],
    ContactPerson: Option[generated.ContactPersonType06]
  ): LocationOfGoodsType05 = LocationOfGoodsType05(
    typeOfLocation = convertTypeOfLocation(typeOfLocation),
    qualifierOfIdentification = convertQualifierOfIdentification(qualifierOfIdentification),
    authorisationNumber = authorisationNumber,
    additionalIdentifier = additionalIdentifier,
    UNLocode = UNLocode,
    CustomsOffice = CustomsOffice,
    GNSS = GNSS,
    EconomicOperator = EconomicOperator,
    Address = Address,
    PostcodeAddress = PostcodeAddress,
    ContactPerson = ContactPerson
  )

  implicit val reads: Reads[LocationOfGoodsType05] = (
    (__ \ "typeOfLocation").read[String] and
      (__ \ "qualifierOfIdentification").read[String] and
      (__ \ "identifier" \ "authorisationNumber").readNullable[String] and
      (__ \ "identifier" \ "additionalIdentifier").readNullable[String] and
      (__ \ "identifier" \ "unLocode").readNullable[String] and
      (__ \ "identifier" \ "customsOffice").readNullable[CustomsOfficeType02](customsOfficeType02.reads) and
      (__ \ "identifier" \ "coordinates").readNullable[GNSSType](gnssType.reads) and
      (__ \ "identifier" \ "eori").readNullable[EconomicOperatorType03](economicOperatorType03.reads) and
      (__ \ "identifier").read[Option[AddressType14]](addressType14.optionalReads) and
      (__ \ "identifier" \ "postalCode").readNullable[PostcodeAddressType02](postcodeAddressType02.reads) and
      (__ \ "contact").readNullable[ContactPersonType06](contactPersonType06.reads)
  )(locationOfGoodsType05.apply _)

  private val convertTypeOfLocation: String => String = {
    case "designatedLocation" => "A"
    case "authorisedPlace"    => "B"
    case "approvedPlace"      => "C"
    case "other"              => "D"
    case _                    => throw new Exception("Invalid type of location value")
  }

  private val convertQualifierOfIdentification: String => String = {
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

object departureTransportMeansType03 {}

object countryOfRoutingOfConsignmentType01 {

  def apply(country: String)(
    sequenceNumber: Int
  ): CountryOfRoutingOfConsignmentType01 =
    CountryOfRoutingOfConsignmentType01(sequenceNumber.toString, country)

  def reads(index: Int): Reads[CountryOfRoutingOfConsignmentType01] =
    (__ \ "countryOfRouting" \ "code").read[String].map(countryOfRoutingOfConsignmentType01(_)(index))
}

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
