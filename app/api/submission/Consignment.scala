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

import api.submission.Level.*
import api.submission.documentType.RichDocumentJsValue
import api.submission.houseConsignmentType10.RichHouseConsignmentType13
import api.submission.transportEquipmentType03.RichTransportEquipmentJsValue
import generated.*
import models.UserAnswers
import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import java.util.UUID
import scala.language.implicitConversions

object Consignment {

  def transform(uA: UserAnswers): ConsignmentType23 =
    uA.metadata.data.as[ConsignmentType23](consignmentType23.reads).postProcess()

  implicit class RichConsignmentType23(value: ConsignmentType23) {

    def postProcess(): ConsignmentType23 = value
      .rollUpUCR()
      .rollUpCountryOfDispatch()
      .rollUpCountryOfDestination()

    def rollUpUCR(): ConsignmentType23 =
      rollUp[String, String](
        consignmentLevel = _.referenceNumberUCR,
        itemLevel = _.referenceNumberUCR,
        updateConsignmentLevel = ucr => _.copy(referenceNumberUCR = ucr),
        updateItemLevel = _ => _.copy(referenceNumberUCR = None)
      )(identity)

    def rollUpCountryOfDispatch(): ConsignmentType23 =
      rollUp[String, String](
        consignmentLevel = _.countryOfDispatch,
        itemLevel = _.countryOfDispatch,
        updateConsignmentLevel = countryOfDispatch => _.copy(countryOfDispatch = countryOfDispatch),
        updateItemLevel = _ => _.copy(countryOfDispatch = None)
      )(identity)

    def rollUpCountryOfDestination(): ConsignmentType23 =
      rollUp[String, String](
        consignmentLevel = _.countryOfDestination,
        itemLevel = _.countryOfDestination,
        updateConsignmentLevel = countryOfDestination => _.copy(countryOfDestination = countryOfDestination),
        updateItemLevel = _ => _.copy(countryOfDestination = None)
      )(identity)

    def update(f: ConsignmentType23 => ConsignmentType23): ConsignmentType23 = f(value)

    private def rollUp[A, B](
      consignmentLevel: ConsignmentType23 => Option[A],
      itemLevel: ConsignmentItemType10 => Option[B],
      updateConsignmentLevel: Option[A] => ConsignmentType23 => ConsignmentType23,
      updateItemLevel: Option[B] => ConsignmentItemType10 => ConsignmentItemType10
    )(f: B => A): ConsignmentType23 =
      value.HouseConsignment.flatMap(_.ConsignmentItem).map(itemLevel) match {
        case itemLevelValues if itemLevelValues.nonEmpty =>
          itemLevelValues match {
            case head :: tail if tail.forall(_ == head) =>
              val shouldRollUp = (consignmentLevel(value), head) match {
                case (None, Some(_))                                     => true
                case (Some(consignmentLevelValue), Some(itemLevelValue)) => consignmentLevelValue == f(itemLevelValue)
                case _                                                   => false
              }

              if (shouldRollUp) {
                value
                  .update(updateConsignmentLevel(head.map(f)))
                  .update(_.copy(HouseConsignment = value.HouseConsignment.map(_.updateItems(updateItemLevel(head)))))
              } else {
                value
              }
            case _ =>
              value
          }
        case _ =>
          value
      }
  }
}

object consignmentType23 {

