/*
 * Copyright 2025 HM Revenue & Customs
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

import repositories.{CacheRepository, LockRepository}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.transaction.{TransactionConfiguration, Transactions}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionService @Inject() (
  mc: MongoComponent,
  lockRepository: LockRepository,
  cacheRepository: CacheRepository
)(implicit ec: ExecutionContext)
    extends Transactions {

  override protected def mongoComponent: MongoComponent = mc

  implicit private val tc: TransactionConfiguration = TransactionConfiguration.strict

  def deleteUserAnswersAndLocks(eoriNumber: String, lrn: String): Future[Unit] =
    withSessionAndTransaction(
      _ =>
        for {
          documentRemoval <- cacheRepository.remove(lrn, eoriNumber)
          lockRemoval     <- lockRepository.unlock(eoriNumber, lrn)
          if documentRemoval && lockRemoval
        } yield ()
    )
}
