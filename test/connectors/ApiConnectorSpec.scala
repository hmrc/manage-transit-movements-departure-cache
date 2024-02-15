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
import models.{Departure, Departures}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.{BadRequest, InternalServerError}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse

import scala.xml.NodeSeq

class ApiConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockServerHandler {

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

  "ApiConnector" when {

    "getDepartures" must {
      val url = "/movements/departures"

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
          get(urlEqualTo(url))
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
      val url = "/movements/departures"

      val payload: NodeSeq =
        <ncts:CC015C PhaseID="NCTS5.0" xmlns:ncts="http://ncts.dgtaxud.ec">
          <foo>bar</foo>
        </ncts:CC015C>

      "success" in {
        server.stubFor(post(urlEqualTo(url)).willReturn(okJson(expected)))

        val res = await(connector.submitDeclaration(payload))
        res.toString shouldBe Right(HttpResponse(OK, expected)).toString
      }

      "bad request" in {
        server.stubFor(post(urlEqualTo(url)).willReturn(badRequest()))

        val res = await(connector.submitDeclaration(payload))
        res shouldBe Left(BadRequest("ApiConnector:submitDeclaration: bad request"))
      }

      "internal server error" in {
        server.stubFor(post(urlEqualTo(url)).willReturn(serverError()))

        val res = await(connector.submitDeclaration(payload))
        res shouldBe Left(InternalServerError("ApiConnector:submitDeclaration: something went wrong"))
      }
    }

    "submitAmendment is called" when {
      val url = s"/movements/departures/$departureId/messages"

      val payload: NodeSeq =
        <ncts:CC013C PhaseID="NCTS5.0" xmlns:ncts="http://ncts.dgtaxud.ec">
          <foo>bar</foo>
        </ncts:CC013C>

      "success" in {
        server.stubFor(post(urlEqualTo(url)).willReturn(okJson(expected)))

        val res = await(connector.submitAmendment(departureId, payload))
        res.toString shouldBe Right(HttpResponse(OK, expected)).toString
      }

      "bad request" in {
        server.stubFor(post(urlEqualTo(url)).willReturn(badRequest()))

        val res = await(connector.submitAmendment(departureId, payload))
        res shouldBe Left(BadRequest("ApiConnector:submitAmendment: bad request"))
      }

      "internal server error" in {
        server.stubFor(post(urlEqualTo(url)).willReturn(serverError()))

        val res = await(connector.submitAmendment(departureId, payload))
        res shouldBe Left(InternalServerError("ApiConnector:submitAmendment: something went wrong"))
      }
    }
  }
}