  implicit val reads: Reads[ConsignmentType23] = for {
    countryOfDispatch          <- (preRequisitesPath \ "countryOfDispatch" \ "code").readNullable[String]
    countryOfDestination       <- (preRequisitesPath \ "itemsDestinationCountry" \ "code").readNullable[String]
    containerIndicator         <- (preRequisitesPath \ "containerIndicator").readNullable[Boolean]
    inlandModeOfTransport      <- (transportDetailsPath \ "inlandMode" \ "code").readNullable[String]
    modeOfTransportAtTheBorder <- (transportDetailsPath \ "borderModeOfTransport" \ "code").readNullable[String]
    referenceNumberUCR         <- (preRequisitesPath \ "uniqueConsignmentReference").readNullable[String]
    carrier                    <- (transportDetailsPath \ "carrierDetails").readNullable[CarrierType06](carrierType06.reads)
    consignor                  <- (consignmentPath \ "consignor").readNullable[ConsignorType10](consignorType10.reads)
    consignee                  <- (consignmentPath \ "consignee").readNullable[ConsigneeType05](consigneeType05.reads)
    additionalSupplyChainActors <- (transportDetailsPath \ "supplyChainActors").readArray[AdditionalSupplyChainActorType01](
      additionalSupplyChainActorType01.reads
    )
    transportEquipment         <- transportEquipmentReads
    locationOfGoods            <- (routeDetailsPath \ "locationOfGoods").readNullable[LocationOfGoodsType04](locationOfGoodsType04.reads)
    departureTransportMeans    <- departureTransportMeansReads
    countriesOfRouting         <- countriesOfRoutingReads
    activeBorderTransportMeans <- activeBorderTransportMeansReads
    placeOfLoading             <- (loadingAndUnloadingPath \ "loading").readNullable[PlaceOfLoadingType](placeOfLoadingType.reads)
    placeOfUnloading           <- (loadingAndUnloadingPath \ "unloading").readNullable[PlaceOfUnloadingType](placeOfUnloadingType.reads)
    previousDocuments          <- previousDocumentsReads
    supportingDocuments        <- supportingDocumentsReads
    transportDocuments         <- transportDocumentsReads
    transportCharges           <- __.read[Option[TransportChargesType01]](transportChargesType.reads)
    houseConsignments          <- __.read[HouseConsignmentType13](houseConsignmentType10.reads()).map(Seq(_))
    additionalReference        <- additionalReferenceReads
    additionalInformation      <- additionalInformationReads
  } yield ConsignmentType23(
    countryOfDispatch = countryOfDispatch,
    countryOfDestination = countryOfDestination,
    containerIndicator = containerIndicator,
    inlandModeOfTransport = inlandModeOfTransport,
    modeOfTransportAtTheBorder = modeOfTransportAtTheBorder,
    grossMass = houseConsignments.map(_.grossMass).sum,
    referenceNumberUCR = referenceNumberUCR,
    Carrier = carrier,
    Consignor = consignor,
    Consignee = consignee,
    AdditionalSupplyChainActor = additionalSupplyChainActors,
    TransportEquipment = transportEquipment,
    LocationOfGoods = locationOfGoods,
    DepartureTransportMeans = departureTransportMeans,
    CountryOfRoutingOfConsignment = countriesOfRouting,
    ActiveBorderTransportMeans = activeBorderTransportMeans,
    PlaceOfLoading = placeOfLoading,
    PlaceOfUnloading = placeOfUnloading,
    PreviousDocument = previousDocuments,
    SupportingDocument = supportingDocuments,
    TransportDocument = transportDocuments,
    AdditionalReference = additionalReference,
    AdditionalInformation = additionalInformation,
    TransportCharges = transportCharges,
    HouseConsignment = houseConsignments
  )

  def transportEquipmentReads: Reads[Seq[TransportEquipmentType03]] =
    itemsPath.read[Seq[JsValue]].flatMap {
      items =>
        equipmentsPath.readFilteredArray[TransportEquipmentType03](
          _.hasBeenAddedToItem(items)
        )(transportEquipmentType03.reads(_, items))
    }

  def departureTransportMeansReads: Reads[Seq[DepartureTransportMeansType01]] =
    (transportMeansPath \ "departure")
      .readArray[DepartureTransportMeansType01](departureTransportMeansType01.reads)

  private def countriesOfRoutingReads: Reads[Seq[CountryOfRoutingOfConsignmentType02]] =
    (routeDetailsPath \ "routing" \ "countriesOfRouting")
      .readArray[CountryOfRoutingOfConsignmentType02](countryOfRoutingOfConsignmentType02.reads)

  def activeBorderTransportMeansReads: Reads[Seq[ActiveBorderTransportMeansType03]] =
    (transportMeansPath \ "active")
      .readArray[ActiveBorderTransportMeansType03](activeBorderTransportMeansType03.reads)

  private def previousDocumentsReads: Reads[Seq[PreviousDocumentType05]] =
    documentsPath
      .readFilteredArray[PreviousDocumentType05](
        _.hasCorrectTypeAndLevel("Previous", ConsignmentLevel)
      )(previousDocumentType05.reads)

  private def additionalReferenceReads: Reads[Seq[AdditionalReferenceType02]] =
    transportDetailsAdditionalReferencePath
      .readArray[AdditionalReferenceType02](additionalReferenceType02.reads)

  private def additionalInformationReads: Reads[Seq[AdditionalInformationType02]] =
    transportDetailsAdditionalInformationPath
      .readArray[AdditionalInformationType02](additionalInformationType02.consignmentReads)

