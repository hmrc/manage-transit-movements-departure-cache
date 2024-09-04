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

import models.Phase
import models.request.{AuthenticatedRequest, VersionedRequest}
import play.api.mvc.Results.BadRequest
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VersionedAction @Inject() (implicit val executionContext: ExecutionContext) extends ActionRefiner[AuthenticatedRequest, VersionedRequest] {

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, VersionedRequest[A]]] =
    request.headers.get("APIVersion").flatMap(Phase(_)) match {
      case Some(phase) => Future.successful(Right(VersionedRequest(request, phase)))
      case None        => Future.successful(Left(BadRequest))
    }
}
