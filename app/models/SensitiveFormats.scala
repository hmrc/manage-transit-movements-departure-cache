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

import play.api.libs.json.{Format, Reads, Writes}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}

class SensitiveFormats(encryptionEnabled: Boolean)(implicit crypto: Encrypter with Decrypter) {

  implicit val reads: Reads[SensitiveString] = {
    if (encryptionEnabled) {
      JsonEncryption.sensitiveDecrypter(SensitiveString.apply)
    } else {
      SensitiveFormats.nonSensitiveReads
    }
  }

  implicit val writes: Writes[SensitiveString] = {
    if (encryptionEnabled) {
      JsonEncryption.sensitiveEncrypter[String, SensitiveString]
    } else {
      SensitiveFormats.nonSensitiveWrites
    }
  }

  implicit val format: Format[SensitiveString] =
    Format(reads, writes)
}

object SensitiveFormats {

  implicit val nonSensitiveReads: Reads[SensitiveString] =
    implicitly[Reads[String]].map(SensitiveString.apply)

  implicit val nonSensitiveWrites: Writes[SensitiveString] =
    implicitly[Writes[String]].contramap(_.decryptedValue)

  implicit val nonSensitiveFormat: Format[SensitiveString] =
    Format(nonSensitiveReads, nonSensitiveWrites)
}
