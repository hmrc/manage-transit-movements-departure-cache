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
import api.submission.houseConsignmentType10.RichHouseConsignmentType10
import api.submission.transportEquipmentType06.RichTransportEquipmentJsValue
import config.Constants.ModeOfTransport.Rail
import generated.*
import models.Phase.Transition
import models.{Phase, UserAnswers}
import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import java.util.UUID
import scala.language.implicitConversions

object Consignment {

  def transform(uA: UserAnswers, phase: Phase): ConsignmentType20 =
    uA.metadata.data.as[ConsignmentType20](consignmentType20.reads(phase)).postProcess(phase)

  implicit class RichConsignmentType20(value: ConsignmentType20) {

    def postProcess(phase: Phase): ConsignmentType20 = value
      .rollUpTransportCharges()
      .rollUpUCR()
      .rollUpCountryOfDispatch()
      .rollUpCountryOfDestination()
      .rollUpConsignee(phase)

    def rollUpTransportCharges(): ConsignmentType20 =
      rollUp[TransportChargesType, TransportChargesType](
        consignmentLevel = _.TransportCharges,
        itemLevel = _.TransportCharges,
        updateConsignmentLevel = transportCharges => _.copy(TransportCharges = transportCharges),
        updateItemLevel = _ => _.copy(TransportCharges = None)
      )(identity)

    def rollUpUCR(): ConsignmentType20 =
      rollUp[String, String](
        consignmentLevel = _.referenceNumberUCR,
        itemLevel = _.referenceNumberUCR,
        updateConsignmentLevel = ucr => _.copy(referenceNumberUCR = ucr),
        updateItemLevel = _ => _.copy(referenceNumberUCR = None)
      )(identity)

    def rollUpCountryOfDispatch(): ConsignmentType20 =
      rollUp[String, String](
        consignmentLevel = _.countryOfDispatch,
        itemLevel = _.countryOfDispatch,
        updateConsignmentLevel = countryOfDispatch => _.copy(countryOfDispatch = countryOfDispatch),
        updateItemLevel = _ => _.copy(countryOfDispatch = None)
      )(identity)

    def rollUpCountryOfDestination(): ConsignmentType20 =
      rollUp[String, String](
        consignmentLevel = _.countryOfDestination,
        itemLevel = _.countryOfDestination,
        updateConsignmentLevel = countryOfDestination => _.copy(countryOfDestination = countryOfDestination),
        updateItemLevel = _ => _.copy(countryOfDestination = None)
      )(identity)

    def rollUpConsignee(phase: Phase): ConsignmentType20 =
      phase match
        case Phase.Transition =>
          rollUp[ConsigneeType05, ConsigneeType02](
            consignmentLevel = _.Consignee,
            itemLevel = _.Consignee,
            updateConsignmentLevel = consignee => _.copy(Consignee = consignee),
            updateItemLevel = _ => _.copy(Consignee = None)
          )(_.asConsigneeType05)
        case Phase.PostTransition =>
          value

    def update(f: ConsignmentType20 => ConsignmentType20): ConsignmentType20 = f(value)

    private def rollUp[A, B](
      consignmentLevel: ConsignmentType20 => Option[A],
      itemLevel: ConsignmentItemType09 => Option[B],
      updateConsignmentLevel: Option[A] => ConsignmentType20 => ConsignmentType20,
      updateItemLevel: Option[B] => ConsignmentItemType09 => ConsignmentItemType09
    )(f: B => A): ConsignmentType20 =
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

  implicit class RichConsigneeType02(value: ConsigneeType02) {
    import api.submission.addressType12.RichAddressType12

    def asConsigneeType05: ConsigneeType05 = ConsigneeType05(
      identificationNumber = value.identificationNumber,
      name = value.name,
      Address = value.Address.map(_.asAddressType17)
    )
  }
}

object consignmentType20 {

