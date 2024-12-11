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

import base.SpecBase
import models.Task.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TaskSpec extends SpecBase with ScalaCheckPropertyChecks {

  "PreTaskList" must {
    "have correct taskName" in {
      PreTaskList.taskName shouldBe ".preTaskList"
    }
  }

  "TraderDetails" must {
    "have correct taskName" in {
      TraderDetails.taskName shouldBe ".traderDetails"
    }

    "resolve to String" in {
      TraderDetails.toString shouldBe "Trader details"
    }
  }

  "RouteDetails" must {
    "have correct taskName" in {
      RouteDetails.taskName shouldBe ".routeDetails"
    }

    "resolve to String" in {
      RouteDetails.toString shouldBe "Route details"
    }
  }

  "TransportDetails" must {
    "have correct taskName" in {
      TransportDetails.taskName shouldBe ".transportDetails"
    }

    "resolve to String" in {
      TransportDetails.toString shouldBe "Transport details"
    }
  }

  "GuaranteeDetails" must {
    "have correct taskName" in {
      GuaranteeDetails.taskName shouldBe ".guaranteeDetails"
    }

    "resolve to String" in {
      GuaranteeDetails.toString shouldBe "Guarantee details"
    }
  }

  "Documents" must {
    "have correct taskName" in {
      Documents.taskName shouldBe ".documents"
    }

    "resolve to String" in {
      Documents.toString shouldBe "Documents"
    }
  }

  "Items" must {
    "have correct taskName" in {
      Items.taskName shouldBe ".items"
    }

    "resolve to String" in {
      Items.toString shouldBe "Items"
    }
  }
}
