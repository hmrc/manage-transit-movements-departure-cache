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

import com.github.dwickern.macros.NameOf._
import config.AppConfig
import models.{DepartureMessageTypes, Departures, MovementReferenceNumber}
import play.api.Logging
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status._
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class ApiConnector @Inject() (http: HttpClientV2)(implicit ec: ExecutionContext, appConfig: AppConfig) extends HttpErrorFunctions with Logging {

  private def acceptHeader: (String, String) = ("Accept", "application/vnd.hmrc.2.0+json")

  def getDepartures()(implicit hc: HeaderCarrier): Future[Departures] = {
    val url = url"${appConfig.apiUrl}/movements/departures"
    http
      .get(url)
      .setHeader(acceptHeader)
      .execute[Departures]
  }

  def getMRN(departureId: String)(implicit hc: HeaderCarrier): Future[MovementReferenceNumber] = {
    val url = url"${appConfig.apiUrl}/movements/departures/$departureId"
    http
      .get(url)
      .setHeader(acceptHeader)
      .execute[MovementReferenceNumber]
  }

  def getMessages(departureId: String)(implicit hc: HeaderCarrier): Future[DepartureMessageTypes] = {
    val url = url"${appConfig.apiUrl}/movements/departures/$departureId/messages"
    http
      .get(url)
      .setHeader(acceptHeader)
      .execute[DepartureMessageTypes]
  }

  def submitAmendment(departureId: String, xml: NodeSeq)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] = {
    val url = url"${appConfig.apiUrl}/movements/departures/$departureId/messages"
    getHttpResponse(url, xml, HttpMethodName(nameOf(submitAmendment _)))
  }

  def submitDeclaration(xml: NodeSeq)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] = {
    val url = url"${appConfig.apiUrl}/movements/departures"
    getHttpResponse(url, xml, HttpMethodName(nameOf(submitDeclaration _)))
  }

  private def getHttpResponse(
    url: URL,
    xml: NodeSeq,
    httpMethod: HttpMethodName
  )(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] =
    http
      .post(url)
      .setHeader(acceptHeader)
      .setHeader(CONTENT_TYPE -> "application/xml")
      .withBody(xml)
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case x if is2xx(x) =>
              Right(response)
            case BAD_REQUEST =>
              logger.info(s"ApiConnector:${httpMethod.name}: bad request")
              Left(BadRequest(s"ApiConnector:${httpMethod.name}: bad request"))
            case e =>
              logger.error(s"ApiConnector:${httpMethod.name}: something went wrong: $e")
              Left(InternalServerError(s"ApiConnector:${httpMethod.name}: something went wrong"))
          }
      }

  private case class HttpMethodName(name: String)
}
