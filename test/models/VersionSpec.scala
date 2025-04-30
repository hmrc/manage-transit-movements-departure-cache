/*
 * Copyright 2025 HM Revenue & Customs
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

import scala.util.Success

class VersionSpec extends SpecBase {

  "Version" when {

    "apply" when {
      "version is 1.0" must {
        "return Success(Phase5)" in {
          val result = Version.apply(Some("1.0"))
          result shouldEqual Success(Version.Phase5)
        }
      }

      "version is 2.0" must {
        "return Some(Phase6)" in {
          val result = Version.apply(Some("2.0"))
          result shouldEqual Success(Version.Phase6)
        }
      }

      "version is undefined" must {
        "return Some(Phase5)" in {
          val result = Version.apply(None)
          result shouldEqual Success(Version.Phase5)
        }
      }

      "version is unexpected value" must {
        "return None" in {
          val result = Version.apply(Some("foo"))
          result.failed.get.getMessage shouldEqual "foo is not a valid version"
        }
      }
    }

    "id" when {
      "Phase5" must {
        "return NCTS5.1" in {
          val result = Version.Phase5.id
          result.toString shouldEqual "NCTS5.1"
        }
      }

      "Phase6" must {
        "return NCTS6" in {
          val result = Version.Phase6.id
          result.toString shouldEqual "NCTS6"
        }
      }
    }
  }
}
