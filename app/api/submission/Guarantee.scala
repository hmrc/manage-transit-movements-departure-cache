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

  private case class GuaranteeType(
    sequenceNumber: String,
    guaranteeType: String,
    otherGuaranteeReference: Option[String] = None,
    GuaranteeReference: Seq[generated.GuaranteeReferenceType03] = Nil
  ) {

    def asGuaranteeType01: GuaranteeType01 =
      GuaranteeType01(sequenceNumber, Some(guaranteeType), otherGuaranteeReference, GuaranteeReference)

    def asGuaranteeType02: GuaranteeType02 =
      GuaranteeType02(sequenceNumber, guaranteeType, otherGuaranteeReference, GuaranteeReference)
  }

  private object GuaranteeType {

    def reads(index: Int): Reads[GuaranteeType] =
      (
        (index.toString: Reads[String]) and
          (__ \ "guaranteeType" \ "code").read[String] and
          (__ \ "otherReference").readNullable[String] and
          __.read[GuaranteeReferenceType03](guaranteeReferenceType03.reads(index)).map(Seq(_))
      )(GuaranteeType.apply)
  }

  def transform(uA: UserAnswers): Seq[GuaranteeType02] =
    transform[GuaranteeType02](uA)(_.asGuaranteeType02)

  def transformIE013(uA: UserAnswers): Seq[GuaranteeType01] =
    transform[GuaranteeType01](uA)(_.asGuaranteeType01)

  private def transform[T](uA: UserAnswers)(f: GuaranteeType => T): Seq[T] =
    uA.get[JsArray](guaranteesPath)
      .readValuesAs[GuaranteeType](GuaranteeType.reads)
      .groupByPreserveOrder {
        x =>
          (x.guaranteeType, x.otherGuaranteeReference)
      }
      .zipWithSequenceNumber
      .map {
        case (((guaranteeType, otherGuaranteeReference), guarantees), index) =>
          GuaranteeType(
            sequenceNumber = index.toString,
            guaranteeType = guaranteeType,
            otherGuaranteeReference = otherGuaranteeReference,
            GuaranteeReference = guaranteeReference(guarantees)
          )
      }
      .map(f)

  private def guaranteeReference(guarantees: Iterable[GuaranteeType]): Seq[GuaranteeReferenceType03] =
    guarantees.flatMap(_.GuaranteeReference).toSeq.zipWithSequenceNumber.map {
      case (guaranteeReference, index) =>
        guaranteeReference.copy(sequenceNumber = index.toString)
    }
}

object guaranteeReferenceType03 {

  def reads(index: Int): Reads[GuaranteeReferenceType03] =
    (
      (index.toString: Reads[String]) and
        (__ \ "referenceNumber").readNullable[String] and
        (__ \ "accessCode").readNullable[String] and
        (__ \ "liabilityAmount").readNullable[BigDecimal] and
        (__ \ "currency" \ "currency").readNullable[String]
    )(GuaranteeReferenceType03.apply)
}