  private def supportingDocumentsReads: Reads[Seq[SupportingDocumentType03]] =
    documentsPath
      .readFilteredArray[SupportingDocumentType03](
        _.hasCorrectTypeAndLevel("Support", ConsignmentLevel)
      )(supportingDocumentType03.reads)

  private def transportDocumentsReads: Reads[Seq[TransportDocumentType01]] =
    documentsPath.readFilteredArray[TransportDocumentType01](
      _.hasCorrectTypeAndLevel("Transport", ConsignmentLevel)
    )(transportDocumentType01.reads)
}

object carrierType06 {

  implicit val reads: Reads[CarrierType06] = (
    (__ \ "identificationNumber").read[String] and
      (__ \ "contact").readNullable[ContactPersonType03](contactPersonType03.reads)
  )(CarrierType06.apply)
}

object consignorType10 {

  implicit val reads: Reads[ConsignorType10] = (
    (__ \ "eori").readNullable[String] and
      (__ \ "name").readNullable[String] and
      __.read[Option[AddressType14]](addressType14.optionalReads) and
      (__ \ "contact").readNullable[ContactPersonType03](contactPersonType03.reads)
  )(ConsignorType10.apply)
}

object consigneeType05 {

  implicit val reads: Reads[ConsigneeType05] = (
    (__ \ "eori").readNullable[String] and
      (__ \ "name").readNullable[String] and
      __.read[Option[AddressType14]](addressType14.optionalReads)
  )(ConsigneeType05.apply)
}

object additionalSupplyChainActorType01 {

  def reads(index: Int): Reads[AdditionalSupplyChainActorType01] = (
    Reads.pure[BigInt](index) and
      (__ \ "supplyChainActorType" \ "role").read[String] and
      (__ \ "identificationNumber").read[String]
  )(AdditionalSupplyChainActorType01.apply)
}

object transportEquipmentType03 {

  private def uuidReads: Reads[UUID] =
    (__ \ "transportEquipment").read[UUID]

  implicit class RichTransportEquipmentJsValue(transportEquipment: JsValue) {

    def hasBeenAddedToItem(items: Seq[JsValue]): Boolean = {
      val uuids = items.flatMap(_.asOpt(uuidReads))
      val uuid  = transportEquipment.asOpt((__ \ "uuid").read[UUID])
      uuid.exists(uuids.contains)
    }
  }

  def apply(
    sequenceNumber: BigInt,
    containerIdentificationNumber: Option[String],
    Seal: Seq[SealType01],
    GoodsReference: Seq[GoodsReferenceType01]
  ): TransportEquipmentType03 =
    TransportEquipmentType03(sequenceNumber, containerIdentificationNumber, Seal.length, Seal, GoodsReference)

  private def goodsReferencesReads(transportEquipmentUuid: UUID, items: Seq[JsValue]): Reads[Seq[GoodsReferenceType01]] =
    Reads.pure {
      items.zipWithSequenceNumber
        .foldLeft[Seq[Int]](Nil) {
          case (acc, (value, itemIndex)) =>
            value.validate(uuidReads) match {
              case JsSuccess(`transportEquipmentUuid`, _) => acc :+ itemIndex
              case _                                      => acc
            }
        }
        .zipWithSequenceNumber
        .map {
          case (declarationGoodsItemNumber, sequenceNumber) =>
            GoodsReferenceType01(sequenceNumber, BigInt(declarationGoodsItemNumber))
        }
    }

  def reads(index: Int, items: Seq[JsValue]): Reads[TransportEquipmentType03] = (
    Reads.pure[BigInt](index) and
      (__ \ "containerIdentificationNumber").readNullable[String] and
      (__ \ "seals").readArray[SealType01](sealType01.reads) and
      (__ \ "uuid").read[UUID].flatMap(goodsReferencesReads(_, items))
  )(transportEquipmentType03.apply)
}

object sealType01 {

  def reads(index: Int): Reads[SealType01] = (
    Reads.pure[BigInt](index) and
      (__ \ "identificationNumber").read[String]
  )(SealType01.apply)
}

object locationOfGoodsType04 {

  private lazy val typeOfLocationReads: Reads[String] =
    (__ \ "typeOfLocation" \ "type").read[String] orElse
      (__ \ "inferredTypeOfLocation" \ "type").read[String]

  private lazy val qualifierOfIdentificationReads: Reads[String] =
    (__ \ "qualifierOfIdentification" \ "qualifier").read[String] orElse
      (__ \ "inferredQualifierOfIdentification" \ "qualifier").read[String]

