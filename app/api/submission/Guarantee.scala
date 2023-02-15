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
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, JsArray, Reads}

object Guarantee {

  def transform(uA: UserAnswers): Seq[GuaranteeType02] = uA
    .get[JsArray](guaranteesPath)
    .readValuesAs[GuaranteeType02](guaranteeType02.reads)
}

object guaranteeType02 {

  def apply(guaranteeType: String, otherGuaranteeReference: Option[String], GuaranteeReference: Seq[GuaranteeReferenceType03])(
    sequenceNumber: Int
  ): GuaranteeType02 =
    GuaranteeType02(sequenceNumber.toString, guaranteeType, otherGuaranteeReference, GuaranteeReference)

  def reads(index: Int): Reads[GuaranteeType02] = (
    (__ \ "guaranteeType").read[String] and
      (__ \ "otherReference").readNullable[String] and
      __.read[GuaranteeReferenceType03](guaranteeReferenceType03.reads(index)).map(Seq(_))
  ).tupled.map((guaranteeType02.apply _).tupled).map(_(index))
}

object guaranteeReferenceType03 {

  def apply(GRN: Option[String], accessCode: Option[String], amountToBeCovered: Option[BigDecimal], currency: Option[String])(
    sequenceNumber: Int
  ): GuaranteeReferenceType03 =
    GuaranteeReferenceType03(sequenceNumber.toString, GRN, accessCode, amountToBeCovered, currency)

  def reads(index: Int): Reads[GuaranteeReferenceType03] = (
    (__ \ "referenceNumber").readNullable[String] and
      (__ \ "accessCode").readNullable[String] and
      (__ \ "liabilityAmount").readNullable[BigDecimal] and
      (__ \ "currency" \ "currency").readNullable[String]
  ).tupled.map((guaranteeReferenceType03.apply _).tupled).map(_(index))
}
