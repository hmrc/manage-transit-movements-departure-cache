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

import connectors.ApiConnector
import models.{Departure, DepartureMessage, SubmissionState, UserAnswers}
import play.api.mvc.Result
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiService @Inject() (
  apiConnector: ApiConnector
)(implicit ec: ExecutionContext) {

  def submitDeclaration(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] =
    apiConnector.submitDeclaration(userAnswers)

  def getDeparturesForLrn(lrn: String)(implicit hc: HeaderCarrier): Future[Option[Seq[Departure]]] =
    apiConnector.getDepartures(Seq("localReferenceNumber" -> lrn))

  def getSubmissionStatus(lrn: String)(implicit hc: HeaderCarrier): Future[SubmissionState] =
    getDeparturesForLrn(lrn).flatMap {
      case Some(departures) =>
        departures.sortBy(_.created).reverse.headOption match {
          case Some(departure) =>
            apiConnector.getDepartureMessages(departure.id).map {
              case Some(messages) =>
                messages.sortBy(_.received).reverse.headOption match {
                  case Some(DepartureMessage(_, "IE015", _))         => SubmissionState.Submitted
                  case Some(DepartureMessage(messageId, "IE056", _)) =>
                    // get message
                    // get functional errors
                    // pass to XPathService isAmendable
                    // if amendable => RejectedPendingChanges
                    // if not amendable => SubmissionState.Submitted
                    SubmissionState.RejectedPendingChanges
                  case _ => SubmissionState.NotSubmitted
                }
              case None =>
                SubmissionState.NotSubmitted
            }
          case None =>
            Future.successful(SubmissionState.NotSubmitted)
        }
      case None =>
        Future.successful(SubmissionState.NotSubmitted)
    }
}