  implicit val reads: Reads[LocationOfGoodsType04] = (
    typeOfLocationReads and
      qualifierOfIdentificationReads and
      (__ \ "identifier" \ "authorisationNumber").readNullable[String] and
      (__ \ "identifier" \ "additionalIdentifier").readNullable[String] and
      (__ \ "identifier" \ "unLocode").readNullable[String] and
      (__ \ "identifier" \ "customsOffice").readNullable[CustomsOfficeType02](customsOfficeType02.reads) and
      (__ \ "identifier" \ "coordinates").readNullable[GNSSType](gnssType.reads) and
      (__ \ "identifier" \ "eori").readNullable[EconomicOperatorType02](economicOperatorType02.reads) and
      (__ \ "identifier").read[Option[AddressType06]](addressType06.optionalReads) and
      Reads.pure[Option[PostcodeAddressType]](None) and
      (__ \ "contact").readNullable[ContactPersonType01](contactPersonType01.reads)
  )(LocationOfGoodsType04.apply)
}

object customsOfficeType02 {

  implicit val reads: Reads[CustomsOfficeType02] =
    (__ \ "id").read[String].map(CustomsOfficeType02.apply)
}

object gnssType {

  implicit val reads: Reads[GNSSType] = (
    (__ \ "latitude").read[String] and
      (__ \ "longitude").read[String]
  )(GNSSType.apply)
}

object economicOperatorType02 {

  implicit val reads: Reads[EconomicOperatorType02] =
    __.read[String].map(EconomicOperatorType02.apply)
}

object departureTransportMeansType01 {

  def reads(index: Int): Reads[DepartureTransportMeansType01] = (
    Reads.pure[BigInt](index) and
      (__ \ "identification" \ "type").read[String] and
      (__ \ "meansIdentificationNumber").read[String] and
      (__ \ "vehicleCountry" \ "code").read[String]
  )(DepartureTransportMeansType01.apply)
}

object countryOfRoutingOfConsignmentType02 {

  def reads(index: Int): Reads[CountryOfRoutingOfConsignmentType02] = (
    Reads.pure[BigInt](index) and
      (__ \ "countryOfRouting" \ "code").read[String]
  )(CountryOfRoutingOfConsignmentType02.apply)
}

object activeBorderTransportMeansType03 {

  private lazy val identificationReads: Reads[String] =
    (__ \ "identification" \ "code").read[String] orElse (__ \ "inferredIdentification" \ "code").read[String]

  def reads(index: Int): Reads[ActiveBorderTransportMeansType03] = (
    Reads.pure[BigInt](index) and
      (__ \ "customsOfficeActiveBorder" \ "id").read[String] and
      identificationReads and
      (__ \ "identificationNumber").read[String] and
      (__ \ "nationality" \ "code").read[String] and
      (__ \ "conveyanceReferenceNumber").readNullable[String]
  )(ActiveBorderTransportMeansType03.apply)
}

object placeOfLoadingType {

  implicit val reads: Reads[PlaceOfLoadingType] = (
    (__ \ "unLocode").readNullable[String] and
      (__ \ "additionalInformation" \ "country" \ "code").readNullable[String] and
      (__ \ "additionalInformation" \ "location").readNullable[String]
  )(PlaceOfLoadingType.apply)
}

object placeOfUnloadingType {

  implicit val reads: Reads[PlaceOfUnloadingType] = (
    (__ \ "unLocode").readNullable[String] and
      (__ \ "additionalInformation" \ "country" \ "code").readNullable[String] and
      (__ \ "additionalInformation" \ "location").readNullable[String]
  )(PlaceOfUnloadingType.apply)
}

object transportChargesType {

  implicit val reads: Reads[Option[TransportChargesType01]] =
    (equipmentsAndChargesPath \ "paymentMethod" \ "method")
      .readNullable[String]
      .map(_.map(TransportChargesType01.apply))
}

object houseConsignmentType10 {

  def reads(goodsItemNumberAcc: Int = 0): Reads[HouseConsignmentType13] =
    for {
      documents <- documentsPath.readWithDefault[JsArray](JsArray()).map(_.value.toSeq)
      consignmentItemReads = (goodsItemNumber: Int) => consignmentItemType10.reads(goodsItemNumber, goodsItemNumberAcc + goodsItemNumber, documents)
      consignmentItems <- itemsPath.readArray[ConsignmentItemType10](consignmentItemReads)
    } yield HouseConsignmentType13(
      sequenceNumber = 1,
      grossMass = consignmentItems.map(_.Commodity.GoodsMeasure.grossMass).sum,
      ConsignmentItem = consignmentItems
    )

