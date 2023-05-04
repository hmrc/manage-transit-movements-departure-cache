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

sealed trait JourneyTask {
  def taskName: String
}

object JourneyTask {

  case object PreTaskList extends JourneyTask {
    override def taskName: String = ".preTaskList"
  }

  case object TraderDetails extends JourneyTask {
    override def taskName: String = ".traderDetails"
  }

  case object RouteDetails extends JourneyTask {
    override def taskName: String = ".routeDetails"
  }

  case object TransportDetails extends JourneyTask {
    override def taskName: String = ".transportDetails"
  }

  case object GuaranteeDetails extends JourneyTask {
    override def taskName: String = ".guaranteeDetails"
  }
}
