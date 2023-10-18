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

import base.{AppWithDefaultMockFixtures, SpecBase}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers.running
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

class SensitiveFormatsSpec extends SpecBase with AppWithDefaultMockFixtures {

  "SensitiveString" when {
    val encryptedValue = "4CocWZm74h6iozJ1bq7K3WatZr4Tk7kWV0pj6W+n0ObUAPY1jw=="
    val decryptedValue = SensitiveString("ABC")

    "reads" when {
      "encryption enabled" must {
        "read and decrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(encryptedValue).as[SensitiveString](sensitiveFormats.sensitiveStringReads)
            result shouldBe decryptedValue
          }
        }
      }

      "encryption disabled" must {
        "read and not decrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue.decryptedValue).as[SensitiveString](sensitiveFormats.sensitiveStringReads)
            result shouldBe decryptedValue
          }
        }
      }
    }

    "writes" when {
      "encryption enabled" must {
        "write and encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.sensitiveStringWrites)
            result.as[JsString].value should not be decryptedValue.decryptedValue
          }
        }
      }

      "encryption disabled" must {
        "write and not encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.sensitiveStringWrites)
            result shouldBe JsString(decryptedValue.decryptedValue)
          }
        }
      }
    }
  }

  "JsObject" when {
    val encryptedValue = "WFYrOuMf6WHDjHooyzED80QIGXMTPSHEjc3Kl8jPFRJFtHWV"
    val decryptedValue = Json.obj()

    "reads" when {
      "encryption enabled" must {
        "read and decrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(encryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result shouldBe decryptedValue
          }
        }
      }

      "encryption disabled" must {
        "read and not decrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result shouldBe decryptedValue
          }
        }
      }
    }

    "writes" when {
      "encryption enabled" must {
        "write and encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.jsObjectWrites)
            result should not be decryptedValue
          }
        }
      }

      "encryption disabled" must {
        "write and not encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.jsObjectWrites)
            result shouldBe decryptedValue
          }
        }
      }
    }
  }
}
