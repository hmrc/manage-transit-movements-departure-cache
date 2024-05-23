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
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiService @Inject() (
  apiConnector: ApiConnector,
  declaration: Declaration
)(implicit ec: ExecutionContext) {

  def submitDeclaration(userAnswers: UserAnswers, phase: Phase)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    apiConnector.submitDeclaration(declaration.transform(userAnswers, MovementReferenceNumber.Empty, phase))

  def submitAmendment(userAnswers: UserAnswers, departureId: String, phase: Phase)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    for {
      mrn <- apiConnector.getMRN(departureId)
      payload = declaration.transform(userAnswers, mrn, phase)
      result <- apiConnector.submitAmendment(departureId, payload)
    } yield result

  def get(lrn: String)(implicit hc: HeaderCarrier): Future[Option[Messages]] =
    apiConnector.getDeparture(lrn).flatMap {
      _.traverse {
        departure => apiConnector.getMessages(departure.id)
      }
    }

  def isIE028DefinedForDeparture(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    get(lrn).map {
      _.exists {
        _.messages.exists(_.`type` == "IE028")
      }
    }
}
