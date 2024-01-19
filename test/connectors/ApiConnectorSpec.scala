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

package connectors

import base.{AppWithDefaultMockFixtures, SpecBase}
import com.github.tomakehurst.wiremock.client.WireMock._
import helper.WireMockServerHandler
import models.{Departure, Departures, UserAnswers}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.{BadRequest, InternalServerError}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse

class ApiConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockServerHandler {

  private val json: JsValue = Json.parse(s"""
    |{
    |  "_id" : "$uuid",
    |  "lrn" : "$lrn",
    |  "eoriNumber" : "$eoriNumber",
    |  "isSubmitted": "notSubmitted",
    |  "data" : {
    |    "preTaskList" : {
    |      "officeOfDeparture" : {
    |        "id" : "XI000142",
    |        "name" : "Belfast EPU",
    |        "phoneNumber" : "+44 (0)02896 931537"
    |      },
    |      "procedureType" : "normal",
    |      "declarationType" : {
    |        "code" : "TIR"
    |      },
    |      "additionalDeclarationType" : {
    |        "code" : "A"
    |      },
    |      "tirCarnetReference" : "1234567",
    |      "securityDetailsType" : {
    |        "code" : "1"
    |      },
    |      "detailsConfirmed" : true
    |    },
    |    "traderDetails" : {
    |      "holderOfTransit" : {
    |        "tirIdentificationYesNo" : true,
    |        "tirIdentification" : "ABC/123/12345",
    |        "name" : "Joe Blog",
    |        "country" : {
    |          "code" : "GB",
    |          "description" : "United Kingdom"
    |        },
    |        "address" : {
    |          "numberAndStreet" : "1 Church lane",
    |          "city" : "Godrics Hollow",
    |          "postalCode" : "BA1 0AA"
    |        },
    |        "addContact" : true,
    |        "contact" : {
    |          "name" : "John contact",
    |          "telephoneNumber" : "+2112212112"
    |        }
    |      },
    |      "actingAsRepresentative" : true,
    |      "representative" : {
    |        "eori" : "FR123123132",
    |        "name" : "Marie Rep",
    |        "capacity" : "indirect",
    |        "telephoneNumber" : "+11 1111 1111"
    |      },
    |      "consignment" : {
    |        "consignor" : {
    |          "eoriYesNo" : true,
    |          "eori" : "IT12312313",
    |          "name" : "Pip Consignor",
    |          "country" : {
    |            "code" : "GB",
    |            "description" : "United Kingdom"
    |          },
    |          "address" : {
    |            "numberAndStreet" : "1 Merry Lane",
    |            "city" : "Godrics Hollow",
    |            "postalCode" : "CA1 9AA"
    |          },
    |          "addContact" : true,
    |          "contact" : {
    |            "name" : "Pip Contact",
    |            "telephoneNumber" : "+123123123213"
    |          }
    |        },
    |        "moreThanOneConsignee" : false,
    |        "consignee" : {
    |          "eoriYesNo" : true,
    |          "eori" : "GE00101001",
    |          "name" : "Simpson Blog Consignee",
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
    |      }
    |    },
    |    "routeDetails" : {
    |      "routing" : {
    |        "countryOfDestination" : {
    |          "code" : "IT",
    |          "description" : "Italy"
    |        },
    |        "officeOfDestination" : {
    |          "id" : "IT018101",
    |          "name" : "Aeroporto Bari - Palese",
    |          "phoneNumber" : "0039 0805316196"
    |        },
    |        "bindingItinerary" : false,
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
    |      "countriesInSecurityAgreement" : false,
    |      "addLocationOfGoods" : true,
    |      "locationOfGoods" : {
    |        "typeOfLocation" : {
    |          "type": "A",
    |          "description": "Designated location"
    |        },
    |        "qualifierOfIdentification" : {
    |          "qualifier": "V",
    |          "description": "Customs office identifier"
    |        },
    |        "identifier" : {
    |          "customsOffice" : {
    |            "id" : "XI000142",
    |            "name" : "Belfast EPU",
    |            "phoneNumber" : "+44 (0)02896 931537"
    |          }
    |        }
    |      },
    |      "loadingAndUnloading" : {
    |        "loading" : {
    |          "addUnLocodeYesNo" : false,
    |          "additionalInformation" : {
    |            "country" : {
    |              "code" : "GB",
    |              "description" : "United Kingdom"
    |            },
    |            "location" : "London"
    |          }
    |        },
    |        "unloading" : {
    |          "addUnLocodeYesNo" : false,
    |          "additionalInformation" : {
    |            "country" : {
    |              "code" : "GB",
    |              "description" : "United Kingdom"
    |            },
    |            "location" : "London"
    |          }
    |        }
    |      }
    |    },
    |    "guaranteeDetails" : [
    |      {
    |        "guaranteeType" : {
    |          "code" : "B",
    |          "description": "Guarantee for goods dispatched under TIR procedure"
    |        }
    |      }
    |    ],
    |    "transportDetails" : {
    |      "preRequisites" : {
    |        "sameUcrYesNo" : true,
    |        "uniqueConsignmentReference" : "GB123456123456",
    |        "countryOfDispatch" : {
    |          "code" : "GB",
    |          "description" : "United Kingdom"
    |        },
    |        "transportedToSameCountryYesNo" : true,
    |        "itemsDestinationCountry" : {
    |          "code" : "GB",
    |          "description" : "United Kingdom"
    |        },
    |        "containerIndicator" : true
    |      },
    |      "inlandMode" : {
    |        "code": "2",
    |        "description": "Rail Transport"
    |      },
    |      "transportMeansDeparture" : {
    |        "identification" : {
    |          "type": "21",
    |          "description": "Train Number"
    |        },
    |        "meansIdentificationNumber" : "1234567",
    |        "vehicleCountry" : {
    |          "code" : "GB",
    |          "desc" : "United Kingdom"
    |        }
    |      },
    |      "borderModeOfTransport" : {
    |        "code": "4",
    |        "description": "Air transport"
    |      },
    |      "transportMeansActiveList" : [
    |        {
    |          "identification" : {
    |            "code": "41",
    |            "description": "Registration Number of the Aircraft"
    |          },
    |          "identificationNumber" : "GB1234567",
    |          "addNationalityYesNo" : true,
    |          "nationality" : {
    |            "code" : "GB",
    |            "desc" : "United Kingdom"
    |          },
    |          "customsOfficeActiveBorder" : {
    |            "id" : "IT018101",
    |            "name" : "Aeroporto Bari - Palese",
    |            "phoneNumber" : "0039 0805316196"
    |          },
    |          "conveyanceReferenceNumber" : "GB123456123456"
    |        }
    |      ],
    |      "supplyChainActorYesNo" : false,
    |      "addAuthorisationsYesNo" : true,
    |      "authorisationsAndLimit" : {
    |        "limit": {
    |          "limitDate": "2023-01-01"
    |        },
    |        "authorisations" : [
    |          {
    |            "authorisationType" : {
    |              "code": "C524",
    |              "description": "TRD - Authorisation to use transit declaration with a reduced dataset (Column 9e, Annex A of Delegated Regulation (EU) 2015/2446)"
    |            },
    |            "authorisationReferenceNumber" : "TRD123"
    |          }
    |        ]
    |      },
    |      "carrierDetails" : {
    |        "identificationNumber" : "GB123456123456",
    |        "addContactYesNo" : true,
    |        "contact" : {
    |          "name" : "Carry",
    |          "telephoneNumber" : "+88 888 888"
    |        }
    |      }
    |    },
    |    "documents" : {
    |      "documents" : [
    |        {
    |          "type" : {
    |            "type" : "Transport",
    |            "code" : "235",
    |            "description" : "Container list"
    |          },
    |          "details" : {
    |            "documentReferenceNumber" : "transport1"
    |          }
    |        }
    |      ]
    |    },
    |    "items" : []
    |  },
    |  "tasks" : {},
    |  "createdAt" : "2022-09-05T15:58:44.188Z",
    |  "lastUpdated" : "2022-09-07T10:33:23.472Z"
    |}
    |""".stripMargin)

