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

package api.submission

import generated.{GuaranteeReferenceType03, GuaranteeType01, GuaranteeType02}
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, JsArray, Reads}

object Guarantee {

  def transform(uA: UserAnswers): Seq[GuaranteeType02] = uA
    .get[JsArray](guaranteesPath)
    .readValuesAs[GuaranteeType02](guaranteeType02.reads)
    .groupByPreserveOrder {
      x => (x.guaranteeType, x.otherGuaranteeReference)
    }
    .zipWithSequenceNumber
    .map {
      case (((guaranteeType, otherGuaranteeReference), guarantees), index) =>
        GuaranteeType02(
          sequenceNumber = index.toString,
          guaranteeType = guaranteeType,
          otherGuaranteeReference = otherGuaranteeReference,
          GuaranteeReference = guarantees.flatMap(_.GuaranteeReference).toSeq.zipWithSequenceNumber.map {
            case (guaranteeReference, index) =>
              guaranteeReference.copy(sequenceNumber = index.toString)
          }
        )
    }

  def transformIE013(uA: UserAnswers): Seq[GuaranteeType01] = uA
    .get[JsArray](guaranteesPath)
    .readValuesAs[GuaranteeType01](guaranteeType01.reads)
    .groupByPreserveOrder {
      x => (x.guaranteeType, x.otherGuaranteeReference)
    }
    .zipWithSequenceNumber
    .map {
      case (((guaranteeType, otherGuaranteeReference), guarantees), index) =>
        GuaranteeType01(
          sequenceNumber = index.toString,
          guaranteeType = guaranteeType,
          otherGuaranteeReference = otherGuaranteeReference,
          GuaranteeReference = guarantees.flatMap(_.GuaranteeReference).toSeq.zipWithSequenceNumber.map {
            case (guaranteeReference, index) =>
              guaranteeReference.copy(sequenceNumber = index.toString)
          }
        )
    }
}

object guaranteeType01 {

  def reads(index: Int): Reads[GuaranteeType01] = (
    (index.toString: Reads[String]) and
      (__ \ "guaranteeType").read[String].map(Some(_)) and
      (__ \ "otherReference").readNullable[String] and
      __.read[GuaranteeReferenceType03](guaranteeReferenceType03.reads(index)).map(Seq(_))
  )(GuaranteeType01.apply _)
}

object guaranteeType02 {

  def reads(index: Int): Reads[GuaranteeType02] = (
    (index.toString: Reads[String]) and
      (__ \ "guaranteeType").read[String] and
      (__ \ "otherReference").readNullable[String] and
      __.read[GuaranteeReferenceType03](guaranteeReferenceType03.reads(index)).map(Seq(_))
  )(GuaranteeType02.apply _)
}

object guaranteeReferenceType03 {

  def reads(index: Int): Reads[GuaranteeReferenceType03] = (
    (index.toString: Reads[String]) and
      (__ \ "referenceNumber").readNullable[String] and
      (__ \ "accessCode").readNullable[String] and
      (__ \ "liabilityAmount").readNullable[BigDecimal] and
      (__ \ "currency" \ "currency").readNullable[String]
  )(GuaranteeReferenceType03.apply _)
}
