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
import play.api.libs.json.Json

class TaskSpec extends SpecBase {

  "must serialise to json" when {
    "href defined" in {
      val task   = Task("section", Status.Completed, Some("#"))
      val result = Json.toJson(task)
      result shouldBe Json.parse("""
          |{
          |  "section" : "section",
          |  "status" : "completed",
          |  "href" : "#"
          |}
          |""".stripMargin)
    }

    "href undefined" in {
      val task   = Task("section", Status.Completed, None)
      val result = Json.toJson(task)
      result shouldBe Json.parse("""
          |{
          |  "section" : "section",
          |  "status" : "completed"
          |}
          |""".stripMargin)
    }
  }

  "must deserialise from json" when {
    "href defined" in {
      val json   = Json.parse("""
          |{
          |  "section" : "section",
          |  "status" : "completed",
          |  "href" : "#"
          |}
          |""".stripMargin)
      val result = json.as[Task]
      result shouldBe Task("section", Status.Completed, Some("#"))
    }

    "href undefined" in {
      val json   = Json.parse("""
          |{
          |  "section" : "section",
          |  "status" : "completed"
          |}
          |""".stripMargin)
      val result = json.as[Task]
      result shouldBe Task("section", Status.Completed, None)
    }
  }

}
