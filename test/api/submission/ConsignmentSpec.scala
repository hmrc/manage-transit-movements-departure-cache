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

import api.submission.Consignment.RichConsignmentType23
import api.submission.consignmentType23.{activeBorderTransportMeansReads, transportEquipmentReads}
import base.SpecBase
import generated.*
import generators.Generators
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}

class ConsignmentSpec extends SpecBase with Generators {

  "Consignment" when {

    "transform is called" must {

      "convert to API format with documents" in {

        val json: JsValue = getUserAnswersJson("1")

        val uA: UserAnswers = json.as[UserAnswers]

        val converted: ConsignmentType23 = Consignment.transform(uA)

        converted.countryOfDispatch shouldEqual Some("FR")
        converted.countryOfDestination shouldEqual Some("IT")
        converted.containerIndicator shouldEqual Some(Number1)
        converted.inlandModeOfTransport shouldEqual Some("1")
        converted.modeOfTransportAtTheBorder shouldEqual Some("1")
        converted.grossMass shouldEqual 580.245
        converted.referenceNumberUCR shouldEqual Some("ucr123")

        converted.Carrier shouldEqual Some(
          CarrierType06(
            identificationNumber = "carrier1",
            ContactPerson = Some(
              ContactPersonType03(
                name = "Carrier Contact",
                phoneNumber = "+44 808 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.Consignor shouldEqual Some(
          ConsignorType10(
            identificationNumber = Some("consignor1"),
            name = Some("Mr Consignor"),
            Address = Some(
              AddressType14(
                streetAndNumber = "21 Test Lane",
                postcode = Some("NE1 1NE"),
                city = "Newcastle upon Tyne",
                country = "GB"
              )
            ),
            ContactPerson = Some(
              ContactPersonType03(
                name = "Consignor Contact",
                phoneNumber = "+44 101 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.Consignee shouldEqual Some(
          ConsigneeType05(
            identificationNumber = Some("consignee1"),
            name = Some("Mr Consignee"),
            Address = Some(
              AddressType14(
                streetAndNumber = "21 Test Rue",
                postcode = Some("PA1 1PA"),
                city = "Paris",
                country = "FR"
              )
            )
          )
        )

        converted.AdditionalSupplyChainActor shouldEqual Seq(
          AdditionalSupplyChainActorType01(
            sequenceNumber = 1,
            role = "CS",
            identificationNumber = "sca1"
          ),
          AdditionalSupplyChainActorType01(
            sequenceNumber = 2,
            role = "FW",
            identificationNumber = "sca2"
          ),
          AdditionalSupplyChainActorType01(
            sequenceNumber = 3,
            role = "MF",
            identificationNumber = "sca3"
          ),
          AdditionalSupplyChainActorType01(
            sequenceNumber = 4,
            role = "WH",
            identificationNumber = "sca4"
          )
        )

        converted.TransportEquipment shouldEqual Seq(
          TransportEquipmentType03(
            sequenceNumber = 1,
            containerIdentificationNumber = Some("container id 1"),
            numberOfSeals = 2,
            Seal = Seq(
              SealType01(
                sequenceNumber = 1,
                identifier = "seal 1"
              ),
              SealType01(
                sequenceNumber = 2,
                identifier = "seal 2"
              )
            ),
            GoodsReference = Seq(
              GoodsReferenceType01(
                sequenceNumber = 1,
                declarationGoodsItemNumber = 1
              ),
              GoodsReferenceType01(
                sequenceNumber = 2,
                declarationGoodsItemNumber = 2
              )
            )
          )
        )

        converted.LocationOfGoods shouldEqual Some(
          LocationOfGoodsType04(
            typeOfLocation = "A",
            qualifierOfIdentification = "T",
            authorisationNumber = Some("authorisation number"),
            additionalIdentifier = Some("additional identifier"),
            UNLocode = Some("DEAAL"),
            CustomsOffice = Some(CustomsOfficeType02(referenceNumber = "XI000142")),
            GNSS = Some(
              GNSSType(
                latitude = "lat",
                longitude = "lon"
              )
            ),
            EconomicOperator = Some(EconomicOperatorType02(identificationNumber = "GB12345")),
            Address = Some(
              AddressType06(
                streetAndNumber = "21 Test Camino",
                postcode = Some("ES1 1SE"),
                city = "Madrid",
                country = "ES"
              )
            ),
            PostcodeAddress = None,
            ContactPerson = Some(
              ContactPersonType01(
                name = "Location of goods Contact",
                phoneNumber = "+44 202 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.DepartureTransportMeans shouldEqual Seq(
          DepartureTransportMeansType01(
            sequenceNumber = 1,
            typeOfIdentification = "10",
            identificationNumber = "means id number",
            nationality = "FR"
          )
        )

        converted.CountryOfRoutingOfConsignment shouldEqual Seq(
          CountryOfRoutingOfConsignmentType02(
            sequenceNumber = 1,
            country = "AD"
          ),
          CountryOfRoutingOfConsignmentType02(
            sequenceNumber = 2,
            country = "AR"
          )
        )

        converted.ActiveBorderTransportMeans shouldEqual Seq(
          ActiveBorderTransportMeansType03(
            sequenceNumber = 1,
            customsOfficeAtBorderReferenceNumber = "IT018101",
            typeOfIdentification = "11",
            identificationNumber = "active id number",
            nationality = "ES",
            conveyanceReferenceNumber = Some("conveyance ref number")
          )
        )

        converted.PlaceOfLoading shouldEqual Some(
          PlaceOfLoadingType(
            Some("AEFAT"),
            Some("Loading country"),
            Some("Loading location")
          )
        )

        converted.PlaceOfUnloading shouldEqual Some(
          PlaceOfUnloadingType(
            Some("ADALV"),
            Some("Unloading country"),
            Some("Unloading location")
          )
        )

        converted.PreviousDocument shouldEqual Seq(
          PreviousDocumentType05(
            sequenceNumber = 1,
            typeValue = "IM",
            referenceNumber = "previous3",
            complementOfInformation = Some("complement of information previous3")
          )
        )

        converted.SupportingDocument shouldEqual Seq(
          SupportingDocumentType03(
            sequenceNumber = 1,
            typeValue = "C673",
            referenceNumber = "support2",
            documentLineItemNumber = None,
            complementOfInformation = None
          )
        )

        converted.TransportDocument shouldEqual Seq(
          TransportDocumentType01(
            sequenceNumber = 1,
            typeValue = "235",
            referenceNumber = "transport2"
          )
        )

        converted.AdditionalReference shouldEqual Seq(
          AdditionalReferenceType02(
            sequenceNumber = 1,
            typeValue = "type123",
            referenceNumber = Some("arno1")
          ),
          AdditionalReferenceType02(
            sequenceNumber = 2,
            typeValue = "type321",
            referenceNumber = Some("1onra")
          )
        )

        converted.AdditionalInformation shouldEqual Seq(
          AdditionalInformationType02(
            sequenceNumber = 1,
            code = "adinftype1",
            text = Some("orca")
          ),
          AdditionalInformationType02(
            sequenceNumber = 2,
            code = "adinftype2",
            text = Some("acro")
          )
        )

        converted.TransportCharges shouldEqual Some(
          TransportChargesType01("A")
        )

        converted.HouseConsignment.size shouldEqual 1
        val ans = converted.HouseConsignment.head

        val exp = HouseConsignmentType13(
          sequenceNumber = 1,
          countryOfDispatch = None,
          grossMass = 580.245,
          referenceNumberUCR = None,
          Consignor = None,
          Consignee = None,
          AdditionalSupplyChainActor = Nil,
          DepartureTransportMeans = Nil,
          PreviousDocument = Nil,
          SupportingDocument = Nil,
          TransportDocument = Nil,
          AdditionalReference = Nil,
          AdditionalInformation = Nil,
          TransportCharges = None,
          ConsignmentItem = Seq(
            ConsignmentItemType10(
              goodsItemNumber = 1,
              declarationGoodsItemNumber = 1,
              declarationType = Some("T1"),
              countryOfDispatch = Some("GB"),
              countryOfDestination = Some("FR"),
              referenceNumberUCR = Some("UCR 1"),
              AdditionalSupplyChainActor = Seq(
                AdditionalSupplyChainActorType01(
                  sequenceNumber = 1,
                  role = "CS",
                  identificationNumber = "itemSCA1"
                ),
                AdditionalSupplyChainActorType01(
                  sequenceNumber = 2,
                  role = "FW",
                  identificationNumber = "itemSCA2"
                ),
                AdditionalSupplyChainActorType01(
                  sequenceNumber = 3,
                  role = "MF",
                  identificationNumber = "itemSCA3"
                ),
                AdditionalSupplyChainActorType01(
                  sequenceNumber = 4,
                  role = "WH",
                  identificationNumber = "itemSCA4"
                )
              ),
              Commodity = CommodityType10(
                descriptionOfGoods = "Description 1",
                cusCode = Some("CUS code 1"),
                CommodityCode = Some(
                  CommodityCodeType04(
                    harmonizedSystemSubHeadingCode = "commodity code 1",
                    combinedNomenclatureCode = Some("CN code 1")
                  )
                ),
                DangerousGoods = Seq(
                  DangerousGoodsType01(
                    sequenceNumber = 1,
                    UNNumber = "UN number 1_1"
                  ),
                  DangerousGoodsType01(
                    sequenceNumber = 2,
                    UNNumber = "UN number 1_2"
                  )
                ),
                GoodsMeasure = GoodsMeasureType04(
                  grossMass = BigDecimal(123.456),
                  netMass = Some(BigDecimal(1234)),
                  supplementaryUnits = Some(BigDecimal(12345))
                )
              ),
              Packaging = Seq(
                PackagingType01(
                  sequenceNumber = 1,
                  typeOfPackages = "VL",
                  numberOfPackages = None,
                  shippingMarks = None
                ),
                PackagingType01(
                  sequenceNumber = 2,
                  typeOfPackages = "NE",
                  numberOfPackages = Some(5),
                  shippingMarks = None
                ),
                PackagingType01(
                  sequenceNumber = 3,
                  typeOfPackages = "TR",
                  numberOfPackages = None,
                  shippingMarks = Some("mark3")
                )
              ),
              PreviousDocument = Seq(
                PreviousDocumentType08(
                  sequenceNumber = 1,
                  typeValue = "CO",
                  referenceNumber = "previous1",
                  goodsItemNumber = Some(1),
                  typeOfPackages = None,
                  numberOfPackages = None,
                  measurementUnitAndQualifier = None,
                  quantity = None,
                  complementOfInformation = None
                ),
                PreviousDocumentType08(
                  sequenceNumber = 2,
                  typeValue = "T2F",
                  referenceNumber = "previous2",
                  goodsItemNumber = Some(1),
                  typeOfPackages = Some("AT"),
                  numberOfPackages = Some(12),
                  measurementUnitAndQualifier = Some("MIL"),
                  quantity = Some(13),
                  complementOfInformation = Some("complement of information previous2")
                )
              ),
              SupportingDocument = Seq(
                SupportingDocumentType03(
                  sequenceNumber = 1,
                  typeValue = "C673",
                  referenceNumber = "support1",
                  documentLineItemNumber = Some(678),
                  complementOfInformation = Some("complement of information support1")
                )
              ),
              AdditionalReference = Seq(
                AdditionalReferenceType01(
                  sequenceNumber = 1,
                  typeValue = "ar1",
                  referenceNumber = Some("arno1")
                ),
                AdditionalReferenceType01(
                  sequenceNumber = 2,
                  typeValue = "ar2",
                  referenceNumber = None
                )
              ),
              AdditionalInformation = Seq(
                AdditionalInformationType02(
                  sequenceNumber = 1,
                  code = "aiCode1",
                  text = Some("ai1")
                ),
                AdditionalInformationType02(
                  sequenceNumber = 2,
                  code = "aiCode2",
                  text = Some("ai2")
                )
              )
            ),
            ConsignmentItemType10(
              goodsItemNumber = 2,
              declarationGoodsItemNumber = 2,
              declarationType = Some("T2"),
              countryOfDispatch = Some("DE"),
              countryOfDestination = Some("ES"),
              referenceNumberUCR = Some("UCR 2"),
              AdditionalSupplyChainActor = Nil,
              Commodity = CommodityType10(
                descriptionOfGoods = "Description 2",
                cusCode = Some("CUS code 2"),
                CommodityCode = Some(
                  CommodityCodeType04(
                    harmonizedSystemSubHeadingCode = "commodity code 2",
                    combinedNomenclatureCode = Some("CN code 2")
                  )
                ),
                DangerousGoods = Seq(
                  DangerousGoodsType01(
                    sequenceNumber = 1,
                    UNNumber = "UN number 2_1"
                  ),
                  DangerousGoodsType01(
                    sequenceNumber = 2,
                    UNNumber = "UN number 2_2"
                  )
                ),
                GoodsMeasure = GoodsMeasureType04(
                  grossMass = BigDecimal(456.789),
                  netMass = None,
                  supplementaryUnits = None
                )
              ),
              Packaging = Nil,
              PreviousDocument = Nil,
              SupportingDocument = Nil,
              AdditionalReference = Seq(
                AdditionalReferenceType01(
                  sequenceNumber = 1,
                  typeValue = "ar1",
                  referenceNumber = Some("arno1")
                )
              ),
              AdditionalInformation = Seq(
                AdditionalInformationType02(
                  sequenceNumber = 1,
                  code = "aiCode1",
                  text = Some("ai1")
                )
              )
            )
          )
        )

        ans shouldEqual exp
      }

      "convert to API format without documents" in {

        val json: JsValue = Json.parse(s"""
             |{
             |  "_id" : "$uuid",
             |  "lrn" : "$lrn",
             |  "eoriNumber" : "$eoriNumber",
             |  "isSubmitted" : "notSubmitted",
             |  "isTransitional": false,
             |  "data" : {
             |    "traderDetails" : {
             |      "consignment" : {
             |        "consignor" : {
             |          "eori" : "consignor1",
             |          "name" : "Mr Consignor",
             |          "country" : {
             |            "code" : "GB",
             |            "description" : "United Kingdom"
             |          },
             |          "address" : {
             |            "numberAndStreet" : "21 Test Lane",
             |            "city" : "Newcastle upon Tyne",
             |            "postalCode" : "NE1 1NE"
             |          },
             |          "contact" : {
             |            "name" : "Consignor Contact",
             |            "telephoneNumber" : "+44 101 157 0192"
             |          }
             |        },
             |        "consignee" : {
             |          "eori" : "consignee1",
             |          "name" : "Mr Consignee",
             |          "country" : {
             |            "code" : "FR",
             |            "description" : "France"
             |          },
             |          "address" : {
             |            "numberAndStreet" : "21 Test Rue",
             |            "city" : "Paris",
             |            "postalCode" : "PA1 1PA"
             |          }
             |        }
             |      }
             |    },
             |    "routeDetails" : {
             |      "routing" : {
             |        "countriesOfRouting" : [
             |          {
             |            "countryOfRouting" : {
             |              "code" : "AD",
             |              "description" : "Andorra"
             |            }
             |          },
             |          {
             |            "countryOfRouting" : {
             |              "code" : "AR",
             |              "description" : "Argentina"
             |            }
             |          }
             |        ]
             |      },
             |      "locationOfGoods" : {
             |        "typeOfLocation" : {
             |          "type": "A",
             |          "description": "Designated location"
             |        },
             |        "qualifierOfIdentification" : {
             |          "qualifier": "T",
             |          "description": "Postal code"
             |        },
             |        "identifier" : {
             |          "authorisationNumber" : "authorisation number",
             |          "additionalIdentifier" : "additional identifier",
             |          "unLocode" : "DEAAL",
             |          "customsOffice" : {
             |            "id" : "XI000142",
             |            "name" : "Belfast EPU",
             |            "phoneNumber" : "+44 (0)02896 931537"
             |          },
             |          "coordinates" : {
             |            "latitude" : "lat",
             |            "longitude" : "lon"
             |          },
             |          "eori" : "GB12345",
             |          "country" : {
             |            "code" : "ES",
             |            "description" : "Spain"
             |          },
             |          "address" : {
             |            "numberAndStreet" : "21 Test Camino",
             |            "city" : "Madrid",
             |            "postalCode" : "ES1 1SE"
             |          },
             |          "postalCode" : {
             |            "streetNumber" : "21",
             |            "postalCode" : "DE1 1DE",
             |            "country" : {
             |              "code" : "DE",
             |              "description" : "Germany"
             |            }
             |          }
             |        },
             |        "contact" : {
             |          "name" : "Location of goods Contact",
             |          "telephoneNumber" : "+44 202 157 0192"
             |        }
             |      },
             |      "loadingAndUnloading" : {
             |        "loading" : {
             |          "unLocode" : "AEFAT",
             |          "additionalInformation" : {
             |            "country" : {
             |              "code" : "Loading country",
             |              "description" : "United Kingdom"
             |            },
             |            "location" : "Loading location"
             |          }
             |        },
             |        "unloading" : {
             |          "unLocode" : "ADALV",
             |          "additionalInformation" : {
             |            "country" : {
             |              "code" : "Unloading country",
             |              "description" : "United Kingdom"
             |            },
             |            "location" : "Unloading location"
             |          }
             |        }
             |      }
             |    },
             |    "transportDetails" : {
             |      "additionalReference" : [
             |        {
             |          "type" : {
             |            "documentType" : "type123"
             |          },
             |          "additionalReferenceNumber" : "ARNO1"
             |        },
             |        {
             |          "type" : {
             |            "documentType" : "type321"
             |          },
             |          "additionalReferenceNumber" : "1ONRA"
             |        }
             |      ],
             |      "additionalInformation" : [
             |        {
             |          "type" : {
             |            "code" : "ADDINFTYPE1"
             |          },
             |          "text" : "ORCA"
             |        },
             |        {
             |          "type" : {
             |            "code" : "ADDINFTYPE2"
             |          },
             |          "text" : "ACRO"
             |        }
             |      ],
             |      "preRequisites" : {
             |        "uniqueConsignmentReference" : "ucr123",
             |        "countryOfDispatch" : {
             |          "code" : "FR",
             |          "description" : "France"
             |        },
             |        "itemsDestinationCountry" : {
             |          "code" : "IT",
             |          "description" : "Italy"
             |        },
             |        "containerIndicator" : true
             |      },
             |      "inlandMode" : {
             |        "code": "1",
             |        "description": "Maritime Transport"
             |      },
             |      "borderModeOfTransport" : {
             |        "code": "1",
             |        "description": "Maritime Transport"
             |      },
             |      "carrierDetails" : {
             |        "identificationNumber" : "carrier1",
             |        "addContactYesNo" : true,
             |        "contact" : {
             |          "name" : "Carrier Contact",
             |          "telephoneNumber" : "+44 808 157 0192"
             |        }
             |      },
             |      "transportMeans" : {
             |        "departure" : [
             |          {
             |            "identification" : {
             |              "type": "10",
             |              "description": "IMO Ship Identification Number"
             |            },
             |            "meansIdentificationNumber" : "means id number",
             |            "vehicleCountry" : {
             |              "code" : "FR",
             |              "desc" : "France"
             |            }
             |          }
             |        ],
             |        "active" : [
             |          {
             |            "identification" : {
             |              "code": "11",
             |              "description": "Name of the sea-going vessel"
             |            },
             |            "identificationNumber" : "active id number",
             |            "customsOfficeActiveBorder" : {
             |              "id" : "IT018101",
             |              "name" : "Aeroporto Bari - Palese",
             |              "phoneNumber" : "0039 0805316196"
             |            },
             |            "nationality" : {
             |              "code" : "ES",
             |              "desc" : "Spain"
             |            },
             |            "conveyanceReferenceNumber" : "conveyance ref number"
             |          }
             |        ]
             |      },
             |      "supplyChainActors" : [
             |        {
             |          "supplyChainActorType" : {
             |            "role": "CS",
             |            "description": "Consolidator"
             |          },
             |          "identificationNumber" : "sca1"
             |        },
             |        {
             |          "supplyChainActorType" : {
             |            "role": "FW",
             |            "description": "Freight Forwarder"
             |          },
             |          "identificationNumber" : "sca2"
             |        },
             |        {
             |          "supplyChainActorType" : {
             |            "role": "MF",
             |            "description": "Manufacturer"
             |          },
             |          "identificationNumber" : "sca3"
             |        },
             |        {
             |          "supplyChainActorType" : {
             |            "role": "WH",
             |            "description": "Warehouse Keeper"
             |          },
             |          "identificationNumber" : "sca4"
             |        }
             |      ],
             |      "equipmentsAndCharges" : {
             |        "equipments" : [
             |          {
             |            "containerIdentificationNumber" : "container id 1",
             |            "seals" : [
             |              {
             |                "identificationNumber" : "seal 1"
             |              },
             |              {
             |                "identificationNumber" : "seal 2"
             |              }
             |            ],
             |            "uuid" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
             |          }
             |        ],
             |        "paymentMethod" : {
             |          "method": "A",
             |          "description": "Payment in cash"
             |        }
             |      }
             |    },
             |    "documents" : {
             |      "addDocumentsYesNo" : false
             |    },
             |    "items" : {
             |      "items" : [
             |        {
             |          "description" : "Description 1",
             |          "declarationType" : {
             |            "code": "T1",
             |            "description": "Goods not having the customs status of Union goods, which are placed under the common transit procedure."
             |          },
             |          "countryOfDispatch" : {
             |            "code" : "GB",
             |            "description" : "United Kingdom"
             |          },
             |          "countryOfDestination" : {
             |            "code" : "FR",
             |            "description" : "France"
             |          },
             |          "uniqueConsignmentReference" : "UCR 1",
             |          "customsUnionAndStatisticsCode" : "CUS code 1",
             |          "commodityCode" : "commodity code 1",
             |          "combinedNomenclatureCode" : "CN code 1",
             |          "dangerousGoodsList" : [
             |            {
             |              "unNumber" : "UN number 1_1"
             |            },
             |            {
             |              "unNumber" : "UN number 1_2"
             |            }
             |          ],
             |          "grossWeight" : 123.456,
             |          "netWeight" : 1234,
             |          "supplementaryUnits" : 12345,
             |          "methodOfPayment" : {
             |            "method" : "A",
             |            "description" : "Payment in cash"
             |          },
             |          "packages" : [
             |            {
             |              "packageType" : {
             |                "code" : "VL",
             |                "description" : "Bulk, liquid",
             |                "type" : "Bulk"
             |              },
             |              "addShippingMarkYesNo" : false
             |            },
             |            {
             |              "packageType" : {
             |                "code" : "NE",
             |                "description" : "Unpacked or unpackaged",
             |                "type" : "Unpacked"
             |              },
             |              "numberOfPackages" : 5,
             |              "addShippingMarkYesNo" : false
             |            },
             |            {
             |              "packageType" : {
             |                "code" : "TR",
             |                "description" : "Trunk",
             |                "type" : "Other"
             |              },
             |              "shippingMark" : "mark3"
             |            }
             |          ],
             |          "addSupplyChainActorYesNo" : true,
             |          "supplyChainActors" : [
             |            {
             |              "supplyChainActorType" : {
             |                "role": "CS",
             |                "description": "Consolidator"
             |              },
             |              "identificationNumber" : "itemSCA1"
             |            },
             |            {
             |              "supplyChainActorType" : {
             |                "role": "FW",
             |                "description": "Freight Forwarder"
             |              },
             |              "identificationNumber" : "itemSCA2"
             |            },
             |            {
             |              "supplyChainActorType" : {
             |                "role": "MF",
             |                "description": "Manufacturer"
             |              },
             |              "identificationNumber" : "itemSCA3"
             |            },
             |            {
             |              "supplyChainActorType" : {
             |                "role": "WH",
             |                "description": "Warehouse Keeper"
             |              },
             |              "identificationNumber" : "itemSCA4"
             |            }
             |          ],
             |          "addDocumentsYesNo" : false,
             |          "addAdditionalReferenceYesNo" : true,
             |          "additionalReferences" : [
             |            {
             |              "additionalReference" : {
             |                "documentType" : "ar1",
             |                "description" : "Additional reference 1"
             |              },
             |              "addAdditionalReferenceNumberYesNo" : true,
             |              "additionalReferenceNumber" : "arno1"
             |            },
             |            {
             |              "additionalReference" : {
             |                "documentType" : "ar2",
             |                "description" : "Additional reference 2"
             |              },
             |              "addAdditionalReferenceNumberYesNo" : false
             |            }
             |          ],
             |          "additionalInformationList" : [
             |            {
             |              "additionalInformationType" : {
             |                "code" : "aiCode1",
             |                "description" : "aiDescription1"
             |              },
             |              "additionalInformation" : "ai1"
             |            },
             |            {
             |              "additionalInformationType" : {
             |                "code" : "aiCode2",
             |                "description" : "aiDescription2"
             |              },
             |              "additionalInformation" : "ai2"
             |            }
             |          ],
             |          "transportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371",
             |          "consignee" : {
             |            "addConsigneeEoriNumberYesNo" : true,
             |            "identificationNumber" : "GE00101001",
             |            "name" : "Mr. Consignee",
             |            "country" : {
             |              "code" : "GB",
             |              "description" : "United Kingdom"
             |            },
             |            "address" : {
             |              "numberAndStreet" : "1 Merry Lane",
             |              "city" : "Godrics Hollow",
             |              "postalCode" : "CA1 9AA"
             |            }
             |          }
             |        },
             |        {
             |          "description" : "Description 2",
             |          "declarationType" : {
             |            "code": "T2",
             |            "description": "Goods having the customs status of Union goods, which are placed under the common transit procedure"
             |          },
             |          "countryOfDispatch" : {
             |            "code" : "DE",
             |            "description" : "Germany"
             |          },
             |          "countryOfDestination" : {
             |            "code" : "ES",
             |            "description" : "Spain"
             |          },
             |          "uniqueConsignmentReference" : "UCR 2",
             |          "customsUnionAndStatisticsCode" : "CUS code 2",
             |          "commodityCode" : "commodity code 2",
             |          "combinedNomenclatureCode" : "CN code 2",
             |          "dangerousGoodsList" : [
             |            {
             |              "unNumber" : "UN number 2_1"
             |            },
             |            {
             |              "unNumber" : "UN number 2_2"
             |            }
             |          ],
             |          "grossWeight" : 456.789,
             |          "methodOfPayment" : {
             |            "method" : "A",
             |            "description" : "Payment in cash"
             |          },
             |          "addSupplyChainActorYesNo" : false,
             |          "addDocumentsYesNo" : false,
             |          "addAdditionalReferenceYesNo" : true,
             |          "additionalReferences" : [
             |            {
             |              "additionalReference" : {
             |                "documentType" : "ar1",
             |                "description" : "Additional reference 1"
             |              },
             |              "addAdditionalReferenceNumberYesNo" : true,
             |              "additionalReferenceNumber" : "arno1"
             |            }
             |          ],
             |          "additionalInformationList" : [
             |            {
             |              "additionalInformationType" : {
             |                "code" : "aiCode1",
             |                "description" : "aiDescription1"
             |              },
             |              "additionalInformation" : "ai1"
             |            }
             |          ],
             |          "transportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
             |        }
             |      ]
             |    }
             |  },
             |  "tasks" : {},
             |  "createdAt" : "2022-09-05T15:58:44.188Z",
             |  "lastUpdated" : "2022-09-07T10:33:23.472Z"
             |}
             |""".stripMargin)

        val uA: UserAnswers = json.as[UserAnswers]

        val converted: ConsignmentType23 = Consignment.transform(uA)

        converted.countryOfDispatch shouldEqual Some("FR")
        converted.countryOfDestination shouldEqual Some("IT")
        converted.containerIndicator shouldEqual Some(Number1)
        converted.inlandModeOfTransport shouldEqual Some("1")
        converted.modeOfTransportAtTheBorder shouldEqual Some("1")
        converted.grossMass shouldEqual 580.245
        converted.referenceNumberUCR shouldEqual Some("ucr123")

        converted.Carrier shouldEqual Some(
          CarrierType06(
            identificationNumber = "carrier1",
            ContactPerson = Some(
              ContactPersonType03(
                name = "Carrier Contact",
                phoneNumber = "+44 808 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.Consignor shouldEqual Some(
          ConsignorType10(
            identificationNumber = Some("consignor1"),
            name = Some("Mr Consignor"),
            Address = Some(
              AddressType14(
                streetAndNumber = "21 Test Lane",
                postcode = Some("NE1 1NE"),
                city = "Newcastle upon Tyne",
                country = "GB"
              )
            ),
            ContactPerson = Some(
              ContactPersonType03(
                name = "Consignor Contact",
                phoneNumber = "+44 101 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.Consignee shouldEqual Some(
          ConsigneeType05(
            identificationNumber = Some("consignee1"),
            name = Some("Mr Consignee"),
            Address = Some(
              AddressType14(
                streetAndNumber = "21 Test Rue",
                postcode = Some("PA1 1PA"),
                city = "Paris",
                country = "FR"
              )
            )
          )
        )

        converted.AdditionalSupplyChainActor shouldEqual Seq(
          AdditionalSupplyChainActorType01(
            sequenceNumber = 1,
            role = "CS",
            identificationNumber = "sca1"
          ),
          AdditionalSupplyChainActorType01(
            sequenceNumber = 2,
            role = "FW",
            identificationNumber = "sca2"
          ),
          AdditionalSupplyChainActorType01(
            sequenceNumber = 3,
            role = "MF",
            identificationNumber = "sca3"
          ),
          AdditionalSupplyChainActorType01(
            sequenceNumber = 4,
            role = "WH",
            identificationNumber = "sca4"
          )
        )

        converted.TransportEquipment shouldEqual Seq(
          TransportEquipmentType03(
            sequenceNumber = 1,
            containerIdentificationNumber = Some("container id 1"),
            numberOfSeals = 2,
            Seal = Seq(
              SealType01(
                sequenceNumber = 1,
                identifier = "seal 1"
              ),
              SealType01(
                sequenceNumber = 2,
                identifier = "seal 2"
              )
            ),
            GoodsReference = Seq(
              GoodsReferenceType01(
                sequenceNumber = 1,
                declarationGoodsItemNumber = 1
              ),
              GoodsReferenceType01(
                sequenceNumber = 2,
                declarationGoodsItemNumber = 2
              )
            )
          )
        )

        converted.LocationOfGoods shouldEqual Some(
          LocationOfGoodsType04(
            typeOfLocation = "A",
            qualifierOfIdentification = "T",
            authorisationNumber = Some("authorisation number"),
            additionalIdentifier = Some("additional identifier"),
            UNLocode = Some("DEAAL"),
            CustomsOffice = Some(CustomsOfficeType02(referenceNumber = "XI000142")),
            GNSS = Some(
              GNSSType(
                latitude = "lat",
                longitude = "lon"
              )
            ),
            EconomicOperator = Some(EconomicOperatorType02(identificationNumber = "GB12345")),
            Address = Some(
              AddressType06(
                streetAndNumber = "21 Test Camino",
                postcode = Some("ES1 1SE"),
                city = "Madrid",
                country = "ES"
              )
            ),
            PostcodeAddress = None,
            ContactPerson = Some(
              ContactPersonType01(
                name = "Location of goods Contact",
                phoneNumber = "+44 202 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.DepartureTransportMeans shouldEqual Seq(
          DepartureTransportMeansType01(
            sequenceNumber = 1,
            typeOfIdentification = "10",
            identificationNumber = "means id number",
            nationality = "FR"
          )
        )

        converted.CountryOfRoutingOfConsignment shouldEqual Seq(
          CountryOfRoutingOfConsignmentType02(
            sequenceNumber = 1,
            country = "AD"
          ),
          CountryOfRoutingOfConsignmentType02(
            sequenceNumber = 2,
            country = "AR"
          )
        )

        converted.ActiveBorderTransportMeans shouldEqual Seq(
          ActiveBorderTransportMeansType03(
            sequenceNumber = 1,
            customsOfficeAtBorderReferenceNumber = "IT018101",
            typeOfIdentification = "11",
            identificationNumber = "active id number",
            nationality = "ES",
            conveyanceReferenceNumber = Some("conveyance ref number")
          )
        )

        converted.PlaceOfLoading shouldEqual Some(
          PlaceOfLoadingType(
            Some("AEFAT"),
            Some("Loading country"),
            Some("Loading location")
          )
        )

        converted.PlaceOfUnloading shouldEqual Some(
          PlaceOfUnloadingType(
            Some("ADALV"),
            Some("Unloading country"),
            Some("Unloading location")
          )
        )

        converted.PreviousDocument shouldEqual Seq.empty

        converted.SupportingDocument shouldEqual Seq.empty

        converted.TransportDocument shouldEqual Seq.empty

        converted.AdditionalReference shouldEqual Seq(
          AdditionalReferenceType02(
            sequenceNumber = 1,
            typeValue = "type123",
            referenceNumber = Some("ARNO1")
          ),
          AdditionalReferenceType02(
            sequenceNumber = 2,
            typeValue = "type321",
            referenceNumber = Some("1ONRA")
          )
        )

        converted.AdditionalInformation shouldEqual Seq(
          AdditionalInformationType02(
            sequenceNumber = 1,
            code = "ADDINFTYPE1",
            text = Some("ORCA")
          ),
          AdditionalInformationType02(
            sequenceNumber = 2,
            code = "ADDINFTYPE2",
            text = Some("ACRO")
          )
        )

        converted.TransportCharges shouldEqual Some(
          TransportChargesType01("A")
        )

        converted.HouseConsignment.size shouldEqual 1
        val output = converted.HouseConsignment.head

        val expected = HouseConsignmentType13(
          sequenceNumber = 1,
          countryOfDispatch = None,
          grossMass = 580.245,
          referenceNumberUCR = None,
          Consignor = None,
          Consignee = None,
          AdditionalSupplyChainActor = Nil,
          DepartureTransportMeans = Nil,
          PreviousDocument = Nil,
          SupportingDocument = Nil,
          TransportDocument = Nil,
          AdditionalReference = Nil,
          AdditionalInformation = Nil,
          TransportCharges = None,
          ConsignmentItem = Seq(
            ConsignmentItemType10(
              goodsItemNumber = 1,
              declarationGoodsItemNumber = 1,
              declarationType = Some("T1"),
              countryOfDispatch = Some("GB"),
              countryOfDestination = Some("FR"),
              referenceNumberUCR = Some("UCR 1"),
              AdditionalSupplyChainActor = Seq(
                AdditionalSupplyChainActorType01(
                  sequenceNumber = 1,
                  role = "CS",
                  identificationNumber = "itemSCA1"
                ),
                AdditionalSupplyChainActorType01(
                  sequenceNumber = 2,
                  role = "FW",
                  identificationNumber = "itemSCA2"
                ),
                AdditionalSupplyChainActorType01(
                  sequenceNumber = 3,
                  role = "MF",
                  identificationNumber = "itemSCA3"
                ),
                AdditionalSupplyChainActorType01(
                  sequenceNumber = 4,
                  role = "WH",
                  identificationNumber = "itemSCA4"
                )
              ),
              Commodity = CommodityType10(
                descriptionOfGoods = "Description 1",
                cusCode = Some("CUS code 1"),
                CommodityCode = Some(
                  CommodityCodeType04(
                    harmonizedSystemSubHeadingCode = "commodity code 1",
                    combinedNomenclatureCode = Some("CN code 1")
                  )
                ),
                DangerousGoods = Seq(
                  DangerousGoodsType01(
                    sequenceNumber = 1,
                    UNNumber = "UN number 1_1"
                  ),
                  DangerousGoodsType01(
                    sequenceNumber = 2,
                    UNNumber = "UN number 1_2"
                  )
                ),
                GoodsMeasure = GoodsMeasureType04(
                  grossMass = BigDecimal(123.456),
                  netMass = Some(BigDecimal(1234)),
                  supplementaryUnits = Some(BigDecimal(12345))
                )
              ),
              Packaging = Seq(
                PackagingType01(
                  sequenceNumber = 1,
                  typeOfPackages = "VL",
                  numberOfPackages = None,
                  shippingMarks = None
                ),
                PackagingType01(
                  sequenceNumber = 2,
                  typeOfPackages = "NE",
                  numberOfPackages = Some(5),
                  shippingMarks = None
                ),
                PackagingType01(
                  sequenceNumber = 3,
                  typeOfPackages = "TR",
                  numberOfPackages = None,
                  shippingMarks = Some("mark3")
                )
              ),
              PreviousDocument = Seq.empty,
              SupportingDocument = Seq.empty,
              AdditionalReference = Seq(
                AdditionalReferenceType01(
                  sequenceNumber = 1,
                  typeValue = "ar1",
                  referenceNumber = Some("arno1")
                ),
                AdditionalReferenceType01(
                  sequenceNumber = 2,
                  typeValue = "ar2",
                  referenceNumber = None
                )
              ),
              AdditionalInformation = Seq(
                AdditionalInformationType02(
                  sequenceNumber = 1,
                  code = "aiCode1",
                  text = Some("ai1")
                ),
                AdditionalInformationType02(
                  sequenceNumber = 2,
                  code = "aiCode2",
                  text = Some("ai2")
                )
              )
            ),
            ConsignmentItemType10(
              goodsItemNumber = 2,
              declarationGoodsItemNumber = 2,
              declarationType = Some("T2"),
              countryOfDispatch = Some("DE"),
              countryOfDestination = Some("ES"),
              referenceNumberUCR = Some("UCR 2"),
              AdditionalSupplyChainActor = Nil,
              Commodity = CommodityType10(
                descriptionOfGoods = "Description 2",
                cusCode = Some("CUS code 2"),
                CommodityCode = Some(
                  CommodityCodeType04(
                    harmonizedSystemSubHeadingCode = "commodity code 2",
                    combinedNomenclatureCode = Some("CN code 2")
                  )
                ),
                DangerousGoods = Seq(
                  DangerousGoodsType01(
                    sequenceNumber = 1,
                    UNNumber = "UN number 2_1"
                  ),
                  DangerousGoodsType01(
                    sequenceNumber = 2,
                    UNNumber = "UN number 2_2"
                  )
                ),
                GoodsMeasure = GoodsMeasureType04(
                  grossMass = BigDecimal(456.789),
                  netMass = None,
                  supplementaryUnits = None
                )
              ),
              Packaging = Nil,
              PreviousDocument = Nil,
              SupportingDocument = Nil,
              AdditionalReference = Seq(
                AdditionalReferenceType01(
                  sequenceNumber = 1,
                  typeValue = "ar1",
                  referenceNumber = Some("arno1")
                )
              ),
              AdditionalInformation = Seq(
                AdditionalInformationType02(
                  sequenceNumber = 1,
                  code = "aiCode1",
                  text = Some("ai1")
                )
              )
            )
          )
        )

        output shouldEqual expected
      }
    }

    "postProcess is called" when {
      "rollUpUCR" when {

        "every item has the same UCR" must {
          "roll up UCR to consignment level and remove them from each item" in {
            val consignment = ConsignmentType23(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      referenceNumberUCR = Some("UCR")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      referenceNumberUCR = Some("UCR")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess()

            result shouldEqual ConsignmentType23(
              grossMass = BigDecimal(1),
              referenceNumberUCR = Some("UCR"),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      referenceNumberUCR = None
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      referenceNumberUCR = None
                    )
                  )
                )
              )
            )
          }
        }

        "some items have the same UCR" must {
          "not roll up UCR to consignment level" in {
            val consignment = ConsignmentType23(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      referenceNumberUCR = Some("UCR")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      referenceNumberUCR = Some("UCR")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 3,
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 3",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(3)
                        )
                      ),
                      referenceNumberUCR = None
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess()

            result shouldEqual consignment
          }
        }

        "no items have the same UCR" must {
          "not roll up UCR to consignment level" in {
            val consignment = ConsignmentType23(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      referenceNumberUCR = Some("UCR1")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      referenceNumberUCR = Some("UCR2")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 3,
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 3",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(3)
                        )
                      ),
                      referenceNumberUCR = Some("UCR3")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess()

            result shouldEqual consignment
          }
        }
      }

      "rollUpCountryOfDispatch" when {

        "every item has the same country of dispatch" must {
          "roll up country of dispatch to consignment level and remove them from each item" in {
            val consignment = ConsignmentType23(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      countryOfDispatch = Some("GB")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      countryOfDispatch = Some("GB")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess()

            result shouldEqual ConsignmentType23(
              grossMass = BigDecimal(1),
              countryOfDispatch = Some("GB"),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      countryOfDispatch = None
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      countryOfDispatch = None
                    )
                  )
                )
              )
            )
          }
        }

        "some items have the same country of dispatch" must {
          "not roll up country of dispatch to consignment level" in {
            val consignment = ConsignmentType23(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      countryOfDispatch = Some("GB")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      countryOfDispatch = Some("GB")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 3,
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 3",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(3)
                        )
                      ),
                      countryOfDispatch = None
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess()

            result shouldEqual consignment
          }
        }

        "no items have the same country of dispatch" must {
          "not roll up country of dispatch to consignment level" in {
            val consignment = ConsignmentType23(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      countryOfDispatch = Some("GB")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      countryOfDispatch = Some("FR")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 3,
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 3",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(3)
                        )
                      ),
                      countryOfDispatch = Some("ES")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess()

            result shouldEqual consignment
          }
        }
      }

      "rollUpCountryOfDestination" when {

        "every item has the same country of destination" must {
          "roll up country of destination to consignment level and remove them from each item" in {
            val consignment = ConsignmentType23(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      countryOfDestination = Some("GB")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      countryOfDestination = Some("GB")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess()

            result shouldEqual ConsignmentType23(
              grossMass = BigDecimal(1),
              countryOfDestination = Some("GB"),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      countryOfDestination = None
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      countryOfDestination = None
                    )
                  )
                )
              )
            )
          }
        }

        "some items have the same country of destination" must {
          "not roll up country of destination to consignment level" in {
            val consignment = ConsignmentType23(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      countryOfDestination = Some("GB")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      countryOfDestination = Some("GB")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 3,
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 3",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(3)
                        )
                      ),
                      countryOfDestination = None
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess()

            result shouldEqual consignment
          }
        }

        "no items have the same country of dispatch" must {
          "not roll up country of dispatch to consignment level" in {
            val consignment = ConsignmentType23(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType13(
                  sequenceNumber = 1,
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType10(
                      goodsItemNumber = 1,
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 1",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(1)
                        )
                      ),
                      countryOfDispatch = Some("GB")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 2,
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 2",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(2)
                        )
                      ),
                      countryOfDispatch = Some("FR")
                    ),
                    ConsignmentItemType10(
                      goodsItemNumber = 3,
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType10(
                        descriptionOfGoods = "Item 3",
                        GoodsMeasure = GoodsMeasureType04(
                          grossMass = BigDecimal(3)
                        )
                      ),
                      countryOfDispatch = Some("ES")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess()

            result shouldEqual consignment
          }
        }
      }
    }

    "activeBorderTransportMeansType03 reads is called" when {

      "identification is not inferred" in {
        val json: JsValue = Json.parse(s"""
            |{
            |  "transportDetails" : {
            |    "transportMeans" : {
            |      "active" : [
            |        {
            |          "identification" : {
            |            "code": "10",
            |            "description": "IMO Ship Identification Number"
            |          },
            |          "identificationNumber" : "active id number",
            |          "customsOfficeActiveBorder" : {
            |            "id" : "IT018101",
            |            "name" : "Aeroporto Bari - Palese",
            |            "phoneNumber" : "0039 0805316196"
            |          },
            |          "nationality" : {
            |            "code" : "ES",
            |            "desc" : "Spain"
            |          },
            |          "conveyanceReferenceNumber" : "conveyance ref number"
            |        }
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin)

        val result: Seq[ActiveBorderTransportMeansType03] =
          json.as[Seq[ActiveBorderTransportMeansType03]](activeBorderTransportMeansReads)

        result shouldEqual Seq(
          ActiveBorderTransportMeansType03(
            sequenceNumber = 1,
            customsOfficeAtBorderReferenceNumber = "IT018101",
            typeOfIdentification = "10",
            identificationNumber = "active id number",
            nationality = "ES",
            conveyanceReferenceNumber = Some("conveyance ref number")
          )
        )
      }

      "identification is inferred" in {
        val json: JsValue = Json.parse(s"""
            |{
            |  "transportDetails" : {
            |    "transportMeans" : {
            |      "active" : [
            |        {
            |          "inferredIdentification" : {
            |            "code": "21",
            |            "description": "Train Number"
            |          },
            |          "identificationNumber" : "active id number",
            |          "customsOfficeActiveBorder" : {
            |            "id" : "IT018101",
            |            "name" : "Aeroporto Bari - Palese",
            |            "phoneNumber" : "0039 0805316196"
            |          },
            |          "nationality" : {
            |            "code" : "ES",
            |            "desc" : "Spain"
            |          },
            |          "conveyanceReferenceNumber" : "conveyance ref number"
            |        }
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin)

        val result: Seq[ActiveBorderTransportMeansType03] =
          json.as[Seq[ActiveBorderTransportMeansType03]](activeBorderTransportMeansReads)

        result shouldEqual Seq(
          ActiveBorderTransportMeansType03(
            sequenceNumber = 1,
            customsOfficeAtBorderReferenceNumber = "IT018101",
            typeOfIdentification = "21",
            identificationNumber = "active id number",
            nationality = "ES",
            conveyanceReferenceNumber = Some("conveyance ref number")
          )
        )
      }
    }

    "locationOfGoodsType04 reads is called" when {
      "type of location and qualifier of identification is not inferred" in {
        val json = Json.parse(s"""
             |{
             |  "typeOfLocation" : {
             |    "type": "C",
             |    "description": "Approved place"
             |  },
             |  "qualifierOfIdentification" : {
             |    "qualifier": "U",
             |    "description": "UN/LOCODE"
             |  },
             |  "identifier" : {
             |    "unLocode" : "UNLOCODE",
             |    "addContact" : false
             |  }
             |}
             |""".stripMargin)

        val result = json.as[LocationOfGoodsType04](locationOfGoodsType04.reads)

        result shouldEqual LocationOfGoodsType04(
          typeOfLocation = "C",
          qualifierOfIdentification = "U",
          authorisationNumber = None,
          additionalIdentifier = None,
          UNLocode = Some("UNLOCODE"),
          CustomsOffice = None,
          GNSS = None,
          EconomicOperator = None,
          Address = None,
          PostcodeAddress = None,
          ContactPerson = None
        )
      }

      "type of location and qualifier of identification is inferred" in {
        val json = Json.parse(s"""
             |{
             |  "inferredTypeOfLocation" : {
             |    "type": "B",
             |    "description": "Authorised place"
             |  },
             |  "inferredQualifierOfIdentification" : {
             |    "qualifier": "Y",
             |    "description": "Authorisation number"
             |  },
             |  "identifier" : {
             |    "authorisationNumber" : "authorisation number",
             |    "addAdditionalIdentifier" : false,
             |    "addContact" : false
             |  }
             |}
             |""".stripMargin)

        val result = json.as[LocationOfGoodsType04](locationOfGoodsType04.reads)

        result shouldEqual LocationOfGoodsType04(
          typeOfLocation = "B",
          qualifierOfIdentification = "Y",
          authorisationNumber = Some("authorisation number"),
          additionalIdentifier = None,
          UNLocode = None,
          CustomsOffice = None,
          GNSS = None,
          EconomicOperator = None,
          Address = None,
          PostcodeAddress = None,
          ContactPerson = None
        )
      }
    }

    "transportEquipmentReads is called" when {
      "there are no transport equipments" in {
        val json = Json.parse("""
            |{
            |  "transportDetails" : {
            |    "addTransportEquipmentYesNo" : false
            |  },
            |  "items" : {
            |    "items" : [
            |      {},
            |      {},
            |      {}
            |    ]
            |  }
            |}
            |""".stripMargin)

        val result = json.as[Seq[TransportEquipmentType03]](transportEquipmentReads)

        result shouldEqual Nil
      }

      "there is one transport equipment" in {
        val json = Json.parse("""
            |{
            |  "transportDetails" : {
            |    "addTransportEquipmentYesNo" : true,
            |    "equipmentsAndCharges" : {
            |      "equipments" : [
            |        {
            |          "containerIdentificationNumber" : "container id 1",
            |          "seals" : [
            |            {
            |              "identificationNumber" : "seal 1"
            |            },
            |            {
            |              "identificationNumber" : "seal 2"
            |            }
            |          ],
            |          "uuid" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
            |        }
            |      ]
            |    }
            |  },
            |  "items" : {
            |    "items" : [
            |      {
            |        "transportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
            |      },
            |      {
            |        "transportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
            |      },
            |      {
            |        "transportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
            |      }
            |    ]
            |  }
            |}
            |""".stripMargin)

        val result = json.as[Seq[TransportEquipmentType03]](transportEquipmentReads)

        result shouldEqual Seq(
          TransportEquipmentType03(
            sequenceNumber = 1,
            containerIdentificationNumber = Some("container id 1"),
            numberOfSeals = BigInt(2),
            Seal = Seq(
              SealType01(1, "seal 1"),
              SealType01(2, "seal 2")
            ),
            GoodsReference = Seq(
              GoodsReferenceType01(1, 1),
              GoodsReferenceType01(2, 2),
              GoodsReferenceType01(3, 3)
            )
          )
        )
      }

      "there are multiple transport equipments" when {
        "all have been attached to an item" in {
          val json = Json.parse("""
              |{
              |  "transportDetails" : {
              |    "addTransportEquipmentYesNo" : true,
              |    "equipmentsAndCharges" : {
              |      "equipments" : [
              |        {
              |          "containerIdentificationNumber" : "container id 1",
              |          "seals" : [],
              |          "uuid" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
              |        },
              |        {
              |          "containerIdentificationNumber" : "container id 2",
              |          "seals" : [],
              |          "uuid" : "b8f72766-3781-49f2-8788-db8913d41f8c"
              |        },
              |        {
              |          "containerIdentificationNumber" : "container id 3",
              |          "seals" : [],
              |          "uuid" : "00602057-2652-43f4-8fe5-d97460d708ec"
              |        }
              |      ]
              |    }
              |  },
              |  "items" : {
              |    "items" : [
              |      {
              |        "transportEquipment" : "00602057-2652-43f4-8fe5-d97460d708ec"
              |      },
              |      {
              |        "transportEquipment" : "b8f72766-3781-49f2-8788-db8913d41f8c"
              |      },
              |      {
              |        "transportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
              |      },
              |      {
              |        "transportEquipment" : "00602057-2652-43f4-8fe5-d97460d708ec"
              |      }
              |    ]
              |  }
              |}
              |""".stripMargin)

          val result = json.as[Seq[TransportEquipmentType03]](transportEquipmentReads)

          result shouldEqual Seq(
            TransportEquipmentType03(
              sequenceNumber = 1,
              containerIdentificationNumber = Some("container id 1"),
              numberOfSeals = BigInt(0),
              Seal = Nil,
              GoodsReference = Seq(
                GoodsReferenceType01(1, 3)
              )
            ),
            TransportEquipmentType03(
              sequenceNumber = 2,
              containerIdentificationNumber = Some("container id 2"),
              numberOfSeals = BigInt(0),
              Seal = Nil,
              GoodsReference = Seq(
                GoodsReferenceType01(1, 2)
              )
            ),
            TransportEquipmentType03(
              sequenceNumber = 3,
              containerIdentificationNumber = Some("container id 3"),
              numberOfSeals = BigInt(0),
              Seal = Nil,
              GoodsReference = Seq(
                GoodsReferenceType01(1, 1),
                GoodsReferenceType01(2, 4)
              )
            )
          )
        }

        "some have not been attached to an item" in {
          val json = Json.parse("""
              |{
              |  "transportDetails" : {
              |    "addTransportEquipmentYesNo" : true,
              |    "equipmentsAndCharges" : {
              |      "equipments" : [
              |        {
              |          "containerIdentificationNumber" : "container id 1",
              |          "seals" : [],
              |          "uuid" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
              |        },
              |        {
              |          "containerIdentificationNumber" : "container id 2",
              |          "seals" : [],
              |          "uuid" : "b8f72766-3781-49f2-8788-db8913d41f8c"
              |        },
              |        {
              |          "containerIdentificationNumber" : "container id 3",
              |          "seals" : [],
              |          "uuid" : "00602057-2652-43f4-8fe5-d97460d708ec"
              |        }
              |      ]
              |    }
              |  },
              |  "items" : {
              |    "items" : [
              |      {
              |        "transportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
              |      },
              |      {
              |        "transportEquipment" : "00602057-2652-43f4-8fe5-d97460d708ec"
              |      }
              |    ]
              |  }
              |}
              |""".stripMargin)

          val result = json.as[Seq[TransportEquipmentType03]](transportEquipmentReads)

          result shouldEqual Seq(
            TransportEquipmentType03(
              sequenceNumber = 1,
              containerIdentificationNumber = Some("container id 1"),
              numberOfSeals = BigInt(0),
              Seal = Nil,
              GoodsReference = Seq(
                GoodsReferenceType01(1, 1)
              )
            ),
            TransportEquipmentType03(
              sequenceNumber = 2,
              containerIdentificationNumber = Some("container id 3"),
              numberOfSeals = BigInt(0),
              Seal = Nil,
              GoodsReference = Seq(
                GoodsReferenceType01(1, 2)
              )
            )
          )
        }
      }
    }

    "commodityType10 reads is called" when {
      "commodity code defined" in {
        val json = Json.parse("""
            |{
            |  "description" : "Description",
            |  "commodityCode" : "commodity code",
            |  "combinedNomenclatureCode" : "CN code",
            |  "grossWeight" : 123.456
            |}
            |""".stripMargin)

        val result = json.as[CommodityType10](commodityType10.reads)

        result shouldEqual CommodityType10(
          descriptionOfGoods = "Description",
          CommodityCode = Some(
            CommodityCodeType04(
              harmonizedSystemSubHeadingCode = "commodity code",
              combinedNomenclatureCode = Some("CN code")
            )
          ),
          GoodsMeasure = GoodsMeasureType04(
            grossMass = BigDecimal(123.456)
          )
        )
      }

      "commodity code undefined" in {
        val json = Json.parse("""
            |{
            |  "description" : "Description",
            |  "grossWeight" : 123.456
            |}
            |""".stripMargin)

        val result = json.as[CommodityType10](commodityType10.reads)

        result shouldEqual CommodityType10(
          descriptionOfGoods = "Description",
          CommodityCode = None,
          GoodsMeasure = GoodsMeasureType04(
            grossMass = BigDecimal(123.456)
          )
        )
      }
    }

    "departureTransportMeansReads is called" when {
      "there are no departure transport means" in {
        val json = Json.parse("""
            |{
            |  "transportDetails" : {}
            |}
            |""".stripMargin)

        val result = json.as[Seq[DepartureTransportMeansType01]](consignmentType23.departureTransportMeansReads)

        result shouldEqual Nil
      }

      "there is a departure transport means" in {
        val json = Json.parse("""
            |{
            |  "transportDetails" : {
            |    "transportMeans" : {
            |      "departure" : [
            |        {
            |          "identification" : {
            |            "type": "10",
            |            "description": "IMO Ship Identification Number"
            |          },
            |          "meansIdentificationNumber" : "means id number",
            |          "vehicleCountry" : {
            |            "code" : "FR",
            |            "desc" : "France"
            |          }
            |        }
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin)

        val result = json.as[Seq[DepartureTransportMeansType01]](consignmentType23.departureTransportMeansReads)

        result shouldEqual Seq(
          DepartureTransportMeansType01(
            sequenceNumber = 1,
            typeOfIdentification = "10",
            identificationNumber = "means id number",
            nationality = "FR"
          )
        )
      }
    }
  }

  private def getUserAnswersJson(inlandMode: String) = {
    Json.parse(s"""
         |{
         |  "_id" : "$uuid",
         |  "lrn" : "$lrn",
         |  "eoriNumber" : "$eoriNumber",
         |  "isSubmitted" : "notSubmitted",
         |  "isTransitional": false,
         |  "data" : {
         |    "traderDetails" : {
         |      "consignment" : {
         |        "consignor" : {
         |          "eori" : "consignor1",
         |          "name" : "Mr Consignor",
         |          "country" : {
         |            "code" : "GB",
         |            "description" : "United Kingdom"
         |          },
         |          "address" : {
         |            "numberAndStreet" : "21 Test Lane",
         |            "city" : "Newcastle upon Tyne",
         |            "postalCode" : "NE1 1NE"
         |          },
         |          "contact" : {
         |            "name" : "Consignor Contact",
         |            "telephoneNumber" : "+44 101 157 0192"
         |          }
         |        },
         |        "consignee" : {
         |          "eori" : "consignee1",
         |          "name" : "Mr Consignee",
         |          "country" : {
         |            "code" : "FR",
         |            "description" : "France"
         |          },
         |          "address" : {
         |            "numberAndStreet" : "21 Test Rue",
         |            "city" : "Paris",
         |            "postalCode" : "PA1 1PA"
         |          }
         |        }
         |      }
         |    },
         |    "routeDetails" : {
         |      "routing" : {
         |        "countriesOfRouting" : [
         |          {
         |            "countryOfRouting" : {
         |              "code" : "AD",
         |              "description" : "Andorra"
         |            }
         |          },
         |          {
         |            "countryOfRouting" : {
         |              "code" : "AR",
         |              "description" : "Argentina"
         |            }
         |          }
         |        ]
         |      },
         |      "locationOfGoods" : {
         |        "typeOfLocation" : {
         |          "type": "A",
         |          "description": "Designated location"
         |        },
         |        "qualifierOfIdentification" : {
         |          "qualifier": "T",
         |          "description": "Postal code"
         |        },
         |        "identifier" : {
         |          "authorisationNumber" : "authorisation number",
         |          "additionalIdentifier" : "additional identifier",
         |          "unLocode" : "DEAAL",
         |          "customsOffice" : {
         |            "id" : "XI000142",
         |            "name" : "Belfast EPU",
         |            "phoneNumber" : "+44 (0)02896 931537"
         |          },
         |          "coordinates" : {
         |            "latitude" : "lat",
         |            "longitude" : "lon"
         |          },
         |          "eori" : "GB12345",
         |          "country" : {
         |            "code" : "ES",
         |            "description" : "Spain"
         |          },
         |          "address" : {
         |            "numberAndStreet" : "21 Test Camino",
         |            "city" : "Madrid",
         |            "postalCode" : "ES1 1SE"
         |          },
         |          "postalCode" : {
         |            "streetNumber" : "21",
         |            "postalCode" : "DE1 1DE",
         |            "country" : {
         |              "code" : "DE",
         |              "description" : "Germany"
         |            }
         |          }
         |        },
         |        "contact" : {
         |          "name" : "Location of goods Contact",
         |          "telephoneNumber" : "+44 202 157 0192"
         |        }
         |      },
         |      "loadingAndUnloading" : {
         |        "loading" : {
         |          "unLocode" : "AEFAT",
         |          "additionalInformation" : {
         |            "country" : {
         |              "code" : "Loading country",
         |              "description" : "United Kingdom"
         |            },
         |            "location" : "Loading location"
         |          }
         |        },
         |        "unloading" : {
         |          "unLocode" : "ADALV",
         |          "additionalInformation" : {
         |            "country" : {
         |              "code" : "Unloading country",
         |              "description" : "United Kingdom"
         |            },
         |            "location" : "Unloading location"
         |          }
         |        }
         |      }
         |    },
         |    "transportDetails" : {
         |      "additionalReference" : [
         |        {
         |          "type" : {
         |            "documentType" : "type123"
         |          },
         |          "additionalReferenceNumber" : "arno1"
         |        },
         |        {
         |          "type" : {
         |            "documentType" : "type321"
         |          },
         |          "additionalReferenceNumber" : "1onra"
         |        }
         |      ],
         |      "additionalInformation" : [
         |        {
         |          "type" : {
         |            "code" : "adinftype1"
         |          },
         |          "text" : "orca"
         |        },
         |        {
         |          "type" : {
         |            "code" : "adinftype2"
         |          },
         |          "text" : "acro"
         |        }
         |      ],
         |      "preRequisites" : {
         |        "uniqueConsignmentReference" : "ucr123",
         |        "countryOfDispatch" : {
         |          "code" : "FR",
         |          "description" : "France"
         |        },
         |        "itemsDestinationCountry" : {
         |          "code" : "IT",
         |          "description" : "Italy"
         |        },
         |        "containerIndicator" : true
         |      },
         |      "inlandMode" : {
         |        "code": "$inlandMode",
         |        "description": "Maritime Transport"
         |      },
         |      "borderModeOfTransport" : {
         |        "code": "1",
         |        "description": "Maritime Transport"
         |      },
         |      "carrierDetails" : {
         |        "identificationNumber" : "carrier1",
         |        "addContactYesNo" : true,
         |        "contact" : {
         |          "name" : "Carrier Contact",
         |          "telephoneNumber" : "+44 808 157 0192"
         |        }
         |      },
         |      "transportMeans" : {
         |        "departure" : [
         |          {
         |            "identification" : {
         |              "type": "10",
         |              "description": "IMO Ship Identification Number"
         |            },
         |            "meansIdentificationNumber" : "means id number",
         |            "vehicleCountry" : {
         |              "code" : "FR",
         |              "desc" : "France"
         |            }
         |          }
         |        ],
         |        "active" : [
         |          {
         |            "identification" : {
         |              "code": "11",
         |              "description": "Name of the sea-going vessel"
         |            },
         |            "identificationNumber" : "active id number",
         |            "customsOfficeActiveBorder" : {
         |              "id" : "IT018101",
         |              "name" : "Aeroporto Bari - Palese",
         |              "phoneNumber" : "0039 0805316196"
         |            },
         |            "nationality" : {
         |              "code" : "ES",
         |              "desc" : "Spain"
         |            },
         |            "conveyanceReferenceNumber" : "conveyance ref number"
         |          }
         |        ]
         |      },
         |      "supplyChainActors" : [
         |        {
         |          "supplyChainActorType" : {
         |            "role": "CS",
         |            "description": "Consolidator"
         |          },
         |          "identificationNumber" : "sca1"
         |        },
         |        {
         |          "supplyChainActorType" : {
         |            "role": "FW",
         |            "description": "Freight Forwarder"
         |          },
         |          "identificationNumber" : "sca2"
         |        },
         |        {
         |          "supplyChainActorType" : {
         |            "role": "MF",
         |            "description": "Manufacturer"
         |          },
         |          "identificationNumber" : "sca3"
         |        },
         |        {
         |          "supplyChainActorType" : {
         |            "role": "WH",
         |            "description": "Warehouse Keeper"
         |          },
         |          "identificationNumber" : "sca4"
         |        }
         |      ],
         |      "equipmentsAndCharges" : {
         |        "equipments" : [
         |          {
         |            "containerIdentificationNumber" : "container id 1",
         |            "seals" : [
         |              {
         |                "identificationNumber" : "seal 1"
         |              },
         |              {
         |                "identificationNumber" : "seal 2"
         |              }
         |            ],
         |            "uuid" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
         |          }
         |        ],
         |        "paymentMethod" : {
         |          "method": "A",
         |          "description": "Payment in cash"
         |        }
         |      }
         |    },
         |    "documents" : {
         |      "addDocumentsYesNo" : true,
         |      "documents" : [
         |        {
         |          "attachToAllItems" : false,
         |          "previousDocumentType" : {
         |            "type" : "Previous",
         |            "code" : "CO",
         |            "description" : "SAD - Community goods subject"
         |          },
         |          "details" : {
         |            "documentReferenceNumber" : "previous1",
         |            "uuid" : "ac50154c-cad1-4320-8def-d282eea63b2e",
         |            "addTypeOfPackageYesNo" : false,
         |            "addNumberOfPackagesYesNo" : false,
         |            "declareQuantityOfGoodsYesNo" : false,
         |            "addAdditionalInformationYesNo" : false
         |          }
         |        },
         |        {
         |          "attachToAllItems" : false,
         |          "type" : {
         |            "type" : "Transport",
         |            "code" : "235",
         |            "description" : "Container list"
         |          },
         |          "details" : {
         |            "documentReferenceNumber" : "transport1",
         |            "uuid" : "1794d93b-17d5-44fe-a18d-aaa2059d06fe"
         |          }
         |        },
         |        {
         |          "inferredAttachToAllItems" : false,
         |          "type" : {
         |            "type" : "Support",
         |            "code" : "C673",
         |            "description" : "Catch certificate"
         |          },
         |          "details" : {
         |            "documentReferenceNumber" : "support1",
         |            "uuid" : "a573bfd3-6470-40c4-a290-ea2d8d43c02a",
         |            "addLineItemNumberYesNo" : true,
         |            "lineItemNumber" : 678,
         |            "addAdditionalInformationYesNo" : true,
         |            "additionalInformation" : "complement of information support1"
         |          }
         |        },
         |        {
         |          "attachToAllItems" : false,
         |          "type" : {
         |            "type" : "Previous",
         |            "code" : "T2F",
         |            "description" : "Internal Community transit Declaration"
         |          },
         |          "details" : {
         |            "documentReferenceNumber" : "previous2",
         |            "uuid" : "3882459f-b7bc-478d-9d24-359533aa8fe3",
         |            "addTypeOfPackageYesNo" : true,
         |            "packageType" : {
         |              "code" : "AT",
         |              "description" : "Atomizer"
         |            },
         |            "addNumberOfPackagesYesNo" : true,
         |            "numberOfPackages" : 12,
         |            "declareQuantityOfGoodsYesNo" : true,
         |            "metric" : {
         |              "code" : "MIL",
         |              "description" : "1000 items"
         |            },
         |            "quantity" : 13,
         |            "addAdditionalInformationYesNo" : true,
         |            "additionalInformation" : "complement of information previous2"
         |          }
         |        },
         |        {
         |          "attachToAllItems" : true,
         |          "type" : {
         |            "type" : "Transport",
         |            "code" : "235",
         |            "description" : "Container list"
         |          },
         |          "details" : {
         |            "documentReferenceNumber" : "transport2",
         |            "uuid" : "4ab6bc5a-608d-41f6-acf7-241eb387cad9"
         |          }
         |        },
         |        {
         |          "attachToAllItems" : true,
         |          "type" : {
         |            "type" : "Support",
         |            "code" : "C673",
         |            "description" : "Catch certificate"
         |          },
         |          "details" : {
         |            "documentReferenceNumber" : "support2",
         |            "uuid" : "92626b24-d08e-4d96-ac2c-33b5549361c8",
         |            "addLineItemNumberYesNo" : false,
         |            "addAdditionalInformationYesNo" : false
         |          }
         |        },
         |        {
         |          "attachToAllItems" : true,
         |          "type" : {
         |            "type" : "Previous",
         |            "code" : "IM",
         |            "description" : "Single Administrative Document"
         |          },
         |          "details" : {
         |            "documentReferenceNumber" : "previous3",
         |            "uuid" : "a3184d85-9860-4258-b5ce-28201f0407d3",
         |            "addAdditionalInformationYesNo" : true,
         |            "additionalInformation" : "complement of information previous3"
         |          }
         |        }
         |      ]
         |    },
         |    "items" : [
         |      {
         |        "description" : "Description 1",
         |        "declarationType" : {
         |          "code": "T1",
         |          "description": "Goods not having the customs status of Union goods, which are placed under the common transit procedure."
         |        },
         |        "countryOfDispatch" : {
         |          "code" : "GB",
         |          "description" : "United Kingdom"
         |        },
         |        "countryOfDestination" : {
         |          "code" : "FR",
         |          "description" : "France"
         |        },
         |        "uniqueConsignmentReference" : "UCR 1",
         |        "customsUnionAndStatisticsCode" : "CUS code 1",
         |        "commodityCode" : "commodity code 1",
         |        "combinedNomenclatureCode" : "CN code 1",
         |        "dangerousGoodsList" : [
         |          {
         |            "unNumber" : "UN number 1_1"
         |          },
         |          {
         |            "unNumber" : "UN number 1_2"
         |          }
         |        ],
         |        "grossWeight" : 123.456,
         |        "netWeight" : 1234,
         |        "supplementaryUnits" : 12345,
         |        "methodOfPayment" : {
         |          "method" : "A",
         |          "description" : "Payment in cash"
         |        },
         |        "packages" : [
         |          {
         |            "packageType" : {
         |              "code" : "VL",
         |              "description" : "Bulk, liquid",
         |              "type" : "Bulk"
         |            },
         |            "addShippingMarkYesNo" : false
         |          },
         |          {
         |            "packageType" : {
         |              "code" : "NE",
         |              "description" : "Unpacked or unpackaged",
         |              "type" : "Unpacked"
         |            },
         |            "numberOfPackages" : 5,
         |            "addShippingMarkYesNo" : false
         |          },
         |          {
         |            "packageType" : {
         |              "code" : "TR",
         |              "description" : "Trunk",
         |              "type" : "Other"
         |            },
         |            "shippingMark" : "mark3"
         |          }
         |        ],
         |        "addSupplyChainActorYesNo" : true,
         |        "supplyChainActors" : [
         |          {
         |            "supplyChainActorType" : {
         |              "role": "CS",
         |              "description": "Consolidator"
         |            },
         |            "identificationNumber" : "itemSCA1"
         |          },
         |          {
         |            "supplyChainActorType" : {
         |              "role": "FW",
         |              "description": "Freight Forwarder"
         |            },
         |            "identificationNumber" : "itemSCA2"
         |          },
         |          {
         |            "supplyChainActorType" : {
         |              "role": "MF",
         |              "description": "Manufacturer"
         |            },
         |            "identificationNumber" : "itemSCA3"
         |          },
         |          {
         |            "supplyChainActorType" : {
         |              "role": "WH",
         |              "description": "Warehouse Keeper"
         |            },
         |            "identificationNumber" : "itemSCA4"
         |          }
         |        ],
         |        "addDocumentsYesNo" : true,
         |        "documents" : [
         |          {
         |            "document" : "1794d93b-17d5-44fe-a18d-aaa2059d06fe"
         |          },
         |          {
         |            "document" : "a573bfd3-6470-40c4-a290-ea2d8d43c02a"
         |          },
         |          {
         |            "document" : "3882459f-b7bc-478d-9d24-359533aa8fe3"
         |          },
         |          {
         |            "document" : "ac50154c-cad1-4320-8def-d282eea63b2e"
         |          },
         |          {
         |            "document" : "7d342b27-5171-428c-9354-fb2928b72c3a"
         |          }
         |        ],
         |        "addAdditionalReferenceYesNo" : true,
         |        "additionalReferences" : [
         |          {
         |            "additionalReference" : {
         |              "documentType" : "ar1",
         |              "description" : "Additional reference 1"
         |            },
         |            "addAdditionalReferenceNumberYesNo" : true,
         |            "additionalReferenceNumber" : "arno1"
         |          },
         |          {
         |            "additionalReference" : {
         |              "documentType" : "ar2",
         |              "description" : "Additional reference 2"
         |            },
         |            "addAdditionalReferenceNumberYesNo" : false
         |          }
         |        ],
         |        "additionalInformationList" : [
         |          {
         |            "additionalInformationType" : {
         |              "code" : "aiCode1",
         |              "description" : "aiDescription1"
         |            },
         |            "additionalInformation" : "ai1"
         |          },
         |          {
         |            "additionalInformationType" : {
         |              "code" : "aiCode2",
         |              "description" : "aiDescription2"
         |            },
         |            "additionalInformation" : "ai2"
         |          }
         |        ],
         |        "transportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371",
         |        "consignee" : {
         |          "addConsigneeEoriNumberYesNo" : true,
         |          "identificationNumber" : "GE00101001",
         |          "name" : "Mr. Consignee",
         |          "country" : {
         |            "code" : "GB",
         |            "description" : "United Kingdom"
         |          },
         |          "address" : {
         |            "numberAndStreet" : "1 Merry Lane",
         |            "city" : "Godrics Hollow",
         |            "postalCode" : "CA1 9AA"
         |          }
         |        }
         |      },
         |      {
         |        "description" : "Description 2",
         |        "declarationType" : {
         |          "code": "T2",
         |          "description": "Goods having the customs status of Union goods, which are placed under the common transit procedure"
         |        },
         |        "countryOfDispatch" : {
         |          "code" : "DE",
         |          "description" : "Germany"
         |        },
         |        "countryOfDestination" : {
         |          "code" : "ES",
         |          "description" : "Spain"
         |        },
         |        "uniqueConsignmentReference" : "UCR 2",
         |        "customsUnionAndStatisticsCode" : "CUS code 2",
         |        "commodityCode" : "commodity code 2",
         |        "combinedNomenclatureCode" : "CN code 2",
         |        "dangerousGoodsList" : [
         |          {
         |            "unNumber" : "UN number 2_1"
         |          },
         |          {
         |            "unNumber" : "UN number 2_2"
         |          }
         |        ],
         |        "grossWeight" : 456.789,
         |        "methodOfPayment" : {
         |          "method" : "A",
         |          "description" : "Payment in cash"
         |        },
         |        "addSupplyChainActorYesNo" : false,
         |        "addDocumentsYesNo" : false,
         |        "addAdditionalReferenceYesNo" : true,
         |        "additionalReferences" : [
         |          {
         |            "additionalReference" : {
         |              "documentType" : "ar1",
         |              "description" : "Additional reference 1"
         |            },
         |            "addAdditionalReferenceNumberYesNo" : true,
         |            "additionalReferenceNumber" : "arno1"
         |          }
         |        ],
         |        "additionalInformationList" : [
         |          {
         |            "additionalInformationType" : {
         |              "code" : "aiCode1",
         |              "description" : "aiDescription1"
         |            },
         |            "additionalInformation" : "ai1"
         |          }
         |        ],
         |        "transportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
         |      }
         |    ]
         |  },
         |  "tasks" : {},
         |  "createdAt" : "2022-09-05T15:58:44.188Z",
         |  "lastUpdated" : "2022-09-07T10:33:23.472Z"
         |}
         |""".stripMargin)
  }
}
