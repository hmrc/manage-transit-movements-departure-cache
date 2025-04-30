/*
 * Copyright 2024 HM Revenue & Customs
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

package api.submission

import base.SpecBase

class DeclarationSpec extends SpecBase {

  private val service = app.injector.instanceOf[Declaration]

  "attributes" must {
    "assign phase ID" in {
      val result = service.attributes
      result.keys.size shouldBe 1
      result.get("@PhaseID").value.value.toString shouldBe "NCTS5.1"
    }
  }
}
