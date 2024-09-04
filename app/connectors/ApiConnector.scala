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

import config.AppConfig
import models.{Departure, Departures, Messages, MovementReferenceNumber, Phase}
import play.api.Logging
import play.api.http.HeaderNames._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class ApiConnector @Inject() (http: HttpClientV2)(implicit ec: ExecutionContext, appConfig: AppConfig) extends HttpErrorFunctions with Logging {

  def getDeparture(lrn: String, phase: Phase)(implicit hc: HeaderCarrier): Future[Option[Departure]] = {
    val url = url"${appConfig.apiUrl}/movements/departures"
    http
      .get(url)
      .transform(_.withQueryStringParameters("localReferenceNumber" -> lrn))
      .setHeader(ACCEPT -> s"application/vnd.hmrc.${phase.version}+json")
      .execute[Departures]
      .map(_.departures.headOption)
  }

  def getMRN(departureId: String, phase: Phase)(implicit hc: HeaderCarrier): Future[MovementReferenceNumber] = {
    val url = url"${appConfig.apiUrl}/movements/departures/$departureId"
    http
      .get(url)
      .setHeader(ACCEPT -> s"application/vnd.hmrc.${phase.version}+json")
      .execute[MovementReferenceNumber]
  }

  def getMessages(departureId: String, phase: Phase)(implicit hc: HeaderCarrier): Future[Messages] = {
    val url = url"${appConfig.apiUrl}/movements/departures/$departureId/messages"
    http
      .get(url)
      .setHeader(ACCEPT -> s"application/vnd.hmrc.${phase.version}+json")
      .execute[Messages]
  }

  def submitAmendment(departureId: String, xml: NodeSeq, phase: Phase)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = url"${appConfig.apiUrl}/movements/departures/$departureId/messages"
    submit(url, xml, phase)
  }

  def submitDeclaration(xml: NodeSeq, phase: Phase)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = url"${appConfig.apiUrl}/movements/departures"
    submit(url, xml, phase)
  }

  private def submit(
    url: URL,
    xml: NodeSeq,
    phase: Phase
  )(implicit hc: HeaderCarrier): Future[HttpResponse] =
    http
      .post(url)
      .setHeader(ACCEPT -> s"application/vnd.hmrc.${phase.version}+json")
      .setHeader(CONTENT_TYPE -> "application/xml")
      .withBody(xml)
      .execute[HttpResponse]
}
