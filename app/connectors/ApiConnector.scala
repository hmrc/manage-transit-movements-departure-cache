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
import config.AppConfig
import models.{DepartureMessageTypes, Departures, MovementReferenceNumber, UserAnswers}
import play.api.Logging
import play.api.http.HeaderNames
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient, HttpErrorFunctions, HttpReads, HttpResponse}
import com.github.dwickern.macros.NameOf._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

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

  def submitAmend(userAnswers: UserAnswers, departureId: String)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] = {

    val declarationUrl = s"${appConfig.apiUrl}/movements/departures/$departureId/messages"
    val requestHeaders = Seq(
      HeaderNames.ACCEPT       -> "application/vnd.hmrc.2.0+json",
      HeaderNames.CONTENT_TYPE -> "application/xml"
    )

    for {
      mrn <- getMRN(departureId)
      payload = Declaration.transform(userAnswers, mrn).toString
      result <- getHttpResponse(declarationUrl, requestHeaders, payload, HttpMethodName(nameOf(submitAmend _)))
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
          logger.debug(s"ApiConnector:${httpMethod.name}: success: ${response.status}-${response.body}")
          Right(response)
      }
      .recover {
        case httpEx: BadRequestException =>
          logger.info(s"ApiConnector:${httpMethod.name}: bad request: ${httpEx.responseCode}-${httpEx.getMessage}")
          Left(BadRequest(s"ApiConnector:${httpMethod.name}: bad request"))
        case e: Exception =>
          logger.error(s"ApiConnector:${httpMethod.name}: something went wrong: ${e.getMessage}")
          Left(InternalServerError(s"ApiConnector:${httpMethod.name}: something went wrong"))
      }

  private case class HttpMethodName(name: String)
}
