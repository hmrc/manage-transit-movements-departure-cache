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
import models._
import org.scalacheck.Gen
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.{BadRequest, InternalServerError}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.Instant

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

    "getDepartures" - {

      val departureId1 = "63651574c3447b12"
      val departureId2 = "6365135ba5e821ee"
      val lrn1         = "3CnsTh79I7vtOy6"
      val lrn2         = "DEF456"

      val responseJson: JsValue = Json.parse(
        s"""
          {
            "_links": {
              "self": {
                "href": "/customs/transits/movements/departures"
              }
            },
            "departures": [
              {
                "_links": {
                  "self": {
                    "href": "/customs/transits/movements/departures/63651574c3447b12"
                  },
                  "messages": {
                    "href": "/customs/transits/movements/departures/63651574c3447b12/messages"
                  }
                },
                "id": "$departureId1",
                "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
                "localReferenceNumber": "$lrn1",
                "created": "2022-11-04T13:36:52.332Z",
                "updated": "2022-11-04T13:36:52.332Z",
                "enrollmentEORINumber": "9999912345",
                "movementEORINumber": "GB1234567890"
              },
              {
                "_links": {
                  "self": {
                    "href": "/customs/transits/movements/departures/6365135ba5e821ee"
                  },
                  "messages": {
                    "href": "/customs/transits/movements/departures/6365135ba5e821ee/messages"
                  }
                },
                "id": "$departureId2",
                "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
                "localReferenceNumber": "$lrn2",
                "created": "2022-11-04T13:27:55.522Z",
                "updated": "2022-11-04T13:27:55.522Z",
                "enrollmentEORINumber": "9999912345",
                "movementEORINumber": "GB1234567890"
              }
            ]
          }
          """
      )

      "must return Departures" in {

        server.stubFor(
          get(urlEqualTo(s"/movements/departures"))
            .willReturn(okJson(responseJson.toString()))
        )

        val expectedResult = Seq(
          Departure(departureId1, lrn1, Instant.ofEpochMilli(1667569012332L)),
          Departure(departureId2, lrn2, Instant.ofEpochMilli(1667568475522L))
        )

        await(connector.getDepartures()) mustBe Some(expectedResult)
      }

      "must return empty Departures when 404 is returned" in {

        server.stubFor(
          get(urlEqualTo(s"/movements/departures"))
            .willReturn(aResponse().withStatus(404))
        )

        connector.getDepartures().futureValue mustBe Some(Seq.empty)
      }

      "must return None when an error is returned" in {
        val genError = Gen.chooseNum(400, 599).suchThat(_ != 404)

        forAll(genError) {
          error =>
            server.stubFor(
              get(urlEqualTo(s"/movements/departures"))
                .willReturn(aResponse().withStatus(error))
            )

            await(connector.getDepartures()) mustBe None
        }
      }
    }

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

    "getDepartureMessages" - {

      val messageId   = "634982098f02f00a"
      val departureId = "6365135ba5e821ee"

      val responseJson: JsValue = Json.parse(s"""
          |{
          |  "_links": {
          |    "self": {
          |      "href": "/customs/transits/movements/departures/$departureId/messages"
          |    },
          |    "departure": {
          |      "href": "/customs/transits/movements/departures/$departureId"
          |    }
          |  },
          |  "totalCount": 1,
          |  "messages": [
          |    {
          |      "_links": {
          |        "self": {
          |          "href": "/customs/transits/movements/departures/$departureId/message/634982098f02f00a"
          |        },
          |        "departure": {
          |          "href": "/customs/transits/movements/departures/$departureId"
          |        }
          |      },
          |      "id": "$messageId",
          |      "departureId": "$departureId",
          |      "received": "2023-08-10T12:04:39.779Z",
          |      "type": "IE015",
          |      "status": "Success"
          |    }
          |  ]
          |}
          |""".stripMargin)

      "must return some messages for given departure ID" in {

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureId/messages"))
            .willReturn(okJson(responseJson.toString()))
        )

        val expectedResult = Seq(DepartureMessage(messageId, "IE015", Instant.ofEpochMilli(1691669079779L)))

        await(connector.getDepartureMessages(departureId)) mustBe Some(expectedResult)
      }

      "must return None Departures when 404 is returned" in {

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureId/messages"))
            .willReturn(aResponse().withStatus(404))
        )

        connector.getDepartureMessages(departureId).futureValue mustBe None
      }
    }

    "getDepartureMessage" - {

      val messageId   = "62f4ebbb765ba8c2"
      val departureId = "62f4ebbbf581d4aa"

      "when IE056" - {

        val responseJson: JsValue = Json.parse(s"""
             |{
             |  "_links": {
             |    "self": {
             |      "href": "/customs/transits/movements/departures/$departureId/messages/62f4ebbb765ba8c2"
             |    },
             |    "departure": {
             |      "href": "/customs/transits/movements/departures/$departureId"
             |    }
             |  },
             |  "id": "$messageId",
             |  "departureId": "$departureId",
             |  "received": "2022-08-11T11:44:59.83705",
             |  "type": "IE015",
             |  "status": "Success",
             |  "body": {
             |    "n1:CC056C": {
             |      "FunctionalError": [
             |        {
             |          "errorPointer": "/CC015C/Authorisation[1]/referenceNumber"
             |        },
             |        {
             |          "errorPointer": "/CC015C/Guarantee[1]/guaranteeType"
             |        }
             |      ]
             |    }
             |  }
             |}
             |""".stripMargin)

        "must return message for given departure ID and message ID" in {

          server.stubFor(
            get(urlEqualTo(s"/movements/departures/$departureId/messages/$messageId"))
              .willReturn(okJson(responseJson.toString()))
          )

          val expectedResult = IE056Message(
            IE056Body(
              Seq(
                FunctionalError("/CC015C/Authorisation[1]/referenceNumber"),
                FunctionalError("/CC015C/Guarantee[1]/guaranteeType")
              )
            )
          )

          await(connector.getDepartureMessage[IE056Message](departureId, messageId)) mustBe expectedResult
        }
      }
    }
  }
}
