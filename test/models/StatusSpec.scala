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
import play.api.libs.json.{JsString, Json}

class StatusSpec extends SpecBase {

  "must serialise to json" when {
    "Completed" in {
      val status = Status.Completed
      val result = Json.toJson(status)
      result shouldEqual JsString("completed")
    }

    "InProgress" in {
      val status = Status.InProgress
      val result = Json.toJson(status)
      result shouldEqual JsString("in-progress")
    }

    "NotStarted" in {
      val status = Status.NotStarted
      val result = Json.toJson(status)
      result shouldEqual JsString("not-started")
    }

    "CannotStartYet" in {
      val status = Status.CannotStartYet
      val result = Json.toJson(status)
      result shouldEqual JsString("cannot-start-yet")
    }

    "Error" in {
      val status = Status.Error
      val result = Json.toJson(status)
      result shouldEqual JsString("error")
    }

    "Amended" in {
      val status = Status.Amended
      val result = Json.toJson(status)
      result shouldEqual JsString("amended")
    }
  }

  "must deserialise from json" when {
    "Completed" in {
      val json   = JsString("completed")
      val result = json.as[Status.Value]
      result shouldEqual Status.Completed
    }

    "InProgress" in {
      val json   = JsString("in-progress")
      val result = json.as[Status.Value]
      result shouldEqual Status.InProgress
    }

    "NotStarted" in {
      val json   = JsString("not-started")
      val result = json.as[Status.Value]
      result shouldEqual Status.NotStarted
    }

    "CannotStartYet" in {
      val json   = JsString("cannot-start-yet")
      val result = json.as[Status.Value]
      result shouldEqual Status.CannotStartYet
    }

    "Error" in {
      val json   = JsString("error")
      val result = json.as[Status.Value]
      result shouldEqual Status.Error
    }
  }

}