  implicit def reads(phase: Phase): Reads[ConsignmentType20] = for {
    countryOfDispatch    <- (preRequisitesPath \ "countryOfDispatch" \ "code").readNullable[String]
    countryOfDestination <- (preRequisitesPath \ "itemsDestinationCountry" \ "code").readNullable[String]
    containerIndicator   <- (preRequisitesPath \ "containerIndicator").readNullable[Boolean]
    inlandModeOfTransport <- (transportDetailsPath \ "inlandMode" \ "code").readNullable[String].map {
      case Some(Rail) if phase == Transition => None
      case value                             => value
    }
    modeOfTransportAtTheBorder  <- (transportDetailsPath \ "borderModeOfTransport" \ "code").readNullable[String]
    referenceNumberUCR          <- (preRequisitesPath \ "uniqueConsignmentReference").readNullable[String]
    carrier                     <- (transportDetailsPath \ "carrierDetails").readNullable[CarrierType04](carrierType04.reads)
    consignor                   <- (consignmentPath \ "consignor").readNullable[ConsignorType07](consignorType07.reads)
    consignee                   <- (consignmentPath \ "consignee").readNullable[ConsigneeType05](consigneeType05.reads)
    additionalSupplyChainActors <- (transportDetailsPath \ "supplyChainActors").readArray[AdditionalSupplyChainActorType](additionalSupplyChainActorType.reads)
    transportEquipment          <- transportEquipmentReads
    locationOfGoods             <- (routeDetailsPath \ "locationOfGoods").readNullable[LocationOfGoodsType05](locationOfGoodsType05.reads)
    departureTransportMeans     <- departureTransportMeansReads
    countriesOfRouting          <- countriesOfRoutingReads
    activeBorderTransportMeans  <- activeBorderTransportMeansReads
    placeOfLoading              <- (loadingAndUnloadingPath \ "loading").readNullable[PlaceOfLoadingType03](placeOfLoadingType03.reads)
    placeOfUnloading            <- (loadingAndUnloadingPath \ "unloading").readNullable[PlaceOfUnloadingType01](placeOfUnloadingType01.reads)
    previousDocuments           <- previousDocumentsReads
    supportingDocuments         <- supportingDocumentsReads
    transportDocuments          <- transportDocumentsReads
    transportCharges            <- __.read[Option[TransportChargesType]](transportChargesType.consignmentReads)
    houseConsignments           <- __.read[HouseConsignmentType10](houseConsignmentType10.reads()).map(Seq(_))
    additionalReference         <- additionalReferenceReads
    additionalInformation       <- additionalInformationReads
  } yield ConsignmentType20(
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

  def transportEquipmentReads: Reads[Seq[TransportEquipmentType06]] =
    itemsPath.read[Seq[JsValue]].flatMap {
      items =>
        equipmentsPath.readFilteredArray[TransportEquipmentType06](
          _.hasBeenAddedToItem(items)
        )(transportEquipmentType06.reads(_, items))
    }

  def departureTransportMeansReads: Reads[Seq[DepartureTransportMeansType03]] =
    (transportMeansPath \ "departure")
      .readArray[DepartureTransportMeansType03](departureTransportMeansType03.reads)

  private def countriesOfRoutingReads: Reads[Seq[CountryOfRoutingOfConsignmentType01]] =
    (routeDetailsPath \ "routing" \ "countriesOfRouting")
      .readArray[CountryOfRoutingOfConsignmentType01](countryOfRoutingOfConsignmentType01.reads)

  def activeBorderTransportMeansReads: Reads[Seq[ActiveBorderTransportMeansType02]] =
    (transportMeansPath \ "active")
      .readArray[ActiveBorderTransportMeansType02](activeBorderTransportMeansType02.reads)

  private def previousDocumentsReads: Reads[Seq[PreviousDocumentType09]] =
    documentsPath
      .readFilteredArray[PreviousDocumentType09](
        _.hasCorrectTypeAndLevel("Previous", ConsignmentLevel)
      )(previousDocumentType09.reads)

  private def additionalReferenceReads: Reads[Seq[AdditionalReferenceType05]] =
    transportDetailsAdditionalReferencePath
      .readArray[AdditionalReferenceType05](additionalReferenceType05.reads)

  private def additionalInformationReads: Reads[Seq[AdditionalInformationType03]] =
    transportDetailsAdditionalInformationPath
      .readArray[AdditionalInformationType03](additionalInformationType03.consignmentReads)

  private def supportingDocumentsReads: Reads[Seq[SupportingDocumentType05]] =
    documentsPath
      .readFilteredArray[SupportingDocumentType05](
        _.hasCorrectTypeAndLevel("Support", ConsignmentLevel)
      )(supportingDocumentType05.reads)

  private def transportDocumentsReads: Reads[Seq[TransportDocumentType04]] =
    documentsPath.readFilteredArray[TransportDocumentType04](
      _.hasCorrectTypeAndLevel("Transport", ConsignmentLevel)
    )(transportDocumentType04.reads)
}

object carrierType04 {

  implicit val reads: Reads[CarrierType04] = (
    (__ \ "identificationNumber").read[String] and
      (__ \ "contact").readNullable[ContactPersonType05](contactPersonType05.reads)
  )(CarrierType04.apply)
}

object consignorType07 {

