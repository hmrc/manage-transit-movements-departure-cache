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

import config.AppConfig
import models.{Status, XPath}
import play.api.Logging
import repositories.CacheRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class XPathService @Inject() (
  cacheRepository: CacheRepository,
  config: AppConfig
)(implicit ec: ExecutionContext)
    extends Logging {

  def isDeclarationAmendable(lrn: String, eoriNumber: String, xPaths: Seq[XPath]): Future[Boolean] =
    cacheRepository.get(lrn, eoriNumber).map {
      case Some(userAnswers) =>
        userAnswers.metadata.isSubmitted.getOrElse(false) && xPaths.size <= config.maxErrorsForAmendableDeclaration && xPaths.exists(_.isAmendable)
      case _ => false
    }

  def handleErrors(lrn: String, eoriNumber: String, xPaths: Seq[XPath]): Future[Boolean] =
    xPaths.flatMap {
      xPath =>
        xPath.taskError
    }.toMap match {
      case tasks if tasks.nonEmpty =>
        cacheRepository.get(lrn, eoriNumber).flatMap {
          case Some(userAnswers) =>
            val updatedTasks: Map[String, Status.Value] = (userAnswers.metadata.tasks.toSeq ++ tasks.toSeq).toMap
            val metaData                                = userAnswers.metadata.copy(tasks = updatedTasks)
            cacheRepository
              .set(metaData)
              .map {
                case true => true
                case false =>
                  logger.error("Write was not acknowledged")
                  false
              }
              .recover {
                case e =>
                  logger.error("Failed to write user answers to mongo", e)
                  false
              }
          case _ => Future.successful(false)
        }
      case _ => Future.successful(false)
    }
}
