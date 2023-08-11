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
import models._
import play.api.mvc.Result
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiService @Inject() (
  apiConnector: ApiConnector,
  xPathService: XPathService
)(implicit ec: ExecutionContext) {

  def submitDeclaration(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Either[Result, HttpResponse]] =
    apiConnector.submitDeclaration(userAnswers)

  def getDeparturesForLrn(lrn: String)(implicit hc: HeaderCarrier): Future[Option[Seq[Departure]]] =
    apiConnector.getDepartures(Seq("localReferenceNumber" -> lrn))

  // TODO - can this be refactored to use OptionT?
  def getSubmissionStatus(lrn: String, eoriNumber: String)(implicit hc: HeaderCarrier): Future[SubmissionState] =
    getDeparturesForLrn(lrn).flatMap {
      case Some(departures) =>
        departures.sortBy(_.created).reverse.headOption match {
          case Some(departure) =>
            apiConnector.getDepartureMessages(departure.id).flatMap {
              case Some(messages) =>
                messages.sortBy(_.received).reverse.headOption match {
                  case Some(DepartureMessage(_, "IE015", _)) => Future.successful(SubmissionState.Submitted)
                  case Some(DepartureMessage(messageId, "IE056", _)) =>
                    apiConnector.getDepartureMessage[IE056Message](departure.id, messageId).map(_.body.xPaths).flatMap {
                      xPaths =>
                        xPathService.isDeclarationAmendable(lrn, eoriNumber, xPaths).map {
                          case true  => SubmissionState.RejectedPendingChanges
                          case false => SubmissionState.Submitted
                        }
                    }
                  case _ => Future.successful(SubmissionState.NotSubmitted)
                }
              case None =>
                Future.successful(SubmissionState.NotSubmitted)
            }
          case None =>
            Future.successful(SubmissionState.NotSubmitted)
        }
      case None =>
        Future.successful(SubmissionState.NotSubmitted)
    }
}