  implicit val reads: Reads[ConsignorType07] = (
    (__ \ "eori").readNullable[String] and
      (__ \ "name").readNullable[String] and
      __.read[Option[AddressType17]](addressType17.optionalReads) and
      (__ \ "contact").readNullable[ContactPersonType05](contactPersonType05.reads)
  )(ConsignorType07.apply)
}

object consigneeType05 {

  implicit val reads: Reads[ConsigneeType05] = (
    (__ \ "eori").readNullable[String] and
      (__ \ "name").readNullable[String] and
      __.read[Option[AddressType17]](addressType17.optionalReads)
  )(ConsigneeType05.apply)
}

object consigneeType02 {

  implicit val reads: Reads[ConsigneeType02] = (
    (__ \ "identificationNumber").readNullable[String] and
      (__ \ "name").readNullable[String] and
      __.read[Option[AddressType12]](addressType12.optionalReads)
  )(ConsigneeType02.apply)
}

object additionalSupplyChainActorType {

  def reads(index: Int): Reads[AdditionalSupplyChainActorType] = (
    Reads.pure[BigInt](index) and
      (__ \ "supplyChainActorType" \ "role").read[String] and
      (__ \ "identificationNumber").read[String]
  )(AdditionalSupplyChainActorType.apply)
}

object transportEquipmentType06 {

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
    Seal: Seq[SealType05],
    GoodsReference: Seq[GoodsReferenceType02]
  ): TransportEquipmentType06 =
    TransportEquipmentType06(sequenceNumber, containerIdentificationNumber, Seal.length, Seal, GoodsReference)

  private def goodsReferencesReads(transportEquipmentUuid: UUID, items: Seq[JsValue]): Reads[Seq[GoodsReferenceType02]] =
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
            GoodsReferenceType02(sequenceNumber, BigInt(declarationGoodsItemNumber))
        }
    }

  def reads(index: Int, items: Seq[JsValue]): Reads[TransportEquipmentType06] = (
    Reads.pure[BigInt](index) and
      (__ \ "containerIdentificationNumber").readNullable[String] and
      (__ \ "seals").readArray[SealType05](sealType05.reads) and
      (__ \ "uuid").read[UUID].flatMap(goodsReferencesReads(_, items))
  )(transportEquipmentType06.apply)
}

object sealType05 {

  def reads(index: Int): Reads[SealType05] = (
    Reads.pure[BigInt](index) and
      (__ \ "identificationNumber").read[String]
  )(SealType05.apply)
}

object locationOfGoodsType05 {

  private lazy val typeOfLocationReads: Reads[String] =
    (__ \ "typeOfLocation" \ "type").read[String] orElse
      (__ \ "inferredTypeOfLocation" \ "type").read[String]

  private lazy val qualifierOfIdentificationReads: Reads[String] =
    (__ \ "qualifierOfIdentification" \ "qualifier").read[String] orElse
      (__ \ "inferredQualifierOfIdentification" \ "qualifier").read[String]

  implicit val reads: Reads[LocationOfGoodsType05] = (
    typeOfLocationReads and
      qualifierOfIdentificationReads and
      (__ \ "identifier" \ "authorisationNumber").readNullable[String] and
      (__ \ "identifier" \ "additionalIdentifier").readNullable[String] and
      (__ \ "identifier" \ "unLocode").readNullable[String] and
      (__ \ "identifier" \ "customsOffice").readNullable[CustomsOfficeType02](customsOfficeType02.reads) and
      (__ \ "identifier" \ "coordinates").readNullable[GNSSType](gnssType.reads) and
      (__ \ "identifier" \ "eori").readNullable[EconomicOperatorType03](economicOperatorType03.reads) and
      (__ \ "identifier").read[Option[AddressType14]](addressType14.optionalReads) and
      Reads.pure[Option[PostcodeAddressType02]](None) and
      (__ \ "contact").readNullable[ContactPersonType06](contactPersonType06.reads)
  )(LocationOfGoodsType05.apply)
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

object economicOperatorType03 {

  implicit val reads: Reads[EconomicOperatorType03] =
    __.read[String].map(EconomicOperatorType03.apply)
}

object departureTransportMeansType03 {

  def reads(index: Int): Reads[DepartureTransportMeansType03] = (
    Reads.pure[BigInt](index) and
      (__ \ "identification" \ "type").readNullable[String] and
      (__ \ "meansIdentificationNumber").readNullable[String] and
      (__ \ "vehicleCountry" \ "code").readNullable[String]
  )(DepartureTransportMeansType03.apply)
}

object countryOfRoutingOfConsignmentType01 {

