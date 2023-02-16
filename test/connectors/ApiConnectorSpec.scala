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

import base.AppWithDefaultMockFixtures
import com.github.tomakehurst.wiremock.client.WireMock._
import helper.WireMockServerHandler
import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.{BadRequest, InternalServerError}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpResponse, UpstreamErrorResponse}

class ApiConnectorSpec extends AnyFreeSpec with AppWithDefaultMockFixtures with WireMockServerHandler with Matchers {

  val lrn        = "lrn"
  val eoriNumber = "eori"
  val uuid       = "2e8ede47-dbfb-44ea-a1e3-6c57b1fe6fe2"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val json: JsValue = Json.parse(s"""
                                    |{
                                    |  "_id" : "$uuid",
                                    |  "lrn" : "$lrn",
                                    |  "eoriNumber" : "$eoriNumber",
                                    |  "data" : {
                                    |    "preTaskList" : {
                                    |      "officeOfDeparture" : {
                                    |        "id" : "XI000142",
                                    |        "name" : "Belfast EPU",
                                    |        "phoneNumber" : "+44 (0)02896 931537"
                                    |      },
                                    |      "procedureType" : "normal",
                                    |      "declarationType" : "TIR",
                                    |      "tirCarnetReference" : "1234567",
                                    |      "securityDetailsType" : "entrySummaryDeclaration",
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
                                    |        "typeOfLocation" : "designatedLocation",
                                    |        "qualifierOfIdentification" : "customsOfficeIdentifier",
                                    |        "identifier" : {
                                    |          "customsOffice" : {
                                    |            "id" : "XI000142",
                                    |            "name" : "Belfast EPU",
                                    |            "phoneNumber" : "+44 (0)02896 931537"
                                    |          }
                                    |        }
                                    |      },
                                    |      "loading" : {
                                    |        "addUnLocodeYesNo" : false,
                                    |        "additionalInformation" : {
                                    |          "country" : {
                                    |            "code" : "GB",
                                    |            "description" : "United Kingdom"
                                    |          },
                                    |          "location" : "London"
                                    |        }
                                    |      },
                                    |      "unloading" : {
                                    |        "addUnLocodeYesNo" : false,
                                    |        "additionalInformation" : {
                                    |          "country" : {
                                    |            "code" : "GB",
                                    |            "description" : "United Kingdom"
                                    |          },
                                    |          "location" : "London"
                                    |        }
                                    |      }
                                    |    },
                                    |    "guaranteeDetails" : [
                                    |      {
                                    |        "guaranteeType" : "B"
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
                                    |      "inlandMode" : "rail",
                                    |      "transportMeansDeparture" : {
                                    |        "identification" : "trainNumber",
                                    |        "meansIdentificationNumber" : "1234567",
                                    |        "vehicleCountry" : {
                                    |          "code" : "GB",
                                    |          "desc" : "United Kingdom"
                                    |        }
                                    |      },
                                    |      "borderModeOfTransport" : "air",
                                    |      "transportMeansActiveList" : [
                                    |        {
                                    |          "identification" : "regNumberAircraft",
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
                                    |            "authorisationType" : "TRD",
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

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.common-transit-convention-traders.port" -> server.port())

  private lazy val connector: ApiConnector = app.injector.instanceOf[ApiConnector]

  val departureId: String = "someid"

  val expected: String = Json
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

  val uri = "/movements/departures"

  "ApiConnector" - {

    "submitDeclaration is called" - {

      "for success" in {

        server.stubFor(post(urlEqualTo(uri)).willReturn(okJson(expected)))

        val res = await(connector.submitDeclaration(uA))
        res.toString mustBe Right(HttpResponse(OK, expected)).toString

      }

      "for bad request" in {

        server.stubFor(post(urlEqualTo(uri)).willReturn(badRequest()))

        val res = await(connector.submitDeclaration(uA))
        res mustBe Left(BadRequest("ApiConnector:submitDeclaration: bad request"))

      }

      "for internal server error" in {

        server.stubFor(post(urlEqualTo(uri)).willReturn(serverError()))

        val res = await(connector.submitDeclaration(uA))
        res mustBe Left(InternalServerError("ApiConnector:submitDeclaration: something went wrong"))

      }

    }

  }

}
