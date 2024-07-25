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

import cats.data.NonEmptyList
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

sealed trait Rejection {
  val departureId: String
}

object Rejection {

  sealed trait BusinessRejectionType

  object BusinessRejectionType {
    case object AmendmentRejection extends BusinessRejectionType
    case object DeclarationRejection extends BusinessRejectionType

    implicit val reads: Reads[BusinessRejectionType] =
      __.read[String].flatMap {
        case "013" => Reads.pure(AmendmentRejection)
        case "015" => Reads.pure(DeclarationRejection)
        case value => Reads.failed(s"Unexpected business rejection type: $value")
      }
  }

  case class IE055Rejection(departureId: String) extends Rejection

  case class IE056Rejection(departureId: String, businessRejectionType: BusinessRejectionType, errorPointers: Option[NonEmptyList[XPath]]) extends Rejection

  implicit val reads: Reads[Rejection] =
    (__ \ "type").read[String].flatMap {
      case "IE055" =>
        (__ \ "departureId").read[String].map(IE055Rejection)
      case "IE056" =>
        (
          (__ \ "departureId").read[String] and
            (__ \ "businessRejectionType").read[BusinessRejectionType] and
            (__ \ "errorPointers").readNullable[NonEmptyList[XPath]]
        )(IE056Rejection.apply _)
      case value => Reads.failed(s"Unexpected rejection type: $value")
    }
}
