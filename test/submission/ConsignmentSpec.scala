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

package submission

import api.submission.Consignment
import base.SpecBase
import generated._
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}

class ConsignmentSpec extends SpecBase {

  "Consignment" when {

    "transform is called" must {

      "convert to API format" in {

        val json: JsValue = Json.parse(s"""
            |{
            |  "_id" : "$uuid",
            |  "lrn" : "$lrn",
            |  "eoriNumber" : "$eoriNumber",
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
            |        "countryOfDestination" : {
            |          "code" : "IT",
            |          "description" : "Italy"
            |        },
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
            |        "typeOfLocation" : "designatedLocation",
            |        "qualifierOfIdentification" : "postalCode",
            |        "identifier" : {
            |          "authorisationNumber" : "authorisation number",
            |          "additionalIdentifier" : "additional identifier",
            |          "unLocode" : "UNLOCODE",
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
            |      "loading" : {
            |        "unLocode" : "UNLOCODE1",
            |        "additionalInformation" : {
            |          "country" : "Loading country",
            |          "location" : "Loading location"
            |        }
            |      },
            |      "unloading" : {
            |        "unLocode" : "UNLOCODE2",
            |        "additionalInformation" : {
            |          "country" : "Unloading country",
            |          "location" : "Unloading location"
            |        }
            |      }
            |    },
            |    "transportDetails" : {
            |      "preRequisites" : {
            |        "uniqueConsignmentReference" : "ucr123",
            |        "countryOfDispatch" : {
            |          "code" : "FR",
            |          "description" : "France"
            |        },
            |        "containerIndicator" : true
            |      },
            |      "inlandMode" : "maritime",
            |      "borderModeOfTransport" : "maritime",
            |      "carrierDetails" : {
            |        "identificationNumber" : "carrier1",
            |        "addContactYesNo" : true,
            |        "contact" : {
            |          "name" : "Carrier Contact",
            |          "telephoneNumber" : "+44 808 157 0192"
            |        }
            |      },
            |      "transportMeansDeparture" : {
            |        "identification" : "imoShipIdNumber",
            |        "meansIdentificationNumber" : "means id number",
            |        "vehicleCountry" : {
            |          "code" : "FR",
            |          "desc" : "France"
            |        }
            |      },
            |      "transportMeansActiveList" : [
            |        {
            |          "identification" : "seaGoingVessel",
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
            |      ],
            |      "supplyChainActors" : [
            |        {
            |          "supplyChainActorType" : "consolidator",
            |          "identificationNumber" : "sca1"
            |        },
            |        {
            |          "supplyChainActorType" : "freightForwarder",
            |          "identificationNumber" : "sca2"
            |        },
            |        {
            |          "supplyChainActorType" : "manufacturer",
            |          "identificationNumber" : "sca3"
            |        },
            |        {
            |          "supplyChainActorType" : "warehouseKeeper",
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
            |            "itemNumbers" : [
            |              {
            |                "itemNumber" : "1"
            |              },
            |              {
            |                "itemNumber" : "2"
            |              }
            |            ]
            |          }
            |        ],
            |        "paymentMethod" : "cash"
            |      }
            |    }
            |  },
            |  "tasks" : {},
            |  "createdAt" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662393524188"
            |    }
            |  },
            |  "lastUpdated" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662546803472"
            |    }
            |  }
            |}
            |""".stripMargin)

        val uA: UserAnswers = json.as[UserAnswers](UserAnswers.mongoFormat)

        val converted: ConsignmentType20 = Consignment.transform(uA)

        converted.countryOfDispatch shouldBe Some("FR")
        converted.countryOfDestination shouldBe Some("IT")
        converted.containerIndicator shouldBe Some(Number1)
        converted.inlandModeOfTransport shouldBe Some("1")
        converted.modeOfTransportAtTheBorder shouldBe Some("1")
        converted.grossMass shouldBe 0
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
            sequenceNumber = "0",
            role = "CS",
            identificationNumber = "sca1"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "1",
            role = "FW",
            identificationNumber = "sca2"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "2",
            role = "MF",
            identificationNumber = "sca3"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "3",
            role = "WH",
            identificationNumber = "sca4"
          )
        )

        converted.TransportEquipment shouldBe Seq(
          TransportEquipmentType06(
            sequenceNumber = "0",
            containerIdentificationNumber = Some("container id 1"),
            numberOfSeals = 2,
            Seal = Seq(
              SealType05(
                sequenceNumber = "0",
                identifier = "seal 1"
              ),
              SealType05(
                sequenceNumber = "1",
                identifier = "seal 2"
              )
            ),
            GoodsReference = Seq(
              GoodsReferenceType02(
                sequenceNumber = "0",
                declarationGoodsItemNumber = 1
              ),
              GoodsReferenceType02(
                sequenceNumber = "1",
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
            UNLocode = Some("UNLOCODE"),
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
            sequenceNumber = "0",
            typeOfIdentification = Some("10"),
            identificationNumber = Some("means id number"),
            nationality = Some("FR")
          )
        )

        converted.ActiveBorderTransportMeans shouldBe Seq(
          ActiveBorderTransportMeansType02(
            sequenceNumber = "0",
            customsOfficeAtBorderReferenceNumber = Some("IT018101"),
            typeOfIdentification = Some("11"),
            identificationNumber = Some("active id number"),
            nationality = Some("ES"),
            conveyanceReferenceNumber = Some("conveyance ref number")
          )
        )

        converted.CountryOfRoutingOfConsignment shouldBe Seq(
          CountryOfRoutingOfConsignmentType01(
            sequenceNumber = "0",
            country = "AD"
          ),
          CountryOfRoutingOfConsignmentType01(
            sequenceNumber = "1",
            country = "AR"
          )
        )

        converted.PlaceOfLoading shouldBe Some(
          PlaceOfLoadingType03(
            Some("UNLOCODE1"),
            Some("Loading country"),
            Some("Loading location")
          )
        )

        converted.PlaceOfUnloading shouldBe Some(
          PlaceOfUnloadingType01(
            Some("UNLOCODE2"),
            Some("Unloading country"),
            Some("Unloading location")
          )
        )

        converted.TransportCharges shouldBe Some(
          TransportChargesType("A")
        )
      }
    }

    "activeBorderTransportMeansType02 reads is called" when {

      "rail (2) border mode of transport" must {
        "read type of identification as trainNumber (21)" in {
          val json: JsValue = Json.parse(s"""
              |{
              |  "_id" : "$uuid",
              |  "lrn" : "$lrn",
              |  "eoriNumber" : "$eoriNumber",
              |  "data" : {
              |    "transportDetails" : {
              |      "borderModeOfTransport" : "rail",
              |      "transportMeansDeparture" : {
              |        "identification" : "wagonNumber",
              |        "meansIdentificationNumber" : "means id number",
              |        "vehicleCountry" : {
              |          "code" : "FR",
              |          "desc" : "France"
              |        }
              |      },
              |      "transportMeansActiveList" : [
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
              |        }
              |      ]
              |    }
              |  },
              |  "tasks" : {},
              |  "createdAt" : {
              |    "$$date" : {
              |      "$$numberLong" : "1662393524188"
              |    }
              |  },
              |  "lastUpdated" : {
              |    "$$date" : {
              |      "$$numberLong" : "1662546803472"
              |    }
              |  }
              |}
              |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers](UserAnswers.mongoFormat)

          val result: Seq[ActiveBorderTransportMeansType02] = Consignment.transform(uA).ActiveBorderTransportMeans

          result shouldBe Seq(
            ActiveBorderTransportMeansType02(
              sequenceNumber = "0",
              customsOfficeAtBorderReferenceNumber = Some("IT018101"),
              typeOfIdentification = Some("21"),
              identificationNumber = Some("active id number"),
              nationality = Some("ES"),
              conveyanceReferenceNumber = Some("conveyance ref number")
            )
          )
        }
      }

      "road (3) border mode of transport" must {
        "read type of identification as regNumberRoadVehicle (30)" in {
          val json: JsValue = Json.parse(s"""
              |{
              |  "_id" : "$uuid",
              |  "lrn" : "$lrn",
              |  "eoriNumber" : "$eoriNumber",
              |  "data" : {
              |    "transportDetails" : {
              |      "borderModeOfTransport" : "road",
              |      "transportMeansDeparture" : {
              |        "identification" : "regNumberRoadTrailer",
              |        "meansIdentificationNumber" : "means id number",
              |        "vehicleCountry" : {
              |          "code" : "FR",
              |          "desc" : "France"
              |        }
              |      },
              |      "transportMeansActiveList" : [
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
              |        }
              |      ]
              |    }
              |  },
              |  "tasks" : {},
              |  "createdAt" : {
              |    "$$date" : {
              |      "$$numberLong" : "1662393524188"
              |    }
              |  },
              |  "lastUpdated" : {
              |    "$$date" : {
              |      "$$numberLong" : "1662546803472"
              |    }
              |  }
              |}
              |""".stripMargin)

          val uA: UserAnswers = json.as[UserAnswers](UserAnswers.mongoFormat)

          val result: Seq[ActiveBorderTransportMeansType02] = Consignment.transform(uA).ActiveBorderTransportMeans

          result shouldBe Seq(
            ActiveBorderTransportMeansType02(
              sequenceNumber = "0",
              customsOfficeAtBorderReferenceNumber = Some("IT018101"),
              typeOfIdentification = Some("30"),
              identificationNumber = Some("active id number"),
              nationality = Some("ES"),
              conveyanceReferenceNumber = Some("conveyance ref number")
            )
          )
        }
      }
    }
  }
}
