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

import models.*
import models.Rejection.BusinessRejectionType
import models.Task.*
import play.api.Logging
import repositories.CacheRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class XPathService @Inject() (
  cacheRepository: CacheRepository
)(implicit ec: ExecutionContext)
    extends Logging {

  def isDeclarationAmendable(lrn: String, eoriNumber: String, xPaths: Seq[XPath]): Future[Boolean] =
    cacheRepository.get(lrn, eoriNumber).map {
      _.exists {
        _.status != SubmissionState.NotSubmitted && xPaths.exists(_.isAmendable)
      }
    }

  def handleRejection(userAnswers: UserAnswers, rejection: Rejection): UserAnswers =
    rejection match {
      case Rejection.IE055Rejection(departureId) =>
        val tasks = userAnswers.metadata.tasks.map {
          case (GuaranteeDetails.taskName, _) => GuaranteeDetails.taskName -> Status.Error
          case (taskName, _)                  => taskName                  -> Status.Unavailable
        }
        userAnswers
          .updateTasks(tasks)
          .copy(
            status = SubmissionState.GuaranteeAmendment,
            departureId = Some(departureId)
          )
      case Rejection.IE056Rejection(departureId, businessRejectionType, errorPointers) =>
        val tasks = userAnswers.metadata.tasks ++ errorPointers.toList.flatMap(_.taskError).toMap
        businessRejectionType match {
          case BusinessRejectionType.AmendmentRejection =>
            prepareForAmendment(userAnswers.updateTasks(tasks), departureId)
          case BusinessRejectionType.DeclarationRejection =>
            userAnswers
              .updateTasks(tasks)
              .copy(
                status = SubmissionState.RejectedPendingChanges
              )
        }
    }

  def prepareForAmendment(userAnswers: UserAnswers, departureId: String): UserAnswers =
    userAnswers
      .copy(
        status = SubmissionState.Amendment,
        departureId = Some(departureId)
      )
}
