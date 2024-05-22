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

import api.submission.Consignment.RichConsignmentType20
import api.submission.consignmentType20.{activeBorderTransportMeansReads, transportEquipmentReads}
import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import generators.Generators
import models.{Phase, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.{JsValue, Json}

import scala.collection.immutable.Seq

class ConsignmentSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val phase = arbitrary[Phase].sample.value

  "Consignment" when {

    "transform is called" must {

      "convert to API format with documents" in {

        val json: JsValue = Json.parse(s"""
            |{
            |  "_id" : "$uuid",
            |  "lrn" : "$lrn",
            |  "eoriNumber" : "$eoriNumber",
            |  "isSubmitted" : "notSubmitted",
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
            |    "additionalReference" : [
            |            {
            |              "type" : "type123",
            |             "additionalReferenceNumber" : "arno1"
            |           },
            |            {
            |              "type" : "type321",
            |             "additionalReferenceNumber" : "1onra"
            |            }
            |          ],
            |         "additionalInformation" : [
            |           {
            |             "type" : "adinftype1",
            |              "text" : "orca"
            |            },
            |           {
            |            "type" : "adinftype2",
            |             "text" : "acro"
            |            }
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

        val uA: UserAnswers = json.as[UserAnswers]

        val converted: ConsignmentType20 = Consignment.transform(uA)

        converted.countryOfDispatch shouldBe Some("FR")
        converted.countryOfDestination shouldBe Some("IT")
        converted.containerIndicator shouldBe Some(Number1)
        converted.inlandModeOfTransport shouldBe Some("1")
        converted.modeOfTransportAtTheBorder shouldBe Some("1")
        converted.grossMass shouldBe 580.245
        converted.referenceNumberUCR shouldBe Some("ucr123")

        converted.Carrier shouldBe Some(
          CarrierType04(
            identificationNumber = "carrier1",
            ContactPerson = Some(
              ContactPersonType05(
                name = "Carrier Contact",
                phoneNumber = "+44 808 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.Consignor shouldBe Some(
          ConsignorType07(
            identificationNumber = Some("consignor1"),
            name = Some("Mr Consignor"),
            Address = Some(
              AddressType17(
                streetAndNumber = "21 Test Lane",
                postcode = Some("NE1 1NE"),
                city = "Newcastle upon Tyne",
                country = "GB"
              )
            ),
            ContactPerson = Some(
              ContactPersonType05(
                name = "Consignor Contact",
                phoneNumber = "+44 101 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.Consignee shouldBe Some(
          ConsigneeType05(
            identificationNumber = Some("consignee1"),
            name = Some("Mr Consignee"),
            Address = Some(
              AddressType17(
                streetAndNumber = "21 Test Rue",
                postcode = Some("PA1 1PA"),
                city = "Paris",
                country = "FR"
              )
            )
          )
        )

        converted.AdditionalSupplyChainActor shouldBe Seq(
          AdditionalSupplyChainActorType(
            sequenceNumber = "1",
            role = "CS",
            identificationNumber = "sca1"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "2",
            role = "FW",
            identificationNumber = "sca2"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "3",
            role = "MF",
            identificationNumber = "sca3"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "4",
            role = "WH",
            identificationNumber = "sca4"
          )
        )

        converted.TransportEquipment shouldBe Seq(
          TransportEquipmentType06(
            sequenceNumber = "1",
            containerIdentificationNumber = Some("container id 1"),
            numberOfSeals = 2,
            Seal = Seq(
              SealType05(
                sequenceNumber = "1",
                identifier = "seal 1"
              ),
              SealType05(
                sequenceNumber = "2",
                identifier = "seal 2"
              )
            ),
            GoodsReference = Seq(
              GoodsReferenceType02(
                sequenceNumber = "1",
                declarationGoodsItemNumber = 1
              ),
              GoodsReferenceType02(
                sequenceNumber = "2",
                declarationGoodsItemNumber = 2
              )
            )
          )
        )

        converted.LocationOfGoods shouldBe Some(
          LocationOfGoodsType05(
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
            EconomicOperator = Some(EconomicOperatorType03(identificationNumber = "GB12345")),
            Address = Some(
              AddressType14(
                streetAndNumber = "21 Test Camino",
                postcode = Some("ES1 1SE"),
                city = "Madrid",
                country = "ES"
              )
            ),
            PostcodeAddress = Some(
              PostcodeAddressType02(
                houseNumber = Some("21"),
                postcode = "DE1 1DE",
                country = "DE"
              )
            ),
            ContactPerson = Some(
              ContactPersonType06(
                name = "Location of goods Contact",
                phoneNumber = "+44 202 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.DepartureTransportMeans shouldBe Seq(
          DepartureTransportMeansType03(
            sequenceNumber = "1",
            typeOfIdentification = Some("10"),
            identificationNumber = Some("means id number"),
            nationality = Some("FR")
          )
        )

        converted.CountryOfRoutingOfConsignment shouldBe Seq(
          CountryOfRoutingOfConsignmentType01(
            sequenceNumber = "1",
            country = "AD"
          ),
          CountryOfRoutingOfConsignmentType01(
            sequenceNumber = "2",
            country = "AR"
          )
        )

        converted.ActiveBorderTransportMeans shouldBe Seq(
          ActiveBorderTransportMeansType02(
            sequenceNumber = "1",
            customsOfficeAtBorderReferenceNumber = Some("IT018101"),
            typeOfIdentification = Some("11"),
            identificationNumber = Some("active id number"),
            nationality = Some("ES"),
            conveyanceReferenceNumber = Some("conveyance ref number")
          )
        )

        converted.PlaceOfLoading shouldBe Some(
          PlaceOfLoadingType03(
            Some("AEFAT"),
            Some("Loading country"),
            Some("Loading location")
          )
        )

        converted.PlaceOfUnloading shouldBe Some(
          PlaceOfUnloadingType01(
            Some("ADALV"),
            Some("Unloading country"),
            Some("Unloading location")
          )
        )

        converted.PreviousDocument shouldBe Seq(
          PreviousDocumentType09(
            sequenceNumber = "1",
            typeValue = "IM",
            referenceNumber = "previous3",
            complementOfInformation = Some("complement of information previous3")
          )
        )

        converted.SupportingDocument shouldBe Seq(
          SupportingDocumentType05(
            sequenceNumber = "1",
            typeValue = "C673",
            referenceNumber = "support2",
            documentLineItemNumber = None,
            complementOfInformation = None
          )
        )

        converted.TransportDocument shouldBe Seq(
          TransportDocumentType04(
            sequenceNumber = "1",
            typeValue = "235",
            referenceNumber = "transport2"
          )
        )

        converted.AdditionalReference shouldBe Seq(
          AdditionalReferenceType05(
            sequenceNumber = "1",
            typeValue = "type123",
            referenceNumber = Some("arno1")
          ),
          AdditionalReferenceType05(
            sequenceNumber = "2",
            typeValue = "type321",
            referenceNumber = Some("1onra")
          )
        )

        converted.AdditionalInformation shouldBe Seq(
          AdditionalInformationType03(
            sequenceNumber = "1",
            code = "adinftype1",
            text = Some("orca")
          ),
          AdditionalInformationType03(
            sequenceNumber = "2",
            code = "adinftype2",
            text = Some("acro")
          )
        )

        converted.TransportCharges shouldBe Some(
          TransportChargesType("A")
        )

        converted.HouseConsignment.size shouldBe 1
        val ans = converted.HouseConsignment.head

        val exp = HouseConsignmentType10(
          sequenceNumber = "1",
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
            ConsignmentItemType09(
              goodsItemNumber = "1",
              declarationGoodsItemNumber = 1,
              declarationType = Some("T1"),
              countryOfDispatch = Some("GB"),
              countryOfDestination = Some("FR"),
              referenceNumberUCR = Some("UCR 1"),
              Consignee = Some(
                ConsigneeType02(
                  identificationNumber = Some("GE00101001"),
                  name = Some("Mr. Consignee"),
                  Address = Some(
                    AddressType12(
                      streetAndNumber = "1 Merry Lane",
                      postcode = Some("CA1 9AA"),
                      city = "Godrics Hollow",
                      country = "GB"
                    )
                  )
                )
              ),
              AdditionalSupplyChainActor = Seq(
                AdditionalSupplyChainActorType(
                  sequenceNumber = "1",
                  role = "CS",
                  identificationNumber = "itemSCA1"
                ),
                AdditionalSupplyChainActorType(
                  sequenceNumber = "2",
                  role = "FW",
                  identificationNumber = "itemSCA2"
                ),
                AdditionalSupplyChainActorType(
                  sequenceNumber = "3",
                  role = "MF",
                  identificationNumber = "itemSCA3"
                ),
                AdditionalSupplyChainActorType(
                  sequenceNumber = "4",
                  role = "WH",
                  identificationNumber = "itemSCA4"
                )
              ),
              Commodity = CommodityType07(
                descriptionOfGoods = "Description 1",
                cusCode = Some("CUS code 1"),
                CommodityCode = Some(
                  CommodityCodeType02(
                    harmonizedSystemSubHeadingCode = "commodity code 1",
                    combinedNomenclatureCode = Some("CN code 1")
                  )
                ),
                DangerousGoods = Seq(
                  DangerousGoodsType01(
                    sequenceNumber = "1",
                    UNNumber = "UN number 1_1"
                  ),
                  DangerousGoodsType01(
                    sequenceNumber = "2",
                    UNNumber = "UN number 1_2"
                  )
                ),
                GoodsMeasure = Some(
                  GoodsMeasureType02(
                    grossMass = Some(BigDecimal(123.456)),
                    netMass = Some(BigDecimal(1234)),
                    supplementaryUnits = Some(BigDecimal(12345))
                  )
                )
              ),
              Packaging = Seq(
                PackagingType03(
                  sequenceNumber = "1",
                  typeOfPackages = "VL",
                  numberOfPackages = None,
                  shippingMarks = None
                ),
                PackagingType03(
                  sequenceNumber = "2",
                  typeOfPackages = "NE",
                  numberOfPackages = Some(5),
                  shippingMarks = None
                ),
                PackagingType03(
                  sequenceNumber = "3",
                  typeOfPackages = "TR",
                  numberOfPackages = None,
                  shippingMarks = Some("mark3")
                )
              ),
              PreviousDocument = Seq(
                PreviousDocumentType08(
                  sequenceNumber = "1",
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
                  sequenceNumber = "2",
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
                SupportingDocumentType05(
                  sequenceNumber = "1",
                  typeValue = "C673",
                  referenceNumber = "support1",
                  documentLineItemNumber = Some(678),
                  complementOfInformation = Some("complement of information support1")
                )
              ),
              TransportDocument = Seq(
                TransportDocumentType04(
                  sequenceNumber = "1",
                  typeValue = "235",
                  referenceNumber = "transport1"
                )
              ),
              AdditionalReference = Seq(
                AdditionalReferenceType04(
                  sequenceNumber = "1",
                  typeValue = "ar1",
                  referenceNumber = Some("arno1")
                ),
                AdditionalReferenceType04(
                  sequenceNumber = "2",
                  typeValue = "ar2",
                  referenceNumber = None
                )
              ),
              AdditionalInformation = Seq(
                AdditionalInformationType03(
                  sequenceNumber = "1",
                  code = "aiCode1",
                  text = Some("ai1")
                ),
                AdditionalInformationType03(
                  sequenceNumber = "2",
                  code = "aiCode2",
                  text = Some("ai2")
                )
              ),
              TransportCharges = None
            ),
            ConsignmentItemType09(
              goodsItemNumber = "2",
              declarationGoodsItemNumber = 2,
              declarationType = Some("T2"),
              countryOfDispatch = Some("DE"),
              countryOfDestination = Some("ES"),
              referenceNumberUCR = Some("UCR 2"),
              Consignee = None,
              AdditionalSupplyChainActor = Nil,
              Commodity = CommodityType07(
                descriptionOfGoods = "Description 2",
                cusCode = Some("CUS code 2"),
                CommodityCode = Some(
                  CommodityCodeType02(
                    harmonizedSystemSubHeadingCode = "commodity code 2",
                    combinedNomenclatureCode = Some("CN code 2")
                  )
                ),
                DangerousGoods = Seq(
                  DangerousGoodsType01(
                    sequenceNumber = "1",
                    UNNumber = "UN number 2_1"
                  ),
                  DangerousGoodsType01(
                    sequenceNumber = "2",
                    UNNumber = "UN number 2_2"
                  )
                ),
                GoodsMeasure = Some(
                  GoodsMeasureType02(
                    grossMass = Some(BigDecimal(456.789)),
                    netMass = None,
                    supplementaryUnits = None
                  )
                )
              ),
              Packaging = Nil,
              PreviousDocument = Nil,
              SupportingDocument = Nil,
              TransportDocument = Nil,
              AdditionalReference = Seq(
                AdditionalReferenceType04(
                  sequenceNumber = "1",
                  typeValue = "ar1",
                  referenceNumber = Some("arno1")
                )
              ),
              AdditionalInformation = Seq(
                AdditionalInformationType03(
                  sequenceNumber = "1",
                  code = "aiCode1",
                  text = Some("ai1")
                )
              ),
              TransportCharges = None
            )
          )
        )

        ans shouldBe exp
      }

      "convert to API format without documents" in {

        val json: JsValue = Json.parse(s"""
             |{
             |  "_id" : "$uuid",
             |  "lrn" : "$lrn",
             |  "eoriNumber" : "$eoriNumber",
             |  "isSubmitted" : "notSubmitted",
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
             |            {
             |              "type" : "type123",
             |             "additionalReferenceNumber" : "ARNO1"
             |           },
             |            {
             |              "type" : "type321",
             |             "additionalReferenceNumber" : "1ONRA"
             |            }
             |          ],
             |         "additionalInformation" : [
             |           {
             |             "type" : "ADDINFTYPE1",
             |              "text" : "ORCA"
             |            },
             |           {
             |            "type" : "ADDINFTYPE2",
             |             "text" : "ACRO"
             |            }
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

        val uA: UserAnswers = json.as[UserAnswers]

        val converted: ConsignmentType20 = Consignment.transform(uA)

        converted.countryOfDispatch shouldBe Some("FR")
        converted.countryOfDestination shouldBe Some("IT")
        converted.containerIndicator shouldBe Some(Number1)
        converted.inlandModeOfTransport shouldBe Some("1")
        converted.modeOfTransportAtTheBorder shouldBe Some("1")
        converted.grossMass shouldBe 580.245
        converted.referenceNumberUCR shouldBe Some("ucr123")

        converted.Carrier shouldBe Some(
          CarrierType04(
            identificationNumber = "carrier1",
            ContactPerson = Some(
              ContactPersonType05(
                name = "Carrier Contact",
                phoneNumber = "+44 808 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.Consignor shouldBe Some(
          ConsignorType07(
            identificationNumber = Some("consignor1"),
            name = Some("Mr Consignor"),
            Address = Some(
              AddressType17(
                streetAndNumber = "21 Test Lane",
                postcode = Some("NE1 1NE"),
                city = "Newcastle upon Tyne",
                country = "GB"
              )
            ),
            ContactPerson = Some(
              ContactPersonType05(
                name = "Consignor Contact",
                phoneNumber = "+44 101 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.Consignee shouldBe Some(
          ConsigneeType05(
            identificationNumber = Some("consignee1"),
            name = Some("Mr Consignee"),
            Address = Some(
              AddressType17(
                streetAndNumber = "21 Test Rue",
                postcode = Some("PA1 1PA"),
                city = "Paris",
                country = "FR"
              )
            )
          )
        )

        converted.AdditionalSupplyChainActor shouldBe Seq(
          AdditionalSupplyChainActorType(
            sequenceNumber = "1",
            role = "CS",
            identificationNumber = "sca1"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "2",
            role = "FW",
            identificationNumber = "sca2"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "3",
            role = "MF",
            identificationNumber = "sca3"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "4",
            role = "WH",
            identificationNumber = "sca4"
          )
        )

        converted.TransportEquipment shouldBe Seq(
          TransportEquipmentType06(
            sequenceNumber = "1",
            containerIdentificationNumber = Some("container id 1"),
            numberOfSeals = 2,
            Seal = Seq(
              SealType05(
                sequenceNumber = "1",
                identifier = "seal 1"
              ),
              SealType05(
                sequenceNumber = "2",
                identifier = "seal 2"
              )
            ),
            GoodsReference = Seq(
              GoodsReferenceType02(
                sequenceNumber = "1",
                declarationGoodsItemNumber = 1
              ),
              GoodsReferenceType02(
                sequenceNumber = "2",
                declarationGoodsItemNumber = 2
              )
            )
          )
        )

        converted.LocationOfGoods shouldBe Some(
          LocationOfGoodsType05(
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
            EconomicOperator = Some(EconomicOperatorType03(identificationNumber = "GB12345")),
            Address = Some(
              AddressType14(
                streetAndNumber = "21 Test Camino",
                postcode = Some("ES1 1SE"),
                city = "Madrid",
                country = "ES"
              )
            ),
            PostcodeAddress = Some(
              PostcodeAddressType02(
                houseNumber = Some("21"),
                postcode = "DE1 1DE",
                country = "DE"
              )
            ),
            ContactPerson = Some(
              ContactPersonType06(
                name = "Location of goods Contact",
                phoneNumber = "+44 202 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.DepartureTransportMeans shouldBe Seq(
          DepartureTransportMeansType03(
            sequenceNumber = "1",
            typeOfIdentification = Some("10"),
            identificationNumber = Some("means id number"),
            nationality = Some("FR")
          )
        )

        converted.CountryOfRoutingOfConsignment shouldBe Seq(
          CountryOfRoutingOfConsignmentType01(
            sequenceNumber = "1",
            country = "AD"
          ),
          CountryOfRoutingOfConsignmentType01(
            sequenceNumber = "2",
            country = "AR"
          )
        )

        converted.ActiveBorderTransportMeans shouldBe Seq(
          ActiveBorderTransportMeansType02(
            sequenceNumber = "1",
            customsOfficeAtBorderReferenceNumber = Some("IT018101"),
            typeOfIdentification = Some("11"),
            identificationNumber = Some("active id number"),
            nationality = Some("ES"),
            conveyanceReferenceNumber = Some("conveyance ref number")
          )
        )

        converted.PlaceOfLoading shouldBe Some(
          PlaceOfLoadingType03(
            Some("AEFAT"),
            Some("Loading country"),
            Some("Loading location")
          )
        )

        converted.PlaceOfUnloading shouldBe Some(
          PlaceOfUnloadingType01(
            Some("ADALV"),
            Some("Unloading country"),
            Some("Unloading location")
          )
        )

        converted.PreviousDocument shouldBe Seq.empty

        converted.SupportingDocument shouldBe Seq.empty

        converted.TransportDocument shouldBe Seq.empty

        converted.AdditionalReference shouldBe Seq(
          AdditionalReferenceType05(
            sequenceNumber = "1",
            typeValue = "type123",
            referenceNumber = Some("ARNO1")
          ),
          AdditionalReferenceType05(
            sequenceNumber = "2",
            typeValue = "type321",
            referenceNumber = Some("1ONRA")
          )
        )

        converted.AdditionalInformation shouldBe Seq(
          AdditionalInformationType03(
            sequenceNumber = "1",
            code = "ADDINFTYPE1",
            text = Some("ORCA")
          ),
          AdditionalInformationType03(
            sequenceNumber = "2",
            code = "ADDINFTYPE2",
            text = Some("ACRO")
          )
        )

        converted.TransportCharges shouldBe Some(
          TransportChargesType("A")
        )

        converted.HouseConsignment.size shouldBe 1
        val output = converted.HouseConsignment.head

        val expected = HouseConsignmentType10(
          sequenceNumber = "1",
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
            ConsignmentItemType09(
              goodsItemNumber = "1",
              declarationGoodsItemNumber = 1,
              declarationType = Some("T1"),
              countryOfDispatch = Some("GB"),
              countryOfDestination = Some("FR"),
              referenceNumberUCR = Some("UCR 1"),
              Consignee = Some(
                ConsigneeType02(
                  identificationNumber = Some("GE00101001"),
                  name = Some("Mr. Consignee"),
                  Address = Some(
                    AddressType12(
                      streetAndNumber = "1 Merry Lane",
                      postcode = Some("CA1 9AA"),
                      city = "Godrics Hollow",
                      country = "GB"
                    )
                  )
                )
              ),
              AdditionalSupplyChainActor = Seq(
                AdditionalSupplyChainActorType(
                  sequenceNumber = "1",
                  role = "CS",
                  identificationNumber = "itemSCA1"
                ),
                AdditionalSupplyChainActorType(
                  sequenceNumber = "2",
                  role = "FW",
                  identificationNumber = "itemSCA2"
                ),
                AdditionalSupplyChainActorType(
                  sequenceNumber = "3",
                  role = "MF",
                  identificationNumber = "itemSCA3"
                ),
                AdditionalSupplyChainActorType(
                  sequenceNumber = "4",
                  role = "WH",
                  identificationNumber = "itemSCA4"
                )
              ),
              Commodity = CommodityType07(
                descriptionOfGoods = "Description 1",
                cusCode = Some("CUS code 1"),
                CommodityCode = Some(
                  CommodityCodeType02(
                    harmonizedSystemSubHeadingCode = "commodity code 1",
                    combinedNomenclatureCode = Some("CN code 1")
                  )
                ),
                DangerousGoods = Seq(
                  DangerousGoodsType01(
                    sequenceNumber = "1",
                    UNNumber = "UN number 1_1"
                  ),
                  DangerousGoodsType01(
                    sequenceNumber = "2",
                    UNNumber = "UN number 1_2"
                  )
                ),
                GoodsMeasure = Some(
                  GoodsMeasureType02(
                    grossMass = Some(BigDecimal(123.456)),
                    netMass = Some(BigDecimal(1234)),
                    supplementaryUnits = Some(BigDecimal(12345))
                  )
                )
              ),
              Packaging = Seq(
                PackagingType03(
                  sequenceNumber = "1",
                  typeOfPackages = "VL",
                  numberOfPackages = None,
                  shippingMarks = None
                ),
                PackagingType03(
                  sequenceNumber = "2",
                  typeOfPackages = "NE",
                  numberOfPackages = Some(5),
                  shippingMarks = None
                ),
                PackagingType03(
                  sequenceNumber = "3",
                  typeOfPackages = "TR",
                  numberOfPackages = None,
                  shippingMarks = Some("mark3")
                )
              ),
              PreviousDocument = Seq.empty,
              SupportingDocument = Seq.empty,
              TransportDocument = Seq.empty,
              AdditionalReference = Seq(
                AdditionalReferenceType04(
                  sequenceNumber = "1",
                  typeValue = "ar1",
                  referenceNumber = Some("arno1")
                ),
                AdditionalReferenceType04(
                  sequenceNumber = "2",
                  typeValue = "ar2",
                  referenceNumber = None
                )
              ),
              AdditionalInformation = Seq(
                AdditionalInformationType03(
                  sequenceNumber = "1",
                  code = "aiCode1",
                  text = Some("ai1")
                ),
                AdditionalInformationType03(
                  sequenceNumber = "2",
                  code = "aiCode2",
                  text = Some("ai2")
                )
              ),
              TransportCharges = None
            ),
            ConsignmentItemType09(
              goodsItemNumber = "2",
              declarationGoodsItemNumber = 2,
              declarationType = Some("T2"),
              countryOfDispatch = Some("DE"),
              countryOfDestination = Some("ES"),
              referenceNumberUCR = Some("UCR 2"),
              Consignee = None,
              AdditionalSupplyChainActor = Nil,
              Commodity = CommodityType07(
                descriptionOfGoods = "Description 2",
                cusCode = Some("CUS code 2"),
                CommodityCode = Some(
                  CommodityCodeType02(
                    harmonizedSystemSubHeadingCode = "commodity code 2",
                    combinedNomenclatureCode = Some("CN code 2")
                  )
                ),
                DangerousGoods = Seq(
                  DangerousGoodsType01(
                    sequenceNumber = "1",
                    UNNumber = "UN number 2_1"
                  ),
                  DangerousGoodsType01(
                    sequenceNumber = "2",
                    UNNumber = "UN number 2_2"
                  )
                ),
                GoodsMeasure = Some(
                  GoodsMeasureType02(
                    grossMass = Some(BigDecimal(456.789)),
                    netMass = None,
                    supplementaryUnits = None
                  )
                )
              ),
              Packaging = Nil,
              PreviousDocument = Nil,
              SupportingDocument = Nil,
              TransportDocument = Nil,
              AdditionalReference = Seq(
                AdditionalReferenceType04(
                  sequenceNumber = "1",
                  typeValue = "ar1",
                  referenceNumber = Some("arno1")
                )
              ),
              AdditionalInformation = Seq(
                AdditionalInformationType03(
                  sequenceNumber = "1",
                  code = "aiCode1",
                  text = Some("ai1")
                )
              ),
              TransportCharges = None
            )
          )
        )

        output shouldBe expected
      }
    }

    "postProcess is called" when {

      "rollUpTransportCharges" when {

        "every item has the same transport charges" when {
          "consignment transport charges undefined" must {
            "roll up transport charges to consignment level and remove them from each item" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        TransportCharges = Some(
                          TransportChargesType("A")
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        TransportCharges = Some(
                          TransportChargesType("A")
                        )
                      )
                    )
                  )
                )
              )

              val result = consignment.postProcess

              result shouldBe ConsignmentType20(
                grossMass = BigDecimal(1),
                TransportCharges = Some(
                  TransportChargesType(
                    methodOfPayment = "A"
                  )
                ),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        TransportCharges = None
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        TransportCharges = None
                      )
                    )
                  )
                )
              )
            }
          }

          "consignment transport charges defined" must {
            "when consignment transport charges are the same as all item transport charges" must {
              "remove transport charges from each item" in {
                val consignment = ConsignmentType20(
                  grossMass = BigDecimal(1),
                  TransportCharges = Some(
                    TransportChargesType("A")
                  ),
                  HouseConsignment = Seq(
                    HouseConsignmentType10(
                      sequenceNumber = "1",
                      grossMass = BigDecimal(1),
                      ConsignmentItem = Seq(
                        ConsignmentItemType09(
                          goodsItemNumber = "1",
                          declarationGoodsItemNumber = BigInt(1),
                          Commodity = CommodityType07(
                            descriptionOfGoods = "Item 1"
                          ),
                          TransportCharges = Some(
                            TransportChargesType("A")
                          )
                        ),
                        ConsignmentItemType09(
                          goodsItemNumber = "2",
                          declarationGoodsItemNumber = BigInt(2),
                          Commodity = CommodityType07(
                            descriptionOfGoods = "Item 2"
                          ),
                          TransportCharges = Some(
                            TransportChargesType("A")
                          )
                        )
                      )
                    )
                  )
                )

                val result = consignment.postProcess

                result shouldBe ConsignmentType20(
                  grossMass = BigDecimal(1),
                  TransportCharges = Some(
                    TransportChargesType("A")
                  ),
                  HouseConsignment = Seq(
                    HouseConsignmentType10(
                      sequenceNumber = "1",
                      grossMass = BigDecimal(1),
                      ConsignmentItem = Seq(
                        ConsignmentItemType09(
                          goodsItemNumber = "1",
                          declarationGoodsItemNumber = BigInt(1),
                          Commodity = CommodityType07(
                            descriptionOfGoods = "Item 1"
                          ),
                          TransportCharges = None
                        ),
                        ConsignmentItemType09(
                          goodsItemNumber = "2",
                          declarationGoodsItemNumber = BigInt(2),
                          Commodity = CommodityType07(
                            descriptionOfGoods = "Item 2"
                          ),
                          TransportCharges = None
                        )
                      )
                    )
                  )
                )
              }
            }

            "when consignment transport charges are not the same as all item transport charges" must {
              "not remove transport charges from each item" in {
                val consignment = ConsignmentType20(
                  grossMass = BigDecimal(1),
                  TransportCharges = Some(
                    TransportChargesType("B")
                  ),
                  HouseConsignment = Seq(
                    HouseConsignmentType10(
                      sequenceNumber = "1",
                      grossMass = BigDecimal(1),
                      ConsignmentItem = Seq(
                        ConsignmentItemType09(
                          goodsItemNumber = "1",
                          declarationGoodsItemNumber = BigInt(1),
                          Commodity = CommodityType07(
                            descriptionOfGoods = "Item 1"
                          ),
                          TransportCharges = Some(
                            TransportChargesType("A")
                          )
                        ),
                        ConsignmentItemType09(
                          goodsItemNumber = "2",
                          declarationGoodsItemNumber = BigInt(2),
                          Commodity = CommodityType07(
                            descriptionOfGoods = "Item 2"
                          ),
                          TransportCharges = Some(
                            TransportChargesType("A")
                          )
                        )
                      )
                    )
                  )
                )

                val result = consignment.postProcess

                result shouldBe consignment
              }
            }
          }
        }

        "some items have the same transport charges" must {
          "not roll up transport charges to consignment level" in {
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      TransportCharges = Some(
                        TransportChargesType("A")
                      )
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      TransportCharges = Some(
                        TransportChargesType("A")
                      )
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "3",
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 3"
                      ),
                      TransportCharges = None
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe consignment
          }
        }

        "no items have the same transport charges" must {
          "not roll up transport charges to consignment level" in {
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      TransportCharges = Some(
                        TransportChargesType("A")
                      )
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      TransportCharges = Some(
                        TransportChargesType("B")
                      )
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "3",
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 3"
                      ),
                      TransportCharges = Some(
                        TransportChargesType("C")
                      )
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe consignment
          }
        }
      }

      "rollUpUCR" when {

        "every item has the same UCR" must {
          "roll up UCR to consignment level and remove them from each item" in {
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      referenceNumberUCR = Some("UCR")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      referenceNumberUCR = Some("UCR")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe ConsignmentType20(
              grossMass = BigDecimal(1),
              referenceNumberUCR = Some("UCR"),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      referenceNumberUCR = None
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
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
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      referenceNumberUCR = Some("UCR")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      referenceNumberUCR = Some("UCR")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "3",
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 3"
                      ),
                      referenceNumberUCR = None
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe consignment
          }
        }

        "no items have the same UCR" must {
          "not roll up UCR to consignment level" in {
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      referenceNumberUCR = Some("UCR1")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      referenceNumberUCR = Some("UCR2")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "3",
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 3"
                      ),
                      referenceNumberUCR = Some("UCR3")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe consignment
          }
        }
      }

      "rollUpCountryOfDispatch" when {

        "every item has the same country of dispatch" must {
          "roll up country of dispatch to consignment level and remove them from each item" in {
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      countryOfDispatch = Some("GB")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      countryOfDispatch = Some("GB")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe ConsignmentType20(
              grossMass = BigDecimal(1),
              countryOfDispatch = Some("GB"),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      countryOfDispatch = None
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
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
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      countryOfDispatch = Some("GB")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      countryOfDispatch = Some("GB")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "3",
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 3"
                      ),
                      countryOfDispatch = None
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe consignment
          }
        }

        "no items have the same country of dispatch" must {
          "not roll up country of dispatch to consignment level" in {
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      countryOfDispatch = Some("GB")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      countryOfDispatch = Some("FR")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "3",
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 3"
                      ),
                      countryOfDispatch = Some("ES")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe consignment
          }
        }
      }

      "rollUpCountryOfDestination" when {

        "every item has the same country of destination" must {
          "roll up country of destination to consignment level and remove them from each item" in {
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      countryOfDestination = Some("GB")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      countryOfDestination = Some("GB")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe ConsignmentType20(
              grossMass = BigDecimal(1),
              countryOfDestination = Some("GB"),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      countryOfDestination = None
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
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
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      countryOfDestination = Some("GB")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      countryOfDestination = Some("GB")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "3",
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 3"
                      ),
                      countryOfDestination = None
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe consignment
          }
        }

        "no items have the same country of dispatch" must {
          "not roll up country of dispatch to consignment level" in {
            val consignment = ConsignmentType20(
              grossMass = BigDecimal(1),
              HouseConsignment = Seq(
                HouseConsignmentType10(
                  sequenceNumber = "1",
                  grossMass = BigDecimal(1),
                  ConsignmentItem = Seq(
                    ConsignmentItemType09(
                      goodsItemNumber = "1",
                      declarationGoodsItemNumber = BigInt(1),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 1"
                      ),
                      countryOfDispatch = Some("GB")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "2",
                      declarationGoodsItemNumber = BigInt(2),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 2"
                      ),
                      countryOfDispatch = Some("FR")
                    ),
                    ConsignmentItemType09(
                      goodsItemNumber = "3",
                      declarationGoodsItemNumber = BigInt(3),
                      Commodity = CommodityType07(
                        descriptionOfGoods = "Item 3"
                      ),
                      countryOfDispatch = Some("ES")
                    )
                  )
                )
              )
            )

            val result = consignment.postProcess

            result shouldBe consignment
          }
        }
      }

      "rollUpAdditionalInformation" when {

        "during post-transition" when {

          "every item has a common additional information" must {
            "roll up each common additional information to consignment level and remove them from each item" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi1",
                            text = Some("ADI 1")
                          ),
                          AdditionalInformationType03(
                            sequenceNumber = "2",
                            code = "adi2",
                            text = Some("ADI 2")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi1",
                            text = Some("ADI 1")
                          )
                        )
                      )
                    )
                  )
                )
              )

              val result = consignment.postProcess

              val expected = ConsignmentType20(
                grossMass = BigDecimal(1),
                AdditionalInformation = Seq.empty,
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi1",
                            text = Some("ADI 1")
                          ),
                          AdditionalInformationType03(
                            sequenceNumber = "2",
                            code = "adi2",
                            text = Some("ADI 2")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi1",
                            text = Some("ADI 1")
                          )
                        )
                      )
                    )
                  )
                )
              )

              result shouldBe expected

            }
          }

          "some items have common additional information" must {
            "not roll up additional information to consignment level" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi1",
                            text = Some("ADI 1")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi1",
                            text = Some("ADI 1")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "3",
                        declarationGoodsItemNumber = BigInt(3),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 3"
                        ),
                        AdditionalInformation = Nil
                      )
                    )
                  )
                )
              )

              val result = consignment.postProcess

              result shouldBe consignment
            }
          }

          "no items have common additional information" must {
            "not roll up additional information to consignment level" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi1",
                            text = Some("ADI 1")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi2",
                            text = Some("ADI 2")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "3",
                        declarationGoodsItemNumber = BigInt(3),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 3"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi3",
                            text = Some("ADI 3")
                          )
                        )
                      )
                    )
                  )
                )
              )

              val result = consignment.postProcess

              result shouldBe consignment
            }
          }

          // This should never happen, but need to be wary of empty.reduceLeft Exception
          "no items" must {
            "not roll up additional information to consignment level" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Nil
                  )
                )
              )

              val result = consignment.postProcess

              result shouldBe consignment
            }
          }
        }

        "during transition" when {
          "every item has a common additional information" must {
            "not roll up additional information to consignment level" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi1",
                            text = Some("ADI 1")
                          ),
                          AdditionalInformationType03(
                            sequenceNumber = "2",
                            code = "adi2",
                            text = Some("ADI 2")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        AdditionalInformation = Seq(
                          AdditionalInformationType03(
                            sequenceNumber = "1",
                            code = "adi1",
                            text = Some("ADI 1")
                          )
                        )
                      )
                    )
                  )
                )
              )

              val result = consignment.postProcess

              result shouldBe consignment
            }
          }
        }
      }

      "rollUpAdditionalReference" when {

        "during post-transition" when {

          "every item has a common additional reference" must {
            "roll up each common additional reference to consignment level and remove them from each item" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi1",
                            referenceNumber = Some("ADI 1")
                          ),
                          AdditionalReferenceType04(
                            sequenceNumber = "2",
                            typeValue = "adi2",
                            referenceNumber = Some("ADI 2")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi1",
                            referenceNumber = Some("ADI 1")
                          )
                        )
                      )
                    )
                  )
                )
              )

              val result = consignment.postProcess

              result shouldBe ConsignmentType20(
                grossMass = BigDecimal(1),
                AdditionalReference = Nil,
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi1",
                            referenceNumber = Some("ADI 1")
                          ),
                          AdditionalReferenceType04(
                            sequenceNumber = "2",
                            typeValue = "adi2",
                            referenceNumber = Some("ADI 2")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi1",
                            referenceNumber = Some("ADI 1")
                          )
                        )
                      )
                    )
                  )
                )
              )
            }
          }

          "some items have common additional reference" must {
            "not roll up additional reference to consignment level" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi1",
                            referenceNumber = Some("ADI 1")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi1",
                            referenceNumber = Some("ADI 1")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "3",
                        declarationGoodsItemNumber = BigInt(3),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 3"
                        ),
                        AdditionalReference = Nil
                      )
                    )
                  )
                )
              )

              val result = consignment.postProcess

              result shouldBe consignment
            }
          }

          "no items have common additional reference" must {
            "not roll up additional reference to consignment level" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi1",
                            referenceNumber = Some("ADI 1")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi2",
                            referenceNumber = Some("ADI 2")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "3",
                        declarationGoodsItemNumber = BigInt(3),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 3"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi3",
                            referenceNumber = Some("ADI 3")
                          )
                        )
                      )
                    )
                  )
                )
              )

              val result = consignment.postProcess

              result shouldBe consignment
            }
          }

          // This should never happen, but need to be wary of empty.reduceLeft Exception
          "no items" must {
            "not roll up additional reference to consignment level" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Nil
                  )
                )
              )

              val result = consignment.postProcess

              result shouldBe consignment
            }
          }
        }

        "during transition" when {
          "every item has a common additional reference" must {
            "not roll up additional reference to consignment level" in {
              val consignment = ConsignmentType20(
                grossMass = BigDecimal(1),
                HouseConsignment = Seq(
                  HouseConsignmentType10(
                    sequenceNumber = "1",
                    grossMass = BigDecimal(1),
                    ConsignmentItem = Seq(
                      ConsignmentItemType09(
                        goodsItemNumber = "1",
                        declarationGoodsItemNumber = BigInt(1),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 1"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi1",
                            referenceNumber = Some("ADI 1")
                          ),
                          AdditionalReferenceType04(
                            sequenceNumber = "2",
                            typeValue = "adi2",
                            referenceNumber = Some("ADI 2")
                          )
                        )
                      ),
                      ConsignmentItemType09(
                        goodsItemNumber = "2",
                        declarationGoodsItemNumber = BigInt(2),
                        Commodity = CommodityType07(
                          descriptionOfGoods = "Item 2"
                        ),
                        AdditionalReference = Seq(
                          AdditionalReferenceType04(
                            sequenceNumber = "1",
                            typeValue = "adi1",
                            referenceNumber = Some("ADI 1")
                          )
                        )
                      )
                    )
                  )
                )
              )

              val result = consignment.postProcess

              result shouldBe consignment
            }
          }
        }
      }
    }

    "activeBorderTransportMeansType02 reads is called" when {

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
            |        },
            |        {
            |          "identification" : {
            |            "code": "80",
            |            "description": "European Vessel Identification Number (ENI Code)"
            |          }
            |        }
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin)

        val result: Seq[ActiveBorderTransportMeansType02] =
          json.as[Seq[ActiveBorderTransportMeansType02]](activeBorderTransportMeansReads)

        result shouldBe Seq(
          ActiveBorderTransportMeansType02(
            sequenceNumber = "1",
            customsOfficeAtBorderReferenceNumber = Some("IT018101"),
            typeOfIdentification = Some("10"),
            identificationNumber = Some("active id number"),
            nationality = Some("ES"),
            conveyanceReferenceNumber = Some("conveyance ref number")
          ),
          ActiveBorderTransportMeansType02(
            sequenceNumber = "2",
            customsOfficeAtBorderReferenceNumber = None,
            typeOfIdentification = Some("80"),
            identificationNumber = None,
            nationality = None,
            conveyanceReferenceNumber = None
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
            |        },
            |        {
            |          "identification" : {
            |            "code": "80",
            |            "description": "European Vessel Identification Number (ENI Code)"
            |          }
            |        }
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin)

        val result: Seq[ActiveBorderTransportMeansType02] =
          json.as[Seq[ActiveBorderTransportMeansType02]](activeBorderTransportMeansReads)

        result shouldBe Seq(
          ActiveBorderTransportMeansType02(
            sequenceNumber = "1",
            customsOfficeAtBorderReferenceNumber = Some("IT018101"),
            typeOfIdentification = Some("21"),
            identificationNumber = Some("active id number"),
            nationality = Some("ES"),
            conveyanceReferenceNumber = Some("conveyance ref number")
          ),
          ActiveBorderTransportMeansType02(
            sequenceNumber = "2",
            customsOfficeAtBorderReferenceNumber = None,
            typeOfIdentification = Some("80"),
            identificationNumber = None,
            nationality = None,
            conveyanceReferenceNumber = None
          )
        )
      }

      "identification is undefined" in {
        val json: JsValue = Json.parse(s"""
            |{
            |  "transportDetails" : {
            |    "transportMeans" : {
            |      "active" : [
            |        {
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
            |        },
            |        {
            |          "identification" : {
            |            "code": "80",
            |            "description": "European Vessel Identification Number (ENI Code)"
            |          }
            |        }
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin)

        val result: Seq[ActiveBorderTransportMeansType02] =
          json.as[Seq[ActiveBorderTransportMeansType02]](activeBorderTransportMeansReads)

        result shouldBe Seq(
          ActiveBorderTransportMeansType02(
            sequenceNumber = "1",
            customsOfficeAtBorderReferenceNumber = Some("IT018101"),
            typeOfIdentification = None,
            identificationNumber = Some("active id number"),
            nationality = Some("ES"),
            conveyanceReferenceNumber = Some("conveyance ref number")
          ),
          ActiveBorderTransportMeansType02(
            sequenceNumber = "2",
            customsOfficeAtBorderReferenceNumber = None,
            typeOfIdentification = Some("80"),
            identificationNumber = None,
            nationality = None,
            conveyanceReferenceNumber = None
          )
        )
      }
    }

    "locationOfGoodsType05 reads is called" when {
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

        val result = json.as[LocationOfGoodsType05](locationOfGoodsType05.reads)

        result shouldBe LocationOfGoodsType05(
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

        val result = json.as[LocationOfGoodsType05](locationOfGoodsType05.reads)

        result shouldBe LocationOfGoodsType05(
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
            |  "items" : [
            |    {},
            |    {},
            |    {}
            |  ]
            |}
            |""".stripMargin)

        val result = json.as[Seq[TransportEquipmentType06]](transportEquipmentReads)

        result shouldBe Nil
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
            |  "items" : [
            |    {
            |      "inferredTransportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
            |    },
            |    {
            |      "inferredTransportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
            |    },
            |    {
            |      "inferredTransportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val result = json.as[Seq[TransportEquipmentType06]](transportEquipmentReads)

        result shouldBe Seq(
          TransportEquipmentType06(
            sequenceNumber = "1",
            containerIdentificationNumber = Some("container id 1"),
            numberOfSeals = BigInt(2),
            Seal = Seq(
              SealType05("1", "seal 1"),
              SealType05("2", "seal 2")
            ),
            GoodsReference = Seq(
              GoodsReferenceType02("1", 1),
              GoodsReferenceType02("2", 2),
              GoodsReferenceType02("3", 3)
            )
          )
        )
      }

      "there are multiple transport equipments" in {
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
            |  "items" : [
            |    {
            |      "transportEquipment" : "00602057-2652-43f4-8fe5-d97460d708ec"
            |    },
            |    {
            |      "transportEquipment" : "b8f72766-3781-49f2-8788-db8913d41f8c"
            |    },
            |    {
            |      "transportEquipment" : "ea575adc-1ab8-4d78-bd76-5eb893def371"
            |    },
            |    {
            |      "transportEquipment" : "00602057-2652-43f4-8fe5-d97460d708ec"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val result = json.as[Seq[TransportEquipmentType06]](transportEquipmentReads)

        result shouldBe Seq(
          TransportEquipmentType06(
            sequenceNumber = "1",
            containerIdentificationNumber = Some("container id 1"),
            numberOfSeals = BigInt(0),
            Seal = Nil,
            GoodsReference = Seq(
              GoodsReferenceType02("1", 3)
            )
          ),
          TransportEquipmentType06(
            sequenceNumber = "2",
            containerIdentificationNumber = Some("container id 2"),
            numberOfSeals = BigInt(0),
            Seal = Nil,
            GoodsReference = Seq(
              GoodsReferenceType02("1", 2)
            )
          ),
          TransportEquipmentType06(
            sequenceNumber = "3",
            containerIdentificationNumber = Some("container id 3"),
            numberOfSeals = BigInt(0),
            Seal = Nil,
            GoodsReference = Seq(
              GoodsReferenceType02("1", 1),
              GoodsReferenceType02("2", 4)
            )
          )
        )
      }
    }

    "commodityType07 reads is called" when {
      "commodity code defined" in {
        val json = Json.parse("""
            |{
            |  "description" : "Description",
            |  "commodityCode" : "commodity code",
            |  "combinedNomenclatureCode" : "CN code"
            |}
            |""".stripMargin)

        val result = json.as[CommodityType07](commodityType07.reads)

        result.CommodityCode.value shouldBe CommodityCodeType02(
          harmonizedSystemSubHeadingCode = "commodity code",
          combinedNomenclatureCode = Some("CN code")
        )
      }

      "commodity code undefined" in {
        val json = Json.parse("""
            |{
            |  "description" : "Description"
            |}
            |""".stripMargin)

        val result = json.as[CommodityType07](commodityType07.reads)

        result.CommodityCode shouldBe None
      }
    }

    "departureTransportMeansReads is called" when {
      "there are no departure transport means" in {
        val json = Json.parse("""
            |{
            |  "transportDetails" : {}
            |}
            |""".stripMargin)

        val result = json.as[Seq[DepartureTransportMeansType03]](consignmentType20.departureTransportMeansReads)

        result shouldBe Nil
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

        val result = json.as[Seq[DepartureTransportMeansType03]](consignmentType20.departureTransportMeansReads)

        result shouldBe Seq(
          DepartureTransportMeansType03(
            sequenceNumber = "1",
            typeOfIdentification = Some("10"),
            identificationNumber = Some("means id number"),
            nationality = Some("FR")
          )
        )
      }
    }
  }
}
