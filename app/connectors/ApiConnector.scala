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
import connectors.CustomHttpReads.rawHttpResponseHttpReads
import models.{Departures, UserAnswers}
import play.api.Logging
import play.api.http.HeaderNames
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient, HttpErrorFunctions, HttpResponse}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private val requestHeaders = Seq(
    HeaderNames.ACCEPT       -> "application/vnd.hmrc.2.0+json",
    HeaderNames.CONTENT_TYPE -> "application/xml"
  )

  def getDepartures(queryParams: Seq[(String, String)] = Seq.empty)(implicit hc: HeaderCarrier): Future[Option[Departures]] = {
    val url = s"${appConfig.apiUrl}/movements/departures"

    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    httpClient
      .GET[HttpResponse](url, queryParams)(rawHttpResponseHttpReads, headers, ec)
      .map {
        response =>
          response.status match {
            case OK        => response.json.asOpt[Departures]
            case NOT_FOUND => Some(Departures(Seq.empty))
            case _         => None
          }
      }
      .recover {
        case e =>
          logger.error(s"Failed to get departure movements with error: $e")
          None
      }
  }

  def submitDeclaration(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] = {

    val declarationUrl  = s"${appConfig.apiUrl}/movements/departures"
    val payload: String = Declaration.transformToXML(userAnswers).toString

    // TODO - can we log and audit here and send a generic error to the FE?
    httpClient
      .POSTString[HttpResponse](declarationUrl, payload, requestHeaders)
      .map {
        response =>
          logger.debug(s"ApiConnector:submitDeclaration: success: ${response.status}-${response.body}")
          Right(response)
      }
      .recover {
        case httpEx: BadRequestException =>
          logger.warn(s"ApiConnector:submitDeclaration: bad request: ${httpEx.responseCode}-${httpEx.getMessage}")
          Left(BadRequest("ApiConnector:submitDeclaration: bad request"))
        case e: Exception =>
          logger.error(s"ApiConnector:submitDeclaration: something went wrong: ${e.getMessage}")
          Left(InternalServerError("ApiConnector:submitDeclaration: something went wrong"))
      }
  }
}
