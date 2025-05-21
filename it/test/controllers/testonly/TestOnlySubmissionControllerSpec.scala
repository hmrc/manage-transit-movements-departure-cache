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

package controllers.testonly

import itbase.CacheRepositorySpecBase
import models.{SensitiveFormats, UserAnswers}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.JsonBodyWritables.*
import play.api.libs.ws.WSClient
import play.api.test.Helpers.running

class TestOnlySubmissionControllerSpec extends CacheRepositorySpecBase {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")

  "POST /test-only/declaration/submit" when {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/test-only/declaration/submit"

    "body cannot be read as UserAnswers" should {
      "respond with 400 status" in {
        val json = Json.parse("""
            |{
            |  "foo" : "bar"
            |}
            |""".stripMargin)
        val response = wsClient
          .url(url)
          .post(json)
          .futureValue

        response.status shouldEqual 400
      }
    }

    "body cannot be transformed to XML" should {
      "respond with 500 status" in {
        val response = wsClient
          .url(url)
          .post(Json.toJson(emptyUserAnswers))
          .futureValue

        response.status shouldEqual 500
      }
    }

    "body is valid" when {

      val data = Json
        .parse("""
            |{
            |  "preTaskList": {
            |    "additionalDeclarationType": {
            |      "code": "A",
            |      "description": "for a standard customs declaration (under Article 162 of the Code)"
            |    },
            |    "officeOfDeparture": {
            |      "id": "GB000060",
            |      "name": "Dover/Folkestone Eurotunnel Freight",
            |      "phoneNumber": "+44 (0)3001060726/27/28/30/31/32",
            |      "countryId": "GB",
            |      "isInCL112": true,
            |      "isInCL147": false,
            |      "isInCL010": false
            |    },
            |    "procedureType": "simplified",
            |    "declarationType": {
            |      "code": "T2",
            |      "description": "Goods having the customs status of Union goods, which are placed under the common transit procedure"
            |    },
            |    "securityDetailsType": {
            |      "code": "2",
            |      "description": "EXS"
            |    },
            |    "detailsConfirmed": true
            |  },
            |  "traderDetails": {
            |    "holderOfTransit": {
            |      "eoriYesNo": false,
            |      "name": "Joe Blog",
            |      "country": {
            |        "code": "GB",
            |        "description": "United Kingdom"
            |      },
            |      "address": {
            |        "numberAndStreet": "1 Church lane",
            |        "city": "Godrics Hollow",
            |        "postalCode": "BA1 0AA"
            |      },
            |      "addContact": false
            |    },
            |    "actingAsRepresentative": true,
            |    "representative": {
            |      "eori": "FR123123132",
            |      "addDetails": true,
            |      "name": "Marie Rep",
            |      "telephoneNumber": "+11 1111 1111"
            |    },
            |    "consignment": {
            |      "approvedOperator": true,
            |      "consignor": {
            |        "eoriYesNo": false,
            |        "name": "Pip Consignor",
            |        "country": {
            |          "code": "GB",
            |          "description": "United Kingdom"
            |        },
            |        "address": {
            |          "numberAndStreet": "1 Merry Lane",
            |          "city": "Godrics Hollow",
            |          "postalCode": "CA1 9AA"
            |        },
            |        "addContact": true,
            |        "contact": {
            |          "name": "Pip Contact",
            |          "telephoneNumber": "+123123123213"
            |        }
            |      },
            |      "consignee": {
            |        "eoriYesNo": false,
            |        "name": "Simpson Blog Consignee",
            |        "country": {
            |          "code": "GB",
            |          "description": "United Kingdom"
            |        },
            |        "address": {
            |          "numberAndStreet": "1 Merry Lane",
            |          "city": "Godrics Hollow",
            |          "postalCode": "CA1 9AA"
            |        }
            |      }
            |    }
            |  },
            |  "routeDetails": {
            |    "addSpecificCircumstanceIndicatorYesNo": true,
            |    "specificCircumstanceIndicator": {
            |      "code": "XXX",
            |      "description": "Authorised economic operators"
            |    },
            |    "routing": {
            |      "countryOfDestination": {
            |        "code": "IT",
            |        "description": "Italy"
            |      },
            |      "officeOfDestination": {
            |        "id": "IT262101",
            |        "name": "AEROPORTO",
            |        "phoneNumber": "0039 0106015339",
            |        "countryId": "IT",
            |        "isInCL112": false
            |      },
            |      "bindingItinerary": true,
            |      "countriesOfRouting": [
            |        {
            |          "countryOfRouting": {
            |            "code": "FR",
            |            "description": "France",
            |            "isInCL112": false,
            |            "isInCL147": true
            |          }
            |        },
            |        {
            |          "countryOfRouting": {
            |            "code": "DE",
            |            "description": "Germany",
            |            "isInCL112": false,
            |            "isInCL147": true
            |          }
            |        }
            |      ]
            |    },
            |    "transit": {
            |      "officesOfTransit": [
            |        {
            |          "officeOfTransitCountry": {
            |            "code": "DE",
            |            "description": "Germany"
            |          },
            |          "officeOfTransit": {
            |            "id": "DE004058",
            |            "name": "Basel",
            |            "phoneNumber": "+41 61 20114-24",
            |            "countryId": "DE",
            |            "isInCL147": true,
            |            "isInCL010": false
            |          },
            |          "addOfficeOfTransitETAYesNo": true,
            |          "arrivalDateTime": "2023-09-26T15:01:00"
            |        }
            |      ]
            |    },
            |    "exit": {
            |      "addCustomsOfficeOfExitYesNoPage": true,
            |      "officesOfExit": [
            |        {
            |          "officeOfExitCountry": {
            |            "code": "FR",
            |            "description": "France"
            |          },
            |          "officeOfExit": {
            |            "id": "FR005130",
            |            "name": "Agen bureau",
            |            "phoneNumber": "09 70 27 57 70",
            |            "countryId": "FR"
            |          }
            |        }
            |      ]
            |    },
            |    "locationOfGoods": {
            |      "typeOfLocation": {
            |        "type": "B",
            |        "description": "Authorised place"
            |      },
            |      "inferredQualifierOfIdentification": {
            |        "qualifier": "Y",
            |        "description": "Authorisation number"
            |      },
            |      "identifier": {
            |        "authorisationNumber": "1234567890",
            |        "addAdditionalIdentifier": true,
            |        "additionalIdentifier": "x9x9"
            |      },
            |      "addContact": false
            |    },
            |    "loadingAndUnloading": {
            |      "loading": {
            |        "addUnLocodeYesNo": true,
            |        "additionalInformation": {
            |          "country": {
            |            "code": "GB",
            |            "description": "United Kingdom"
            |          },
            |          "location": "London"
            |        },
            |        "unLocode": "AEFAT",
            |        "addLocationYesNo": true
            |      },
            |      "addPlaceOfUnloading": true,
            |      "unloading": {
            |        "addUnLocodeYesNo": true,
            |        "additionalInformation": {
            |          "country": {
            |            "code": "IT",
            |            "description": "Italy"
            |          },
            |          "location": "Milano"
            |        },
            |        "unLocode": "DEAAL",
            |        "addExtraInformationYesNo": true
            |      }
            |    }
            |  },
            |  "guaranteeDetails": {
            |    "guaranteeDetails": [
            |      {
            |        "guaranteeType": {
            |          "code": "1",
            |          "description": "Comprehensive guarantee"
            |        },
            |        "referenceNumber": "01GB1234567890120A123456",
            |        "currency": {
            |          "currency": "GBP",
            |          "description": "Pound Sterling"
            |        },
            |        "liabilityAmount": 100,
            |        "accessCode": "AC01"
            |      },
            |      {
            |        "guaranteeType": {
            |          "code": "8",
            |          "description": "Guarantee not required for certain public bodies"
            |        },
            |        "otherReference": "01GB123456789012",
            |        "currency": {
            |          "currency": "GBP",
            |          "description": "Pound Sterling"
            |        },
            |        "liabilityAmount": 123
            |      },
            |      {
            |        "guaranteeType": {
            |          "code": "3",
            |          "description": "Individual guarantee in cash or other means of payment recognised by the customs authorities as being equivalent to a cash deposit, made in euro or in the currency of the Member State in which the guarantee is registered."
            |        },
            |        "otherReferenceYesNo": true,
            |        "otherReference": "01GB123456789012",
            |        "currency": {
            |          "currency": "GBP",
            |          "description": "Pound Sterling"
            |        },
            |        "liabilityAmount": 54.99
            |      },
            |      {
            |        "guaranteeType": {
            |          "code": "5",
            |          "description": "Guarantee waiver where the amount of import or export duty to be secured does not exceed the statistical value threshold for declarations laid down in accordance with Article 3(4) of Regulation (EC) No 471/2009"
            |        },
            |        "currency": {
            |          "currency": "GBP",
            |          "description": "Pound Sterling"
            |        },
            |        "liabilityAmount": 54.99
            |      }
            |    ],
            |    "addAnotherGuarantee": false
            |  },
            |  "transportDetails": {
            |    "preRequisites": {
            |      "sameUcrYesNo": true,
            |      "uniqueConsignmentReference": "GB123456123456",
            |      "sameCountryOfDispatchYesNo": true,
            |      "countryOfDispatch": {
            |        "code": "GB",
            |        "description": "United Kingdom"
            |      },
            |      "transportedToSameCountryYesNo": true,
            |      "itemsDestinationCountry": {
            |        "code": "IT",
            |        "description": "Italy",
            |        "isInCL009": true
            |      },
            |      "containerIndicator": true,
            |      "addCountryOfDestination": true
            |    },
            |    "equipmentsAndCharges": {
            |      "equipments": [
            |        {
            |          "containerIdentificationNumber": "C001",
            |          "uuid": "25c9018d-4b58-4561-b263-e45d40c4cbc6",
            |          "addSealsYesNo": true,
            |          "seals": [
            |            {
            |              "identificationNumber": "S002"
            |            }
            |          ]
            |        },
            |        {
            |          "addContainerIdentificationNumberYesNo": true,
            |          "uuid": "d1f2000f-3a8b-41e6-84bc-bbdb5fb17510",
            |          "containerIdentificationNumber": "C002",
            |          "addSealsYesNo": false
            |        }
            |      ],
            |      "addPaymentMethodYesNo": false
            |    },
            |    "addInlandModeYesNo": true,
            |    "inlandMode": {
            |      "code": "3",
            |      "description": "Road transport"
            |    },
            |    "borderModeOfTransport": {
            |      "code": "1",
            |      "description": "Maritime Transport"
            |    },
            |    "transportMeans": {
            |      "addDepartureTransportMeansYesNo": true,
            |      "departure": [
            |        {
            |          "addIdentificationTypeYesNo": true,
            |          "addIdentificationNumberYesNo": true,
            |          "identification": {
            |            "type": "31",
            |            "description": "Registration Number of the Road Trailer"
            |          },
            |          "meansIdentificationNumber": "GB1234567",
            |          "addVehicleCountryYesNo": true,
            |          "vehicleCountry": {
            |            "code": "GB",
            |            "description": "United Kingdom"
            |          }
            |        }
            |      ],
            |      "active": [
            |        {
            |          "identification": {
            |            "code": "11",
            |            "description": "Name of the sea-going vessel"
            |          },
            |          "identificationNumber": "GB1234567",
            |          "addNationalityYesNo": true,
            |          "nationality": {
            |            "code": "GB",
            |            "description": "United Kingdom"
            |          },
            |          "customsOfficeActiveBorder": {
            |            "id": "DE004058",
            |            "name": "Basel",
            |            "phoneNumber": "+41 61 20114-24",
            |            "countryId": "DE"
            |          },
            |          "conveyanceReferenceNumberYesNo": true,
            |          "conveyanceReferenceNumber": "GB123456123456"
            |        }
            |      ]
            |    },
            |    "supplyChainActorYesNo": true,
            |    "supplyChainActors": [
            |      {
            |        "supplyChainActorType": {
            |          "role": "CS",
            |          "description": "Consolidator"
            |        },
            |        "identificationNumber": "FR98472189002"
            |      }
            |    ],
            |    "authorisationsAndLimit": {
            |      "authorisations": [
            |        {
            |          "inferredAuthorisationType": {
            |            "code": "C521",
            |            "description": "ACR - Authorisation for the status of authorised consignor for Union transit (Column 9b, Annex A of Delegated Regulation (EU) 2015/2446)"
            |          },
            |          "authorisationReferenceNumber": "ACR123"
            |        }
            |      ],
            |      "authorisationsInferred": true,
            |      "limit": {
            |        "limitDate": "2023-09-28"
            |      }
            |    },
            |    "carrierDetailYesNo": true,
            |    "carrierDetails": {
            |      "identificationNumber": "GB123456123456",
            |      "addContactYesNo": true,
            |      "contact": {
            |        "name": "Moseley",
            |        "telephoneNumber": "+88 888 888"
            |      }
            |    },
            |    "addAdditionalInformationYesNo": false,
            |    "addAdditionalReferenceYesNo": false
            |  },
            |  "documents": {
            |    "documents": [
            |      {
            |        "attachToAllItems": false,
            |        "previousDocumentType": {
            |          "type": "Previous",
            |          "code": "C605",
            |          "description": "Information sheet INF3"
            |        },
            |        "details": {
            |          "documentReferenceNumber": "1234",
            |          "uuid": "88880074-eb56-4bed-bf51-aec0e6a2ed40",
            |          "addTypeOfPackageYesNo": true,
            |          "packageType": {
            |            "code": "BG",
            |            "description": "Bag"
            |          },
            |          "addNumberOfPackagesYesNo": true,
            |          "numberOfPackages": 50,
            |          "declareQuantityOfGoodsYesNo": true,
            |          "metric": {
            |            "code": "GRM",
            |            "description": "Gram"
            |          },
            |          "quantity": 1500,
            |          "addAdditionalInformationYesNo": false
            |        }
            |      },
            |      {
            |        "attachToAllItems": false,
            |        "type": {
            |          "type": "Support",
            |          "code": "N003",
            |          "description": "Certificate of quality"
            |        },
            |        "details": {
            |          "documentReferenceNumber": "98765",
            |          "uuid": "ff2110fa-8e75-4db2-be00-3e2006557e4e",
            |          "addLineItemNumberYesNo": true,
            |          "lineItemNumber": 10101,
            |          "addAdditionalInformationYesNo": true,
            |          "additionalInformation": "12345"
            |        }
            |      },
            |      {
            |        "attachToAllItems": true,
            |        "type": {
            |          "type": "Transport",
            |          "code": "N741",
            |          "description": "Master airwaybill"
            |        },
            |        "details": {
            |          "documentReferenceNumber": "1234",
            |          "uuid": "15db6313-c0e1-4c06-895e-f415e8fba345"
            |        }
            |      }
            |    ],
            |    "addAnotherDocument": false
            |  },
            |  "items": {
            |    "items": [
            |      {
            |        "description": "This is a description",
            |        "transportEquipment": "25c9018d-4b58-4561-b263-e45d40c4cbc6",
            |        "addCUSCodeYesNo": false,
            |        "commodityCode": "ABC123",
            |        "addDangerousGoodsYesNo": false,
            |        "grossWeight": 100,
            |        "addSupplementaryUnitsYesNo": false,
            |        "packages": [
            |          {
            |            "packageType": {
            |              "code": "AE",
            |              "description": "Aerosol",
            |              "type": "Other"
            |            },
            |            "numberOfPackages": 10,
            |            "shippingMark": "2nd item shipping mark cargo description"
            |          }
            |        ],
            |        "addSupplyChainActorYesNo": false,
            |        "documents": [
            |          {
            |            "document": "88880074-eb56-4bed-bf51-aec0e6a2ed40"
            |          }
            |        ],
            |        "documentsInProgress": false,
            |        "addAdditionalReferenceYesNo": false,
            |        "addAdditionalInformationYesNo": false,
            |        "countryOfDispatch": {
            |          "code": "GB",
            |          "description": "United Kingdom"
            |        }
            |      }
            |    ],
            |    "addAnotherItem": false
            |  }
            |}
            |""".stripMargin)
        .as[JsObject]

      "body is not encrypted" should {
        "respond with 200 status" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val metadata    = emptyMetadata.copy(data = data)
            val userAnswers = emptyUserAnswers.copy(metadata = metadata)

            val wsClient: WSClient = app.injector.instanceOf[WSClient]

            implicit val sensitiveFormats: SensitiveFormats = app.injector.instanceOf[SensitiveFormats]

            val response = wsClient
              .url(url)
              .post(Json.toJson(userAnswers)(UserAnswers.sensitiveFormat))
              .futureValue

            response.status shouldEqual 200
          }
        }
      }

      "body is encrypted" should {
        "respond with 200 status" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val metadata    = emptyMetadata.copy(data = data)
            val userAnswers = emptyUserAnswers.copy(metadata = metadata)

            val wsClient: WSClient = app.injector.instanceOf[WSClient]

            implicit val sensitiveFormats: SensitiveFormats = app.injector.instanceOf[SensitiveFormats]

            val response = wsClient
              .url(url)
              .post(Json.toJson(userAnswers)(UserAnswers.sensitiveFormat))
              .futureValue

            response.status shouldEqual 200
          }
        }
      }
    }
  }
}