  def reads(index: Int): Reads[CountryOfRoutingOfConsignmentType01] = (
    Reads.pure[BigInt](index) and
      (__ \ "countryOfRouting" \ "code").read[String]
  )(CountryOfRoutingOfConsignmentType01.apply)
}

object activeBorderTransportMeansType02 {

  private lazy val identificationReads: Reads[Option[String]] =
    ((__ \ "identification" \ "code").read[String] orElse (__ \ "inferredIdentification" \ "code").read[String])
      .map(Option(_))
      .orElse(Reads.pure[Option[String]](None))

  def reads(index: Int): Reads[ActiveBorderTransportMeansType02] = (
    Reads.pure[BigInt](index) and
      (__ \ "customsOfficeActiveBorder" \ "id").readNullable[String] and
      identificationReads and
      (__ \ "identificationNumber").readNullable[String] and
      (__ \ "nationality" \ "code").readNullable[String] and
      (__ \ "conveyanceReferenceNumber").readNullable[String]
  )(ActiveBorderTransportMeansType02.apply)
}

object placeOfLoadingType03 {

  implicit val reads: Reads[PlaceOfLoadingType03] = (
    (__ \ "unLocode").readNullable[String] and
      (__ \ "additionalInformation" \ "country" \ "code").readNullable[String] and
      (__ \ "additionalInformation" \ "location").readNullable[String]
  )(PlaceOfLoadingType03.apply)
}

object placeOfUnloadingType01 {

  implicit val reads: Reads[PlaceOfUnloadingType01] = (
    (__ \ "unLocode").readNullable[String] and
      (__ \ "additionalInformation" \ "country" \ "code").readNullable[String] and
      (__ \ "additionalInformation" \ "location").readNullable[String]
  )(PlaceOfUnloadingType01.apply)
}

object transportChargesType {

  val itemReads: Reads[Option[TransportChargesType]] =
    (__ \ "methodOfPayment" \ "method").readNullable[String].map(_.map(TransportChargesType.apply))

  val consignmentReads: Reads[Option[TransportChargesType]] =
    (equipmentsAndChargesPath \ "paymentMethod" \ "method")
      .readNullable[String]
      .map(_.map(TransportChargesType.apply))
}

object houseConsignmentType10 {

  def reads(goodsItemNumberAcc: Int = 0): Reads[HouseConsignmentType10] =
    for {
      documents <- documentsPath.readWithDefault[JsArray](JsArray()).map(_.value.toSeq)
      consignmentItemReads = (goodsItemNumber: Int) => consignmentItemType09.reads(goodsItemNumber, goodsItemNumberAcc + goodsItemNumber, documents)
      consignmentItems <- itemsPath.readArray[ConsignmentItemType09](consignmentItemReads)
    } yield HouseConsignmentType10(
      sequenceNumber = 1,
      grossMass = consignmentItems.flatMap(_.Commodity.GoodsMeasure.flatMap(_.grossMass)).sum,
      ConsignmentItem = consignmentItems
    )

  implicit class RichHouseConsignmentType10(value: HouseConsignmentType10) {

    def updateItems(f: ConsignmentItemType09 => ConsignmentItemType09): HouseConsignmentType10 =
      value.copy(ConsignmentItem = value.ConsignmentItem.map(f))
  }
}

object consignmentItemType09 {

  /** @param goodsItemNumber
    *   should be unique to the house consignment (essentially the sequence number of an item within a house consignment)
    * @param declarationGoodsItemNumber
    *   should be unique to the consignment as a whole
    * @param documents
    *   documents as provided in the documents journey
    * @return
    */
  def reads(goodsItemNumber: Int, declarationGoodsItemNumber: Int, documents: Seq[JsValue]): Reads[ConsignmentItemType09] =
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
            itemConsigneePath.readNullable[ConsigneeType02](consigneeType02.reads) and
            (__ \ "supplyChainActors").readArray[AdditionalSupplyChainActorType](additionalSupplyChainActorType.reads) and
            __.read[CommodityType07](commodityType07.reads) and
            (__ \ "packages").readArray[PackagingType03](packagingType03.reads) and
            readDocuments[PreviousDocumentType08]("Previous")(previousDocumentType08.reads(goodsItemNumber, _)) and
            readDocuments[SupportingDocumentType05]("Support")(supportingDocumentType05.reads) and
            readDocuments[TransportDocumentType04]("Transport")(transportDocumentType04.reads) and
            (__ \ "additionalReferences").readArray[AdditionalReferenceType04](additionalReferenceType04.reads) and
            (__ \ "additionalInformationList").readArray[AdditionalInformationType03](additionalInformationType03.itemReads) and
            __.read[Option[TransportChargesType]](transportChargesType.itemReads)
        )(ConsignmentItemType09.apply)
    }
}

