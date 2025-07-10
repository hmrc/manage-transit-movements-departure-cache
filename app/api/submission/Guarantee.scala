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

import generated.*
import models.UserAnswers
import play.api.libs.functional.syntax.*
import play.api.libs.json.{__, JsArray, Reads}

object Guarantee {

  def transform(uA: UserAnswers): Seq[GuaranteeType05] =
    uA.get[JsArray](guaranteesPath)
      .readValuesAs[GuaranteeType05](guaranteeType05.reads)
      .groupByPreserveOrder {
        x =>
          (x.guaranteeType, x.otherGuaranteeReference)
      }
      .zipWithSequenceNumber
      .map {
        case (((guaranteeType, otherGuaranteeReference), guarantees), index) =>
          GuaranteeType05(
            sequenceNumber = index,
            guaranteeType = guaranteeType,
            otherGuaranteeReference = otherGuaranteeReference,
            GuaranteeReference = guarantees.flatMap(_.GuaranteeReference).toSeq.zipWithSequenceNumber.map {
              case (guaranteeReference, index) =>
                guaranteeReference.copy(sequenceNumber = index)
            }
          )
      }
}

object guaranteeType05 {

  def reads(index: Int): Reads[GuaranteeType05] =
    (
      Reads.pure[BigInt](index) and
        (__ \ "guaranteeType" \ "code").read[String] and
        (__ \ "otherReference").readNullable[String] and
        __.read[Option[GuaranteeReferenceType03]](guaranteeReferenceType03.reads(index)).map(_.toSeq)
    )(GuaranteeType05.apply)
}

object guaranteeReferenceType03 {

  def reads(index: Int): Reads[Option[GuaranteeReferenceType03]] =
    (
      Reads.pure[BigInt](index) and
        (__ \ "referenceNumber").readNullable[String] and
        (__ \ "accessCode").readNullable[String] and
        (__ \ "liabilityAmount").readNullable[BigDecimal] and
        (__ \ "currency" \ "currency").readNullable[String]
    ).tupled.map {
      case (sequenceNumber, grn, accessCode, Some(amountToBeCovered), Some(currency)) =>
        Some(GuaranteeReferenceType03(sequenceNumber, grn, accessCode, amountToBeCovered, currency))
      case _ => None
    }
}
