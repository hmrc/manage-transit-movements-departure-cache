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

import play.api.Logging
import repositories.CacheRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// TODO can this be removed? Its a service of a service
class DuplicateService @Inject() (
  apiService: ApiService,
  cacheRepository: CacheRepository
)(implicit ec: ExecutionContext)
    extends Logging {

  def doesIE028ExistForLrn(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    apiService.isIE028DefinedForDeparture(lrn)

  def doesDraftExistForLrn(lrn: String): Future[Boolean] =
    cacheRepository.doesDraftExistForLrn(lrn)

  // TODO this needs removing as it duplicates line 33
  def doesDraftOrSubmissionExistForLrn(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    doesIE028ExistForLrn(lrn)

}