  private lazy val uA: UserAnswers = json.as[UserAnswers]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.common-transit-convention-traders.port" -> server.port())

  private lazy val connector: ApiConnector = app.injector.instanceOf[ApiConnector]

  private val expected: String = Json
    .obj(
      "_links" -> Json.obj(
        "self" -> Json.obj(
          "href" -> s"/customs/transits/movements/departures/$departureId"
        ),
        "messages" -> Json.obj(
          "href" -> s"/customs/transits/movements/departures/$departureId/messages"
        )
      )
    )
    .toString()
    .stripMargin

  private val uri = "/movements/departures"

  "ApiConnector" when {

    "getDepartures" must {

      val lrn1 = "3CnsTh79I7vtOy6"
      val lrn2 = "DEF456"

      val responseJson: JsValue = Json.parse(s"""
           |{
           |  "_links": {
           |    "self": {
           |      "href": "/customs/transits/movements/departures"
           |    }
           |  },
           |  "departures": [
           |    {
           |      "_links": {
           |        "self": {
           |          "href": "/customs/transits/movements/departures/63651574c3447b12"
           |        },
           |        "messages": {
           |          "href": "/customs/transits/movements/departures/63651574c3447b12/messages"
           |        }
           |      },
           |      "id": "63651574c3447b12",
           |      "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
           |      "localReferenceNumber": "$lrn1",
           |      "created": "2022-11-04T13:36:52.332Z",
           |      "updated": "2022-11-04T13:36:52.332Z",
           |      "enrollmentEORINumber": "9999912345",
           |      "movementEORINumber": "GB1234567890"
           |    },
           |    {
           |      "_links": {
           |        "self": {
           |          "href": "/customs/transits/movements/departures/6365135ba5e821ee"
           |        },
           |        "messages": {
           |          "href": "/customs/transits/movements/departures/6365135ba5e821ee/messages"
           |        }
           |      },
           |      "id": "6365135ba5e821ee",
           |      "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
           |      "localReferenceNumber": "$lrn2",
           |      "created": "2022-11-04T13:27:55.522Z",
           |      "updated": "2022-11-04T13:27:55.522Z",
           |      "enrollmentEORINumber": "9999912345",
           |      "movementEORINumber": "GB1234567890"
           |    }
           |  ]
           |}
           |""".stripMargin)

      "return Departures" in {
        server.stubFor(
          get(urlEqualTo(s"/movements/departures"))
            .willReturn(okJson(responseJson.toString()))
        )

        val expectedResult = Departures(
          Seq(
            Departure(lrn1, "movements/departures/63651574c3447b12/messages"),
            Departure(lrn2, "movements/departures/6365135ba5e821ee/messages")
          )
        )

        await(connector.getDepartures()) shouldBe expectedResult
      }
    }

    "submitDeclaration is called" when {

      "success" in {
        server.stubFor(post(urlEqualTo(uri)).willReturn(okJson(expected)))

        val res = await(connector.submitDeclaration(uA))
        res.toString shouldBe Right(HttpResponse(OK, expected)).toString
      }

      "bad request" in {
        server.stubFor(post(urlEqualTo(uri)).willReturn(badRequest()))

        val res = await(connector.submitDeclaration(uA))
        res shouldBe Left(BadRequest("ApiConnector:submitDeclaration: bad request"))
      }

      "internal server error" in {
        server.stubFor(post(urlEqualTo(uri)).willReturn(serverError()))

        val res = await(connector.submitDeclaration(uA))
        res shouldBe Left(InternalServerError("ApiConnector:submitDeclaration: something went wrong"))
      }
    }
  }
}
