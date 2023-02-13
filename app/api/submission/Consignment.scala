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

import api.ApiXmlHelper
import generated._
import models.UserAnswers

object Consignment {

//  def transform(uA: UserAnswers): ConsignmentType20 =
//    ConsignmentType20(
//      countryOfDispatch = transportDomain.preRequisites.countryOfDispatch.map(
//        x => x.code.code
//      ),
//      countryOfDestination = transportDomain.preRequisites.itemsDestinationCountry.map(
//        x => x.code.code
//      ),
//      containerIndicator = Some(ApiXmlHelper.boolToFlag(transportDomain.preRequisites.containerIndicator)),
//      inlandModeOfTransport = Some(transportDomain.transportMeans.inlandMode.inlandModeType.toString),
//      // TODO - In user answers modeOfTransportAtTheBorder is in the active list domain and has multiple entries.
//      // Do we set this to none here and populate in ActiveBorderTransportMeans?
//      modeOfTransportAtTheBorder = None,
//      grossMass = 1d, // TODO - from items domain when we have the journey built
//      referenceNumberUCR = transportDomain.preRequisites.ucr,
//      Carrier = carrierType(transportDomain.carrierDetails),
//      Consignor = consignor(traderDetailsDomain.consignment.consignor),
//      Consignee = consignee(traderDetailsDomain.consignment.consignee),
//      AdditionalSupplyChainActor = additionalSupplyChainActor(transportDomain.supplyChainActors),
//      TransportEquipment = Seq.empty, // TODO - when the journey is built
//      LocationOfGoods = locationOfGoods(routeDetailsDomain.locationOfGoods),
//      DepartureTransportMeans = departureTransportMeans(transportDomain.transportMeans),
//      CountryOfRoutingOfConsignment = countryOfRoutingOfConsignment(routeDetailsDomain.routing),
//      ActiveBorderTransportMeans = activeBorderTransportMeans(transportDomain.transportMeans),
//      PlaceOfLoading = placeOfLoading(routeDetailsDomain.loadingAndUnloading),
//      PlaceOfUnloading = placeOfUnloading(routeDetailsDomain.loadingAndUnloading),
//      PreviousDocument = Seq.empty, // TODO - at item level when the journey is built
//      SupportingDocument = Seq.empty, // TODO - at item level when the journey is built
//      TransportDocument = Seq.empty, // TODO - at item level when the journey is built
//      AdditionalReference = Seq.empty, // TODO - at item level when the journey is built
//      AdditionalInformation = Seq.empty, // TODO - at item level when the journey is built
//      TransportCharges = None, // TODO - at item level when the journey is built
//      HouseConsignment = Seq(houseConsignment()) // TODO - from items domain when we have it
//    )
//
//  private def carrierType(domain: CarrierDetailsDomain): Option[CarrierType04] =
//    Some(
//      CarrierType04(domain.identificationNumber,
//                    domain.contactPerson.map(
//                      x => ContactPersonType05(x.name, x.telephoneNumber, None)
//                    )
//      )
//    )
//
//  private def consignor(domain: Option[ConsignmentConsignorDomain]): Option[ConsignorType07] =
//    domain.map(
//      consignor =>
//        ConsignorType07(
//          consignor.eori.map(
//            x => x.value
//          ),
//          Some(consignor.name),
//          Some(AddressType17(consignor.address.numberAndStreet, consignor.address.postalCode, consignor.address.city, consignor.country.code.code))
//        )
//    )
//
//  private def consignee(domain: Option[ConsignmentConsigneeDomain]): Option[ConsigneeType05] =
//    domain.map(
//      consignee =>
//        ConsigneeType05(
//          consignee.eori.map(
//            x => x.value
//          ),
//          Some(consignee.name),
//          Some(AddressType17(consignee.address.numberAndStreet, consignee.address.postalCode, consignee.address.city, consignee.country.code.code))
//        )
//    )
//
//  private def additionalSupplyChainActor(domain: Option[SupplyChainActorsDomain]): Seq[AdditionalSupplyChainActorType] =
//    domain
//      .map(
//        supplyChainActorsDomain =>
//          supplyChainActorsDomain.SupplyChainActorsDomain.map(
//            supplyChainActor =>
//              AdditionalSupplyChainActorType(
//                supplyChainActorsDomain.SupplyChainActorsDomain.indexOf(supplyChainActor).toString,
//                supplyChainActor.role.toString,
//                supplyChainActor.identification
//              )
//          )
//      )
//      .getOrElse(Seq.empty)
//
//  private def locationOfGoods(domain: Option[LocationOfGoodsDomain]): Option[LocationOfGoodsType05] =
//    domain.map(
//      locationOfGoodsDomain =>
//        LocationOfGoodsType05(
//          typeOfLocation = locationOfGoodsDomain.typeOfLocation.code,
//          qualifierOfIdentification = locationOfGoodsDomain.qualifierOfIdentification.code,
//          authorisationNumber = authorisationNumber(locationOfGoodsDomain),
//          additionalIdentifier = additionalIdentifier(locationOfGoodsDomain),
//          UNLocode = unLocode(locationOfGoodsDomain),
//          CustomsOffice = customsOffice(locationOfGoodsDomain),
//          GNSS = gnss(locationOfGoodsDomain),
//          EconomicOperator = economicOperator(locationOfGoodsDomain),
//          Address = address(locationOfGoodsDomain),
//          PostcodeAddress = postcodeAddress(locationOfGoodsDomain),
//          ContactPerson = locationOfGoodsDomain.additionalContact.map(
//            p => ContactPersonType06(p.name, p.telephoneNumber)
//          )
//        )
//    )
//
//  private def authorisationNumber(domain: LocationOfGoodsDomain): Option[String] =
//    domain match {
//      case LocationOfGoodsDomain.LocationOfGoodsY(_, authorisationNumber, _, _) => Some(authorisationNumber)
//      case _                                                                    => None
//    }
//
//  private def additionalIdentifier(domain: LocationOfGoodsDomain): Option[String] =
//    domain match {
//      case LocationOfGoodsDomain.LocationOfGoodsY(_, _, additionalIdentifier, _) => additionalIdentifier
//      case _                                                                     => None
//    }
//
//  private def unLocode(domain: LocationOfGoodsDomain): Option[String] =
//    domain match {
//      case LocationOfGoodsDomain.LocationOfGoodsU(_, unLocode, _) => Some(unLocode.unLocodeExtendedCode)
//      case _                                                      => None
//    }
//
//  private def customsOffice(domain: LocationOfGoodsDomain): Option[CustomsOfficeType02] =
//    domain match {
//      case LocationOfGoodsDomain.LocationOfGoodsV(_, customsOffice) => Some(CustomsOfficeType02(customsOffice.id))
//      case _                                                        => None
//    }
//
//  private def gnss(domain: LocationOfGoodsDomain): Option[GNSSType] =
//    domain match {
//      case LocationOfGoodsDomain.LocationOfGoodsW(_, coordinates, _) => Some(GNSSType(coordinates.latitude, coordinates.longitude))
//      case _                                                         => None
//    }
//
//  private def economicOperator(domain: LocationOfGoodsDomain): Option[EconomicOperatorType03] =
//    domain match {
//      case LocationOfGoodsDomain.LocationOfGoodsX(_, identificationNumber, _, _) => Some(EconomicOperatorType03(identificationNumber))
//      case _                                                                     => None
//    }
//
//  private def address(domain: LocationOfGoodsDomain): Option[AddressType14] =
//    domain match {
//      case LocationOfGoodsDomain.LocationOfGoodsZ(_, country, address, _) =>
//        Some(AddressType14(address.numberAndStreet, address.postalCode, address.city, country.code.code))
//      case _ => None
//    }
//
//  private def postcodeAddress(domain: LocationOfGoodsDomain): Option[PostcodeAddressType02] =
//    domain match {
//      case LocationOfGoodsDomain.LocationOfGoodsT(_, postalCodeAddress, _) =>
//        Some(PostcodeAddressType02(Some(postalCodeAddress.streetNumber), postalCodeAddress.postalCode, postalCodeAddress.country.code.code))
//      case _ => None
//    }
//
//  // TODO - need to know how to capture this?
//  private def departureTransportMeans(domain: TransportMeansDomain): Seq[DepartureTransportMeansType03] =
//    domain match {
//      case TransportMeansDomainWithOtherInlandMode(_, means: TransportMeansDepartureDomainWithIdentification, _) =>
//        Seq(
//          DepartureTransportMeansType03("0",
//                                        Some(means.identification.identificationType.toString),
//                                        Some(means.identificationNumber),
//                                        Some(means.nationality.code)
//          )
//        )
//      case _ => Seq.empty
//    }
//
//  private def activeBorderTransportMeans(domain: TransportMeansDomain): Seq[ActiveBorderTransportMeansType02] =
//    domain match {
//      case TransportMeansDomainWithOtherInlandMode(_, _, transportMeansActiveList) =>
//        transportMeansActiveList
//          .map(
//            activeList =>
//              activeList.transportMeansActiveListDomain.map(
//                active =>
//                  ActiveBorderTransportMeansType02(
//                    sequenceNumber = activeList.transportMeansActiveListDomain.indexOf(active).toString,
//                    customsOfficeAtBorderReferenceNumber = Some(active.customsOffice.id),
//                    typeOfIdentification = Some(active.identification.borderModeType.toString),
//                    identificationNumber = Some(active.identificationNumber),
//                    nationality = active.nationality.map(
//                      n => n.code
//                    ),
//                    conveyanceReferenceNumber = active.conveyanceReferenceNumber
//                  )
//              )
//          )
//          .getOrElse(Seq.empty)
//      case _ => Seq.empty
//    }
//
//  private def countryOfRoutingOfConsignment(domain: RoutingDomain): Seq[CountryOfRoutingOfConsignmentType01] =
//    domain.countriesOfRouting.map(
//      c =>
//        CountryOfRoutingOfConsignmentType01(
//          domain.countriesOfRouting.indexOf(c).toString,
//          c.country.code.code
//        )
//    )
//
//  private def placeOfLoading(domain: LoadingAndUnloadingDomain): Option[PlaceOfLoadingType03] =
//    domain.loading.map(
//      x =>
//        PlaceOfLoadingType03(
//          x.unLocode.map(
//            locode => locode.unLocodeExtendedCode
//          ),
//          x.additionalInformation.map(
//            info => info.country.code.code
//          ),
//          x.additionalInformation.map(
//            info => info.location
//          )
//        )
//    )
//
//  private def placeOfUnloading(domain: LoadingAndUnloadingDomain): Option[PlaceOfUnloadingType01] =
//    domain.unloading.map(
//      x =>
//        PlaceOfUnloadingType01(
//          x.unLocode.map(
//            locode => locode.unLocodeExtendedCode
//          ),
//          x.additionalInformation.map(
//            info => info.country.code.code
//          ),
//          x.additionalInformation.map(
//            info => info.location
//          )
//        )
//    )
//
//  /** **************************************************************************************
//    * TODO - Test data section!!                                                            *
//    * This section must be updated from journey domain models when the journeys are built   *
//    * **************************************************************************************
//    */
//
//  // TODO - this is test data for submission API test
//  private def houseConsignment() =
//    HouseConsignmentType10(
//      sequenceNumber = "1",
//      countryOfDispatch = None,
//      grossMass = 1d,
//      referenceNumberUCR = None,
//      Consignor = None,
//      Consignee = None,
//      AdditionalSupplyChainActor = Seq.empty,
//      DepartureTransportMeans = Seq.empty,
//      PreviousDocument = Seq.empty,
//      SupportingDocument = Seq.empty,
//      TransportDocument = Seq.empty,
//      AdditionalReference = Seq.empty,
//      AdditionalInformation = Seq.empty,
//      TransportCharges = None,
//      ConsignmentItem = Seq(consignmentItem())
//    )
//
//  // TODO - this is test data for submission API test
//  private def consignmentItem() =
//    ConsignmentItemType09(
//      goodsItemNumber = "1",
//      declarationGoodsItemNumber = 1,
//      declarationType = None,
//      countryOfDispatch = None,
//      countryOfDestination = None,
//      referenceNumberUCR = None,
//      Consignee = None,
//      AdditionalSupplyChainActor = Seq.empty,
//      Commodity = commodity(),
//      Packaging = Seq(packaging())
//    )
//
//  // TODO - this is test data for submission API test
//  private def commodity() =
//    CommodityType06(
//      descriptionOfGoods = "test",
//      cusCode = None,
//      CommodityCode = None,
//      DangerousGoods = Seq.empty,
//      GoodsMeasure = None
//    )
//
//  // TODO - this is test data for submission API test
//  private def packaging() =
//    PackagingType03(
//      sequenceNumber = "1",
//      typeOfPackages = "Nu",
//      numberOfPackages = None,
//      shippingMarks = None
//    )
}
