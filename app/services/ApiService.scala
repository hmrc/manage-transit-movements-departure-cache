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
import cats.implicits.*
import connectors.ApiConnector
import models.*
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiService @Inject() (
  apiConnector: ApiConnector,
  declaration: Declaration
)(implicit ec: ExecutionContext) {

  def submitDeclaration(userAnswers: UserAnswers, version: Phase)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    apiConnector.submitDeclaration(declaration.transform(userAnswers, MovementReferenceNumber.Empty, version), version)

  def submitAmendment(userAnswers: UserAnswers, departureId: String, version: Phase)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    for {
      mrn <- apiConnector.getMRN(departureId, version)
      payload = declaration.transform(userAnswers, mrn, version)
      result <- apiConnector.submitAmendment(departureId, payload, version)
    } yield result

  def get(lrn: String, version: Phase)(implicit hc: HeaderCarrier): Future[Option[Messages]] =
    apiConnector.getDeparture(lrn, version).flatMap {
      _.traverse {
        departure => apiConnector.getMessages(departure.id, version)
      }
    }
}
