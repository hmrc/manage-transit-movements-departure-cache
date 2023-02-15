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

import api.submission.Header.scope
import api.submission._
import config.AppConfig
import generated._
import models.UserAnswers
import play.api.Logging
import play.api.http.HeaderNames
import scalaxb.`package`.toXML
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private val requestHeaders = Seq(
    HeaderNames.ACCEPT       -> "application/vnd.hmrc.2.0+json",
    HeaderNames.CONTENT_TYPE -> "application/xml"
  )

  def submitDeclaration(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val declarationUrl  = s"${appConfig.apiUrl}/movements/departures"
    val payload: String = toXML[CC015CType](Declaration.transform(userAnswers), "ncts:CC015C", scope).toString

    httpClient.POSTString(declarationUrl, payload, requestHeaders)

  }

}