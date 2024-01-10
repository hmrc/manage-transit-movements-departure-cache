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

import api.submission._
import com.github.dwickern.macros.NameOf._
import config.AppConfig
import models.{DepartureMessageTypes, Departures, MovementReferenceNumber, UserAnswers}
import play.api.Logging
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiConnector @Inject() (httpClient: HttpClient)(implicit ec: ExecutionContext, appConfig: AppConfig) extends HttpErrorFunctions with Logging {

  private def headers(implicit hc: HeaderCarrier): HeaderCarrier = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

  def getDepartures()(implicit hc: HeaderCarrier): Future[Departures] = {
    val url = s"${appConfig.apiUrl}/movements/departures"

    httpClient.GET[Departures](url)(implicitly, headers, ec)
  }

  def getMRN(departureId: String)(implicit hc: HeaderCarrier): Future[MovementReferenceNumber] = {
    val url = s"${appConfig.apiUrl}/movements/departures/$departureId"
    httpClient.GET[MovementReferenceNumber](url)(HttpReads[MovementReferenceNumber], headers, ec)
  }

  def getMessageTypesByPath(
    path: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, HttpReads: HttpReads[DepartureMessageTypes]): Future[DepartureMessageTypes] = {
    val url = s"${appConfig.apiUrl}/$path"

    httpClient.GET[DepartureMessageTypes](url)(implicitly, headers, ec)
  }

  def submitAmendment(userAnswers: UserAnswers, departureId: String)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] = {

    val declarationUrl = s"${appConfig.apiUrl}/movements/departures/$departureId/messages"
    val requestHeaders = Seq(
      HeaderNames.ACCEPT       -> "application/vnd.hmrc.2.0+json",
      HeaderNames.CONTENT_TYPE -> "application/xml"
    )

    for {
      mrn <- getMRN(departureId)
      payload = Declaration.transform(userAnswers, mrn).toString
      result <- getHttpResponse(declarationUrl, requestHeaders, payload, HttpMethodName(nameOf(submitAmendment _)))
    } yield result

  }

  def submitDeclaration(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] = {

    val declarationUrl = s"${appConfig.apiUrl}/movements/departures"

    val payload: String = Declaration.transform(userAnswers, mrn = MovementReferenceNumber.Empty).toString

    val requestHeaders = Seq(
      HeaderNames.ACCEPT       -> "application/vnd.hmrc.2.0+json",
      HeaderNames.CONTENT_TYPE -> "application/xml"
    )

    getHttpResponse(declarationUrl, requestHeaders, payload, HttpMethodName(nameOf(submitDeclaration _)))
  }

  private def getHttpResponse(declarationUrl: String, requestHeaders: Seq[(String, String)], payload: String, httpMethod: HttpMethodName)(implicit
    hc: HeaderCarrier
  ) =
    httpClient
      .POSTString[HttpResponse](declarationUrl, payload, requestHeaders)
      .map {
        response =>
          response.status match {
            case x if is2xx(x) =>
              logger.debug(s"ApiConnector:${httpMethod.name}: success: ${response.status}-${response.body}")
              Right(response)
            case BAD_REQUEST =>
              logger.info(s"ApiConnector:${httpMethod.name}: bad request: ${response.body}")
              Left(BadRequest(s"ApiConnector:${httpMethod.name}: bad request"))
            case _ =>
              logger.error(s"ApiConnector:${httpMethod.name}: something went wrong: ${response.body}")
              Left(InternalServerError(s"ApiConnector:${httpMethod.name}: something went wrong"))
          }
      }
  private case class HttpMethodName(name: String)
}