object commodityType07 {

  implicit val reads: Reads[CommodityType07] = (
    (__ \ "description").read[String] and
      (__ \ "customsUnionAndStatisticsCode").readNullable[String] and
      __.read[Option[CommodityCodeType02]](commodityCodeType02.reads) and
      (__ \ "dangerousGoodsList").readArray[DangerousGoodsType01](dangerousGoodsType01.reads) and
      __.readNullable[GoodsMeasureType02](goodsMeasureType02.reads)
  )(CommodityType07.apply)
}

object commodityCodeType02 {

  implicit val reads: Reads[Option[CommodityCodeType02]] = (
    (__ \ "commodityCode").readNullable[String] and
      (__ \ "combinedNomenclatureCode").readNullable[String]
  ).tupled.map {
    case (Some(harmonizedSystemSubHeadingCode), combinedNomenclatureCode) =>
      Some(CommodityCodeType02(harmonizedSystemSubHeadingCode, combinedNomenclatureCode))
    case _ => None
  }
}

object dangerousGoodsType01 {

  def reads(index: Int): Reads[DangerousGoodsType01] = (
    Reads.pure[BigInt](index) and
      (__ \ "unNumber").read[String]
  )(DangerousGoodsType01.apply)
}

object goodsMeasureType02 {

  implicit val reads: Reads[GoodsMeasureType02] = (
    (__ \ "grossWeight").readNullable[BigDecimal] and
      (__ \ "netWeight").readNullable[BigDecimal] and
      (__ \ "supplementaryUnits").readNullable[BigDecimal]
  )(GoodsMeasureType02.apply)
}

object packagingType03 {

  def reads(index: Int): Reads[PackagingType03] = (
    Reads.pure[BigInt](index) and
      (__ \ "packageType" \ "code").read[String] and
      (__ \ "numberOfPackages").readNullable[BigInt] and
      (__ \ "shippingMark").readNullable[String]
  )(PackagingType03.apply)
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

object previousDocumentType09 {

  def reads(index: Int): Reads[PreviousDocumentType09] = (
    Reads.pure[BigInt](index) and
      documentType.codeReads and
      (__ \ "details" \ "documentReferenceNumber").read[String] and
      (__ \ "details" \ "additionalInformation").readNullable[String]
  )(PreviousDocumentType09.apply)
}

object additionalReferenceType05 {

  def reads(index: Int): Reads[AdditionalReferenceType05] = (
    Reads.pure[BigInt](index) and
      (__ \ "type" \ "documentType").read[String] and
      (__ \ "additionalReferenceNumber").readNullable[String]
  )(AdditionalReferenceType05.apply)
}

object supportingDocumentType05 {

  def reads(index: Int): Reads[SupportingDocumentType05] = (
    Reads.pure[BigInt](index) and
      (__ \ "type" \ "code").read[String] and
      (__ \ "details" \ "documentReferenceNumber").read[String] and
      (__ \ "details" \ "lineItemNumber").readNullable[BigInt] and
      (__ \ "details" \ "additionalInformation").readNullable[String]
  )(SupportingDocumentType05.apply)
}

object transportDocumentType04 {

  def reads(index: Int): Reads[TransportDocumentType04] = (
    Reads.pure[BigInt](index) and
      (__ \ "type" \ "code").read[String] and
      (__ \ "details" \ "documentReferenceNumber").read[String]
  )(TransportDocumentType04.apply)
}

object additionalReferenceType04 {

  def reads(index: Int): Reads[AdditionalReferenceType04] = (
    Reads.pure[BigInt](index) and
      (__ \ "additionalReference" \ "documentType").read[String] and
      (__ \ "additionalReferenceNumber").readNullable[String]
  )(AdditionalReferenceType04.apply)
}

object additionalInformationType03 {

  def itemReads(index: Int): Reads[AdditionalInformationType03] = (
    Reads.pure[BigInt](index) and
      (__ \ "additionalInformationType" \ "code").read[String] and
      (__ \ "additionalInformation").readNullable[String]
  )(AdditionalInformationType03.apply)

  def consignmentReads(index: Int): Reads[AdditionalInformationType03] = (
    Reads.pure[BigInt](index) and
      (__ \ "type" \ "code").read[String] and
      (__ \ "text").readNullable[String]
  )(AdditionalInformationType03.apply)
}
