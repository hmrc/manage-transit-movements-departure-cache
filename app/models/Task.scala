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

sealed trait Task {
  val taskName: String

  def taskError: (String, Status.Value) =
    taskName -> Status.Error
}

object Task {

  case object PreTaskList extends Task {
    override val taskName: String = ".preTaskList"
  }

  case object TraderDetails extends Task {
    override val taskName: String = ".traderDetails"

    override def toString: String = "Trader details"
  }

  case object RouteDetails extends Task {
    override val taskName: String = ".routeDetails"

    override def toString: String = "Route details"
  }

  case object TransportDetails extends Task {
    override val taskName: String = ".transportDetails"

    override def toString: String = "Transport details"
  }

  case object Documents extends Task {
    override val taskName: String = ".documents"

    override def toString: String = "Documents"
  }

  case object Items extends Task {
    override val taskName: String = ".items"

    override def toString: String = "Items"
  }

  case object GuaranteeDetails extends Task {
    override val taskName: String = ".guaranteeDetails"

    override def toString: String = "Guarantee details"
  }
}