  implicit class RichHouseConsignmentType13(value: HouseConsignmentType13) {

    def updateItems(f: ConsignmentItemType10 => ConsignmentItemType10): HouseConsignmentType13 =
      value.copy(ConsignmentItem = value.ConsignmentItem.map(f))
  }
}

object consignmentItemType10 {

  /** @param goodsItemNumber
    *   should be unique to the house consignment (essentially the sequence number of an item within a house consignment)
    * @param declarationGoodsItemNumber
    *   should be unique to the consignment as a whole
    * @param documents
    *   documents as provided in the documents journey
    * @return
    */
  def reads(goodsItemNumber: Int, declarationGoodsItemNumber: Int, documents: Seq[JsValue]): Reads[ConsignmentItemType10] =
    (__ \ "documents").readWithDefault[JsArray](JsArray()).map(_.value.toSeq).flatMap {
      itemDocuments =>
        def readDocuments[T](`type`: String)(implicit rds: Int => Reads[T]): Reads[Seq[T]] =
          Reads.pure {
            documents.readFilteredValuesAs {
              document =>
                document.hasCorrectTypeAndLevel(`type`, ItemLevel) && document.addedForItem(itemDocuments)
            }
          }

        (
          Reads.pure[BigInt](goodsItemNumber) and
            Reads.pure[BigInt](declarationGoodsItemNumber) and
            (__ \ "declarationType" \ "code").readNullable[String] and
            (__ \ "countryOfDispatch" \ "code").readNullable[String] and
            (__ \ "countryOfDestination" \ "code").readNullable[String] and
            (__ \ "uniqueConsignmentReference").readNullable[String] and
            (__ \ "supplyChainActors").readArray[AdditionalSupplyChainActorType01](additionalSupplyChainActorType01.reads) and
            __.read[CommodityType10](commodityType10.reads) and
            (__ \ "packages").readArray[PackagingType01](packagingType01.reads) and
            readDocuments[PreviousDocumentType08]("Previous")(previousDocumentType08.reads(goodsItemNumber, _)) and
            readDocuments[SupportingDocumentType03]("Support")(supportingDocumentType03.reads) and
            (__ \ "additionalReferences").readArray[AdditionalReferenceType01](additionalReferenceType01.reads) and
            (__ \ "additionalInformationList").readArray[AdditionalInformationType02](additionalInformationType02.itemReads)
        )(ConsignmentItemType10.apply)
    }
}

object commodityType10 {

  implicit val reads: Reads[CommodityType10] = (
    (__ \ "description").read[String] and
      (__ \ "customsUnionAndStatisticsCode").readNullable[String] and
      __.read[Option[CommodityCodeType04]](commodityCodeType04.reads) and
      (__ \ "dangerousGoodsList").readArray[DangerousGoodsType01](dangerousGoodsType01.reads) and
      __.read[GoodsMeasureType04](goodsMeasureType04.reads)
  )(CommodityType10.apply)
}

object commodityCodeType04 {

  implicit val reads: Reads[Option[CommodityCodeType04]] = (
    (__ \ "commodityCode").readNullable[String] and
      (__ \ "combinedNomenclatureCode").readNullable[String]
  ).tupled.map {
    case (Some(harmonizedSystemSubHeadingCode), combinedNomenclatureCode) =>
      Some(CommodityCodeType04(harmonizedSystemSubHeadingCode, combinedNomenclatureCode))
    case _ => None
  }
}

object dangerousGoodsType01 {

  def reads(index: Int): Reads[DangerousGoodsType01] = (
    Reads.pure[BigInt](index) and
      (__ \ "unNumber").read[String]
  )(DangerousGoodsType01.apply)
}

object goodsMeasureType04 {

  implicit val reads: Reads[GoodsMeasureType04] = (
    (__ \ "grossWeight").read[BigDecimal] and
      (__ \ "netWeight").readNullable[BigDecimal] and
      (__ \ "supplementaryUnits").readNullable[BigDecimal]
  )(GoodsMeasureType04.apply)
}

object packagingType01 {

  def reads(index: Int): Reads[PackagingType01] = (
    Reads.pure[BigInt](index) and
      (__ \ "packageType" \ "code").read[String] and
      (__ \ "numberOfPackages").readNullable[BigInt] and
      (__ \ "shippingMark").readNullable[String]
  )(PackagingType01.apply)
}

object documentType {

  private val genericType  = "type"
  private val previousType = "previousDocumentType"

