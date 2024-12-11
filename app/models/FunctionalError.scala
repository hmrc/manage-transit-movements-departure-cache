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

package models

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import scala.annotation.tailrec

case class FunctionalError(
  errorPointer: XPath,
  errorCode: String,
  errorReason: String,
  originalAttributeValue: Option[String]
) {

  def section: Option[String] = errorPointer.task.map(_.toString)
}

object FunctionalError {

  implicit val reads: Reads[FunctionalError] = Json.reads[FunctionalError]

  implicit val writes: Writes[FunctionalError] = (
    (__ \ "error").write[String] and
      (__ \ "businessRuleId").write[String] and
      (__ \ "section").writeNullable[String] and
      (__ \ "invalidDataItem").write[String] and
      (__ \ "invalidAnswer").writeNullable[String]
  )(
    functionalError =>
      (functionalError.errorCode,
       functionalError.errorReason,
       functionalError.section,
       functionalError.errorPointer.value,
       functionalError.originalAttributeValue
      )
  )
}
