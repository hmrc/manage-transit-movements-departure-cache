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

import models.SensitiveFormats.RichJsObject
import org.mongodb.scala.bson.BsonValue
import play.api.libs.json._
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.play.json.Codecs

class SensitiveFormats(encryptionEnabled: Boolean)(implicit crypto: Encrypter with Decrypter) {

  implicit val sensitiveStringReads: Reads[SensitiveString] = {
    if (encryptionEnabled) {
      JsonEncryption.sensitiveDecrypter(SensitiveString.apply)
    } else {
      implicitly[Reads[String]].map(SensitiveString.apply)
    }
  }

  implicit val sensitiveStringWrites: Writes[SensitiveString] = {
    if (encryptionEnabled) {
      JsonEncryption.sensitiveEncrypter[String, SensitiveString]
    } else {
      implicitly[Writes[String]].contramap(_.decryptedValue)
    }
  }

  implicit val sensitiveStringFormat: Format[SensitiveString] =
    Format(sensitiveStringReads, sensitiveStringWrites)

  val jsObjectReads: Reads[JsObject] =
    if (encryptionEnabled) {
      __.read[SensitiveString].map(_.decryptedValue).map(Json.parse(_).as[JsObject])
    } else {
      __.read[JsObject]
    }

  val jsObjectWrites: Writes[JsObject] =
    if (encryptionEnabled) {
      __.write[SensitiveString].contramap(_.encrypt)
    } else {
      __.write[JsObject]
    }

  def bsonData(data: JsObject): BsonValue =
    if (encryptionEnabled) {
      Codecs.toBson(data.encrypt)
    } else {
      Codecs.toBson(data)
    }
}

object SensitiveFormats {

  implicit class RichJsObject(jsObject: JsObject) {
    def encrypt: SensitiveString = SensitiveString(Json.stringify(jsObject))
  }
}