  private val typeReads: Reads[String] =
    (__ \ genericType \ "type").read[String] orElse (__ \ previousType \ "type").read[String]

  val codeReads: Reads[String] =
    (__ \ genericType \ "code").read[String] orElse (__ \ previousType \ "code").read[String]

  implicit class RichDocumentJsValue(document: JsValue) {

    def hasCorrectTypeAndLevel(`type`: String, level: Level): Boolean = {
      val reads: Reads[Boolean] =
        for {
          hasCorrectType  <- typeReads.map(_ == `type`)
          hasCorrectLevel <- __.read[Level].map(_ == level)
        } yield hasCorrectType && hasCorrectLevel

      document.validate(reads).exists(identity)
    }

    def readUuid(uuidPath: JsPath): Option[String] =
      document.validate(uuidPath.read[String]).asOpt

    def addedForItem(itemDocuments: Seq[JsValue]): Boolean = {

      def areDocumentsEqual(itemDocument: JsValue): Boolean = {
        val result = for {
          documentUuid     <- document.readUuid(__ \ "details" \ "uuid")
          itemDocumentUuid <- itemDocument.readUuid(__ \ "document")
        } yield documentUuid == itemDocumentUuid

        result.getOrElse(false)
      }

      itemDocuments.exists(areDocumentsEqual)
    }
  }
}

object previousDocumentType08 {

  def reads(goodsItemNumber: Int, documentIndex: Int): Reads[PreviousDocumentType08] = (
    Reads.pure[BigInt](documentIndex) and
      documentType.codeReads and
      (__ \ "details" \ "documentReferenceNumber").read[String] and
      Reads.pure[BigInt](goodsItemNumber).map(Some(_)) and
      (__ \ "details" \ "packageType" \ "code").readNullable[String] and
      (__ \ "details" \ "numberOfPackages").readNullable[BigInt] and
      (__ \ "details" \ "metric" \ "code").readNullable[String] and
      (__ \ "details" \ "quantity").readNullable[BigDecimal] and
      (__ \ "details" \ "additionalInformation").readNullable[String]
  )(PreviousDocumentType08.apply)
}

object previousDocumentType05 {

  def reads(index: Int): Reads[PreviousDocumentType05] = (
    Reads.pure[BigInt](index) and
      documentType.codeReads and
      (__ \ "details" \ "documentReferenceNumber").read[String] and
      (__ \ "details" \ "additionalInformation").readNullable[String]
  )(PreviousDocumentType05.apply)
}

object additionalReferenceType02 {

  def reads(index: Int): Reads[AdditionalReferenceType02] = (
    Reads.pure[BigInt](index) and
      (__ \ "type" \ "documentType").read[String] and
      (__ \ "additionalReferenceNumber").readNullable[String]
  )(AdditionalReferenceType02.apply)
}

object supportingDocumentType03 {

  def reads(index: Int): Reads[SupportingDocumentType03] = (
    Reads.pure[BigInt](index) and
      (__ \ "type" \ "code").read[String] and
      (__ \ "details" \ "documentReferenceNumber").read[String] and
      (__ \ "details" \ "lineItemNumber").readNullable[BigInt] and
      (__ \ "details" \ "additionalInformation").readNullable[String]
  )(SupportingDocumentType03.apply)
}

object transportDocumentType01 {

  def reads(index: Int): Reads[TransportDocumentType01] = (
    Reads.pure[BigInt](index) and
      (__ \ "type" \ "code").read[String] and
      (__ \ "details" \ "documentReferenceNumber").read[String]
  )(TransportDocumentType01.apply)
}

object additionalReferenceType01 {

  def reads(index: Int): Reads[AdditionalReferenceType01] = (
    Reads.pure[BigInt](index) and
      (__ \ "additionalReference" \ "documentType").read[String] and
      (__ \ "additionalReferenceNumber").readNullable[String]
  )(AdditionalReferenceType01.apply)
}

object additionalInformationType02 {

  def itemReads(index: Int): Reads[AdditionalInformationType02] = (
    Reads.pure[BigInt](index) and
      (__ \ "additionalInformationType" \ "code").read[String] and
      (__ \ "additionalInformation").readNullable[String]
  )(AdditionalInformationType02.apply)

  def consignmentReads(index: Int): Reads[AdditionalInformationType02] = (
    Reads.pure[BigInt](index) and
      (__ \ "type" \ "code").read[String] and
      (__ \ "text").readNullable[String]
  )(AdditionalInformationType02.apply)
}
