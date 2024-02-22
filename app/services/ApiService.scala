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

package services

import api.submission.Declaration
import cats.implicits._
import connectors.ApiConnector
import models._
import play.api.mvc.Result
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiService @Inject() (
  apiConnector: ApiConnector,
  declaration: Declaration
)(implicit ec: ExecutionContext) {

  def submitDeclaration(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] =
    apiConnector.submitDeclaration(declaration.transform(userAnswers, MovementReferenceNumber.Empty))

  def submitAmendment(userAnswers: UserAnswers, departureId: String)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] =
    for {
      mrn <- apiConnector.getMRN(departureId)
      payload = declaration.transform(userAnswers, mrn)
      result <- apiConnector.submitAmendment(departureId, payload)
    } yield result

  def isIE028DefinedForDeparture(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    apiConnector
      .getDepartures()
      .flatMap {
        _.departures.find(_.localReferenceNumber == lrn).traverse {
          departure =>
            apiConnector.getMessages(departure.id).map {
              _.messageTypes.exists(_.messageType == "IE028")
            }
        }
      }
      .map(_.getOrElse(false))
}
