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
import play.api.libs.json._

object Consignment {

  def transform(uA: UserAnswers): ConsignmentType20 =
    uA.metadata.data.as[ConsignmentType20](consignmentType20.reads)
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
    (transportDetailsPath \ "transportMeansActiveList").readArray[ActiveBorderTransportMeansType02](
      activeBorderTransportMeansType02.reads
    )

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

  lazy val qualifierOfIdentificationReads: Reads[String] =
    (__ \ "qualifierOfIdentification").read[String] orElse (__ \ "inferredQualifierOfIdentification").read[String]

  implicit val reads: Reads[LocationOfGoodsType05] = (
    (__ \ "typeOfLocation").read[String].map(convertTypeOfLocation) and
      qualifierOfIdentificationReads.map(convertQualifierOfIdentification) and
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
    ("1": Reads[String]) and
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

  lazy val identificationReads: Reads[Option[String]] =
    ((__ \ "identification").read[String] orElse (__ \ "inferredIdentification").read[String])
      .map(Option(_))
      .orElse(None)

  def reads(index: Int): Reads[ActiveBorderTransportMeansType02] = (
    (index.toString: Reads[String]) and
      (__ \ "customsOfficeActiveBorder" \ "id").readNullable[String] and
      identificationReads.map(convertTypeOfIdentification) and
      (__ \ "identificationNumber").readNullable[String] and
      (__ \ "nationality" \ "code").readNullable[String] and
      (__ \ "conveyanceReferenceNumber").readNullable[String]
  )(ActiveBorderTransportMeansType02.apply _)
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

  implicit val reads: Reads[HouseConsignmentType10] =
    documentsPath
      .read[JsArray]
      .map(_.value.toSeq)
      .flatMap {
        documents =>
          (
            ("1": Reads[String]) and
              itemsPath.readArray[ConsignmentItemType09](consignmentItemType09.reads(_, documents))
          ).apply { // TODO - Should be able to change this to `(HouseConsignmentType10.apply _)` once this is all done
            (
              sequenceNumber,
              ConsignmentItem
            ) =>
              HouseConsignmentType10(
                sequenceNumber = sequenceNumber,
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
}

object consignmentItemType09 {

  private def readString(document: JsValue, path: JsPath): Option[String] =
    readString(document, path.json.pick[JsString].map(_.value))

  private def readString(document: JsValue, reads: Reads[String]): Option[String] =
    document.validate(reads).asOpt

  private def areDocumentsEqual(document: JsValue, itemDocument: JsValue): Boolean = {
    val result = for {
      type1 <- readString(document, documentType.typeReads)
      code1 <- readString(document, documentType.codeReads)
      ref1  <- readString(document, __ \ "details" \ "documentReferenceNumber")
      type2 <- readString(itemDocument, __ \ "document" \ "type")
      code2 <- readString(itemDocument, __ \ "document" \ "code")
      ref2  <- readString(itemDocument, __ \ "document" \ "referenceNumber")
    } yield type1 == type2 && code1 == code2 && ref1 == ref2

    result.getOrElse(false)
  }

  private def documentHasCorrectType(document: JsValue, `type`: String): Boolean =
    document.validate(documentType.typeReads).exists(_ == `type`)

  private def documentAddedForItem(itemDocuments: Seq[JsValue], document: JsValue): Boolean =
    itemDocuments.exists(areDocumentsEqual(document, _))

  private def readDocuments[T](
    `type`: String,
    documents: Seq[JsValue],
    itemDocuments: Seq[JsValue]
  )(implicit rds: Int => Reads[T]): Reads[Seq[T]] = JsArray {
    documents.filter(documentHasCorrectType(_, `type`)).filter(documentAddedForItem(itemDocuments, _))
  }.readValuesAs[T]

  def reads(index: Int, documents: Seq[JsValue]): Reads[ConsignmentItemType09] =
    (__ \ "documents").readWithDefault[JsArray](JsArray()).map(_.value.toSeq).flatMap {
      itemDocuments =>
        (
          (index.toString: Reads[String]) and
            (index: Reads[Int]) and
            (__ \ "declarationType").readNullable[String] and
            (__ \ "countryOfDispatch" \ "code").readNullable[String] and
            (__ \ "countryOfDestination" \ "code").readNullable[String] and
            (__ \ "uniqueConsignmentReference").readNullable[String] and
            __.read[CommodityType06](commodityType06.reads) and
            (__ \ "packages").readArray[PackagingType03](packagingType03.reads) and
            readDocuments[PreviousDocumentType08]("Previous", documents, itemDocuments)(previousDocumentType08.reads) and
            readDocuments[SupportingDocumentType05]("Support", documents, itemDocuments)(supportingDocumentType05.reads) and
            readDocuments[TransportDocumentType04]("Transport", documents, itemDocuments)(transportDocumentType04.reads)
        ).apply { // TODO - Should be able to change this to `(ConsignmentItemType09.apply _)` once this is all done
          (
            goodsItemNumber,
            declarationGoodsItemNumber,
            declarationType,
            countryOfDispatch,
            countryOfDestination,
            referenceNumberUCR,
            Commodity,
            Packaging,
            PreviousDocument,
            SupportingDocument,
            TransportDocument
          ) =>
            ConsignmentItemType09(
              goodsItemNumber = goodsItemNumber,
              declarationGoodsItemNumber = declarationGoodsItemNumber,
              declarationType = declarationType,
              countryOfDispatch = countryOfDispatch,
              countryOfDestination = countryOfDestination,
              referenceNumberUCR = referenceNumberUCR,
              Consignee = None, // TODO
              AdditionalSupplyChainActor = Nil, // TODO
              Commodity = Commodity,
              Packaging = Packaging,
              PreviousDocument = PreviousDocument,
              SupportingDocument = SupportingDocument,
              TransportDocument = TransportDocument,
              AdditionalReference = Nil, // TODO
              AdditionalInformation = Nil, // TODO
              TransportCharges = None // TODO
            )
        }
    }
}

object commodityType06 {

  implicit val reads: Reads[CommodityType06] = (
    (__ \ "description").read[String] and
      (__ \ "customsUnionAndStatisticsCode").readNullable[String] and
      __.readNullable[CommodityCodeType02](commodityCodeType02.reads) and
      (__ \ "dangerousGoodsList").readArray[DangerousGoodsType01](dangerousGoodsType01.reads) and
      __.readNullable[GoodsMeasureType02](goodsMeasureType02.reads)
  )(CommodityType06.apply _)
}

object commodityCodeType02 {

  implicit val reads: Reads[CommodityCodeType02] = (
    (__ \ "commodityCode").read[String] and
      (__ \ "combinedNomenclatureCode").readNullable[String]
  )(CommodityCodeType02.apply _)
}

object dangerousGoodsType01 {

  def reads(index: Int): Reads[DangerousGoodsType01] = (
    (index.toString: Reads[String]) and
      (__ \ "unNumber").read[String]
  )(DangerousGoodsType01.apply _)
}

object goodsMeasureType02 {

  implicit val reads: Reads[GoodsMeasureType02] = (
    (__ \ "grossWeight").readNullable[BigDecimal] and
      (__ \ "netWeight").readNullable[BigDecimal] and
      (__ \ "supplementaryUnits").readNullable[BigDecimal]
  )(GoodsMeasureType02.apply _)
}

object packagingType03 {

  def reads(index: Int): Reads[PackagingType03] = (
    (index.toString: Reads[String]) and
      (__ \ "packageType" \ "code").read[String] and
      (__ \ "numberOfPackages").readNullable[BigInt] and
      (__ \ "shippingMark").readNullable[String]
  )(PackagingType03.apply _)
}

object documentType {

  private val genericType  = "type"
  private val previousType = "previousDocumentType"

  val typeReads: Reads[String] =
    (__ \ genericType \ "type").read[String] orElse (__ \ previousType \ "type").read[String]

  val codeReads: Reads[String] =
    (__ \ genericType \ "code").read[String] orElse (__ \ previousType \ "code").read[String]
}

object previousDocumentType08 {

  def reads(index: Int): Reads[PreviousDocumentType08] = (
    (index.toString: Reads[String]) and
      documentType.codeReads and
      (__ \ "details" \ "documentReferenceNumber").read[String] and
      (__ \ "details" \ "goodsItemNumber").readNullable[BigInt] and
      (__ \ "details" \ "packageType" \ "code").readNullable[String] and
      (__ \ "details" \ "numberOfPackages").readNullable[BigInt] and
      (__ \ "details" \ "metric" \ "code").readNullable[String] and
      (__ \ "details" \ "quantity").readNullable[BigDecimal] and
      (None: Reads[Option[String]])
  )(PreviousDocumentType08.apply _)
}

object supportingDocumentType05 {

  def reads(index: Int): Reads[SupportingDocumentType05] = (
    (index.toString: Reads[String]) and
      (__ \ "type" \ "code").read[String] and
      (__ \ "details" \ "documentReferenceNumber").read[String] and
      (__ \ "details" \ "lineItemNumber").readNullable[BigInt] and
      (None: Reads[Option[String]])
  )(SupportingDocumentType05.apply _)
}

object transportDocumentType04 {

  def reads(index: Int): Reads[TransportDocumentType04] = (
    (index.toString: Reads[String]) and
      (__ \ "type" \ "code").read[String] and
      (__ \ "details" \ "documentReferenceNumber").read[String]
  )(TransportDocumentType04.apply _)
}
