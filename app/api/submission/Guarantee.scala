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

import generated.{GuaranteeReferenceType03, GuaranteeType02}
import gettables.sections.GuaranteeDetailsSection
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

object Guarantee {

  def transform(uA: UserAnswers): Seq[GuaranteeType02] =
    uA.get(GuaranteeDetailsSection)
      .map {
        _.value.zipWithIndex.map {
          case (value, i) => value.as[GuaranteeType02](GuaranteeType02.reads(i))
        }
      }
      .getOrElse(Seq.empty)
      .toSeq
}

object GuaranteeType02 {

  def apply(
    guaranteeType: String,
    otherGuaranteeReference: Option[String] = None,
    GuaranteeReference: Seq[GuaranteeReferenceType03]
  )(
    sequenceNumber: String
  ): GuaranteeType02 = new GuaranteeType02(sequenceNumber, guaranteeType, otherGuaranteeReference, GuaranteeReference)

  def reads(index: Int): Reads[GuaranteeType02] = (
    (__ \ "guaranteeType").read[String] and
      (__ \ "otherReference").readNullable[String] and
      __.read[GuaranteeReferenceType03](GuaranteeReferenceType03.reads(index)).map(Seq(_))
  ).tupled.map((GuaranteeType02.apply _).tupled).map(_(index.toString))
}

object GuaranteeReferenceType03 {

  def apply(
    GRN: Option[String] = None,
    accessCode: Option[String] = None,
    amountToBeCovered: Option[BigDecimal] = None,
    currency: Option[String] = None
  )(
    sequenceNumber: String
  ): GuaranteeReferenceType03 = new GuaranteeReferenceType03(sequenceNumber, GRN, accessCode, amountToBeCovered, currency)

  def reads(index: Int): Reads[GuaranteeReferenceType03] = (
    (__ \ "referenceNumber").readNullable[String] and
      (__ \ "accessCode").readNullable[String] and
      (__ \ "liabilityAmount").readNullable[BigDecimal] and
      (__ \ "currency" \ "currency").readNullable[String]
  ).tupled.map((GuaranteeReferenceType03.apply _).tupled).map(_(index.toString))
}
