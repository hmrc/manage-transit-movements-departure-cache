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

object CustomsOffices {

//  def transformOfficeOfDeparture(uA: UserAnswers): CustomsOfficeOfDepartureType03 =
//    CustomsOfficeOfDepartureType03(customsOffice.id)
//
//  def transformOfficeOfDestination(uA: UserAnswers): CustomsOfficeOfDestinationDeclaredType01 =
//    CustomsOfficeOfDestinationDeclaredType01(customsOffice.id)
//
//  def transformOfficeOfTransit(uA: UserAnswers): Seq[CustomsOfficeOfTransitDeclaredType03] =
//    domain
//      .map(
//        transitDomain =>
//          transitDomain.officesOfTransit
//            .map(
//              officeOfTransitDomain =>
//                CustomsOfficeOfTransitDeclaredType03(
//                  transitDomain.officesOfTransit.indexOf(officeOfTransitDomain.customsOffice).toString,
//                  officeOfTransitDomain.customsOffice.id
//                )
//            )
//      )
//      .getOrElse(Seq.empty)
//
//  def transformOfficeOfExit(uA: UserAnswers): Seq[CustomsOfficeOfExitForTransitDeclaredType02] =
//    domain
//      .map(
//        transitDomain =>
//          transitDomain.officesOfExit
//            .map(
//              officeOfExitDomain =>
//                CustomsOfficeOfExitForTransitDeclaredType02(
//                  transitDomain.officesOfExit.indexOf(officeOfExitDomain).toString,
//                  officeOfExitDomain.customsOffice.id
//                )
//            )
//      )
//      .getOrElse(Seq.empty)
}
