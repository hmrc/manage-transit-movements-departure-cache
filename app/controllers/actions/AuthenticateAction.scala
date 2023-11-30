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

package controllers.actions

import com.codahale.metrics.MetricRegistry
import config.AppConfig
import models.request.AuthenticatedRequest
import play.api.Logging
import play.api.mvc.Results.{Forbidden, Unauthorized}
import play.api.mvc.{ActionRefiner, Request, Result}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthenticateAction @Inject() (
  override val authConnector: AuthConnector,
  val metrics: MetricRegistry,
  appConfig: AppConfig
)(implicit val executionContext: ExecutionContext)
    extends ActionRefiner[Request, AuthenticatedRequest]
    with AuthorisedFunctions
    with Logging {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    authorised(Enrolment(appConfig.enrolmentKey)).retrieve(Retrievals.authorisedEnrolments) {
      enrolments =>
        (for {
          enrolment  <- enrolments.getEnrolment(appConfig.enrolmentKey)
          identifier <- enrolment.getIdentifier(appConfig.enrolmentIdentifier)
        } yield Future.successful(Right(AuthenticatedRequest(request, identifier.value)))).getOrElse {
          Future.failed(InsufficientEnrolments(s"Unable to retrieve ${appConfig.enrolmentKey} enrolment"))
        }
    }
  }.recover {
    case e: InsufficientEnrolments =>
      logger.warn("Failed to authorise due to insufficient enrolments", e)
      Left(Forbidden)
    case e: AuthorisationException =>
      logger.warn("Failed to authorise", e)
      Left(Unauthorized)
  }
}
