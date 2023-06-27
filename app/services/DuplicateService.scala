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
import connectors.ApiConnector
import models.Departures
import play.api.Logging
import repositories.CacheRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DuplicateService @Inject() (
  cacheRepository: CacheRepository,
  apiConnector: ApiConnector,
  config: AppConfig
)(implicit ec: ExecutionContext)
    extends Logging {

  def apiLRNCheck(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    apiConnector.getDepartures(Seq("localReferenceNumber" -> lrn)).map(_.isDefined)

  def cacheLRNCheck(lrn: String, eoriNumber: String): Future[Boolean] =
    cacheRepository.existsLRN(lrn, eoriNumber)

  def isDuplicateLRN(lrn: String, eoriNumber: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    apiLRNCheck(lrn).flatMap {
      case false => cacheLRNCheck(lrn, eoriNumber) // TODO: Ask Sayak what we do if the lrn exists in cache e.g drafts
    }

}
