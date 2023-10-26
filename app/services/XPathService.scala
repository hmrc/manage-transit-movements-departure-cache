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

import models.Task._
import models.{Status, SubmissionState, XPath}
import play.api.Logging
import repositories.CacheRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class XPathService @Inject() (
  cacheRepository: CacheRepository
)(implicit ec: ExecutionContext)
    extends Logging {

  def isDeclarationAmendable(lrn: String, eoriNumber: String, xPaths: Seq[XPath]): Future[Boolean] =
    cacheRepository.get(lrn, eoriNumber).map(_.isDefined && xPaths.exists(_.isAmendable))

  def handleGuaranteeErrors(lrn: String, eoriNumber: String): Future[Boolean] = {

    val tasks: Map[String, Status.Value] =
      Map(
        PreTaskList.taskName      -> Status.Unavailable,
        TraderDetails.taskName    -> Status.Unavailable,
        RouteDetails.taskName     -> Status.Unavailable,
        TransportDetails.taskName -> Status.Unavailable,
        Documents.taskName        -> Status.Unavailable,
        Items.taskName            -> Status.Unavailable,
        GuaranteeDetails.taskName -> Status.Error
      )

    cacheRepository.get(lrn, eoriNumber).flatMap {
      case Some(userAnswers) =>
        cacheRepository
          .set(userAnswers.updateTasks(tasks), SubmissionState.GuaranteeAmendment, userAnswers.departureId)
          .map {
            case true => true
            case false =>
              logger.warn("Write was not acknowledged")
              false
          }
          .recover {
            case e =>
              logger.warn("Failed to write user answers to mongo", e)
              false
          }
      case _ => Future.successful(false)
    }

  }

  private def setTasksUnavailable(tasks: Map[String, Status.Value]): Map[String, Status.Value] =
    tasks.map(
      task => if (task._1 != PreTaskList.taskName) (task._1, Status.Unavailable) else task
    )

  def handleErrors(lrn: String, eoriNumber: String, xPaths: Seq[XPath]): Future[Boolean] =
    xPaths.flatMap {
      _.taskError
    }.toMap match {
      case tasks if tasks.nonEmpty =>
        cacheRepository.get(lrn, eoriNumber).flatMap {
          case Some(userAnswers) =>
            val updatedTasks: Map[String, Status.Value] = userAnswers.metadata.tasks ++ tasks
            cacheRepository
              .set(userAnswers.updateTasks(updatedTasks), SubmissionState.RejectedPendingChanges, userAnswers.departureId)
              .map {
                case true => true
                case false =>
                  logger.warn("Write was not acknowledged")
                  false
              }
              .recover {
                case e =>
                  logger.warn("Failed to write user answers to mongo", e)
                  false
              }
          case _ => Future.successful(false)
        }
      case _ => Future.successful(false)
    }
}
