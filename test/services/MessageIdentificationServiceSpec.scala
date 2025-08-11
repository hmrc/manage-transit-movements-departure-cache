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

package services

import base.SpecBase

class MessageIdentificationServiceSpec extends SpecBase {

  private val service = new MessageIdentificationService()

  "randomIdentifier" must {
    "generate a random string of 35 characters" in {
      val result = service.randomIdentifier
      result.length shouldEqual 35
      result.matches("^[a-zA-Z0-9]*$") shouldEqual true
    }
  }
}
