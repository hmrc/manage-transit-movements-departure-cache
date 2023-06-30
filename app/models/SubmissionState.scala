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

import play.api.libs.json.{__, JsString, Reads, Writes}

sealed trait SubmissionState {
  def toString: String
  val amendable: Boolean
}

object SubmissionState {

  case object NotSubmitted extends SubmissionState {
    override def toString: String = "notSubmitted"
    val amendable: Boolean        = isAmendable(this)
  }

  case object Submitted extends SubmissionState {
    override def toString: String = "submitted"
    val amendable: Boolean        = isAmendable(this)
  }

  case object RejectedPendingChanges extends SubmissionState {
    override def toString: String = "rejectedPendingChanges"
    val amendable: Boolean        = isAmendable(this)
  }

  case object RejectedAndResubmitted extends SubmissionState {
    override def toString: String = "rejectedAndResubmitted"
    val amendable: Boolean        = isAmendable(this)
  }

  def isAmendable(state: SubmissionState): Boolean = state match {
    case NotSubmitted           => false
    case RejectedAndResubmitted => false
    case Submitted              => true
    case RejectedPendingChanges => true
  }

  def apply(state: String): SubmissionState = state match {
    case "submitted"              => Submitted
    case "rejectedPendingChanges" => RejectedPendingChanges
    case "rejectedAndResubmitted" => RejectedAndResubmitted
    case _                        => NotSubmitted
  }

  implicit def reads: Reads[SubmissionState] =
    __.readWithDefault[String](NotSubmitted.toString).map(SubmissionState.apply)

  implicit def writes: Writes[SubmissionState] = Writes {
    state => JsString(state.toString)
  }

}
