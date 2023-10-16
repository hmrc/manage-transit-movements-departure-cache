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

package models

import play.api.libs.json._
import play.api.mvc.QueryStringBindable

sealed trait SubmissionState {
  val asString: String
}

object SubmissionState {

  case object NotSubmitted extends SubmissionState {
    override val asString: String = "notSubmitted"
  }

  case object Submitted extends SubmissionState {
    override val asString: String = "submitted"
  }

  case object RejectedPendingChanges extends SubmissionState {
    override val asString: String = "rejectedPendingChanges"
  }

  case object Amendment extends SubmissionState {
    override val asString: String = "amendment"
  }

  case object GuaranteeAmendment extends SubmissionState {
    override val asString: String = "guaranteeAmendment"
  }

  implicit val reads: Reads[SubmissionState] = Reads {
    case JsString(NotSubmitted.asString)           => JsSuccess(NotSubmitted)
    case JsString(Submitted.asString)              => JsSuccess(Submitted)
    case JsString(RejectedPendingChanges.asString) => JsSuccess(RejectedPendingChanges)
    case JsString(Amendment.asString)              => JsSuccess(Amendment)
    case JsString(GuaranteeAmendment.asString)     => JsSuccess(GuaranteeAmendment)
    case x                                         => JsError(s"Could not read $x as SubmissionState")
  }

  implicit val writes: Writes[SubmissionState] = Writes {
    state => JsString(state.asString)
  }

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[SubmissionState] =
    new QueryStringBindable[SubmissionState] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, SubmissionState]] =
        stringBinder.bind(key, params).map {
          case Right(NotSubmitted.asString)           => Right(NotSubmitted)
          case Right(Submitted.asString)              => Right(Submitted)
          case Right(RejectedPendingChanges.asString) => Right(RejectedPendingChanges)
          case x                                      => Left(s"Unable to bind $x")
        }

      override def unbind(key: String, value: SubmissionState): String = value.asString
    }

}
