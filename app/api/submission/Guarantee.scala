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

import generated._
import models.UserAnswers
//import models.journeyDomain.guaranteeDetails.{GuaranteeDetailsDomain, GuaranteeDomain}

object Guarantee {

//  def transform(uA: UserAnswers): Seq[GuaranteeType02] =
//    domain.guarantees.map {
//      case guaranteeDomain @ GuaranteeDomain.GuaranteeOfTypesAB(guaranteeType) =>
//        GuaranteeType02(
//          guaranteeDomain.index.position.toString,
//          guaranteeType.toString,
//          None
//        )
//      case guaranteeDomain @ GuaranteeDomain.GuaranteeOfTypes01249(guaranteeType, grn, currency, liabilityAmount, accessCode) =>
//        GuaranteeType02(
//          guaranteeDomain.index.position.toString,
//          guaranteeType.toString,
//          None,
//          Seq(GuaranteeReferenceType03(guaranteeDomain.index.position.toString, Some(grn), Some(accessCode), Some(liabilityAmount), Some(currency.currency)))
//        )
//      case guaranteeDomain @ GuaranteeDomain.GuaranteeOfType5(guaranteeType, currency, liabilityAmount) =>
//        GuaranteeType02(
//          guaranteeDomain.index.position.toString,
//          guaranteeType.toString,
//          None,
//          Seq(GuaranteeReferenceType03(guaranteeDomain.index.position.toString, None, None, Some(liabilityAmount), Some(currency.currency)))
//        )
//      case guaranteeDomain @ GuaranteeDomain.GuaranteeOfType8(guaranteeType, otherReference, currencyCode, liabilityAmount) =>
//        GuaranteeType02(
//          guaranteeDomain.index.position.toString,
//          guaranteeType.toString,
//          Some(otherReference),
//          Seq(
//            GuaranteeReferenceType03(
//              guaranteeDomain.index.position.toString,
//              None,
//              None,
//              Some(liabilityAmount),
//              Some(currencyCode.currency)
//            )
//          )
//        )
//      case guaranteeDomain @ GuaranteeDomain.GuaranteeOfType3WithReference(guaranteeType, otherReference, currencyCode, liabilityAmount) =>
//        GuaranteeType02(
//          guaranteeDomain.index.position.toString,
//          guaranteeType.toString,
//          Some(otherReference),
//          Seq(
//            GuaranteeReferenceType03(
//              guaranteeDomain.index.position.toString,
//              None,
//              None,
//              Some(liabilityAmount),
//              Some(currencyCode.currency)
//            )
//          )
//        )
//      case guaranteeDomain @ GuaranteeDomain.GuaranteeOfType3WithoutReference(guaranteeType) =>
//        GuaranteeType02(
//          guaranteeDomain.index.position.toString,
//          guaranteeType.toString,
//          None,
//          Seq.empty
//        )
//    }
}
