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

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Document(`type`: String, code: String, referenceNumber: String)

object Document {

  val readsFromItemsFrontend: Reads[Document] = (
    (__ \ "document" \ "type").read[String] and
      (__ \ "document" \ "code").read[String] and
      (__ \ "document" \ "referenceNumber").read[String]
  )(Document.apply _)

  val readsFromDocumentsFrontend: Reads[Document] = {
    def reads(key: String): Reads[Document] = (
      (__ \ key \ "type").read[String] and
        (__ \ key \ "code").read[String] and
        (__ \ "details" \ "documentReferenceNumber").read[String]
    )(Document.apply _)

    reads("type") orElse reads("previousDocumentType")
  }
}
