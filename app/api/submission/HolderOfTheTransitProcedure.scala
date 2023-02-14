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

object HolderOfTheTransitProcedure {

//  def transform(uA: UserAnswers): HolderOfTheTransitProcedureType14 =
//    domain.holderOfTransit match {
//      case HolderOfTransitDomain.HolderOfTransitEori(eori, name, country, address, additionalContact) =>
//        holderOfTheTransitProcedure(eori.map(
//                                      x => x.value
//                                    ),
//                                    Some(name),
//                                    country,
//                                    address,
//                                    additionalContact
//        )
//      case HolderOfTransitDomain.HolderOfTransitTIR(tir, name, country, address, additionalContact) =>
//        holderOfTheTransitProcedure(tir, Some(name), country, address, additionalContact)
//    }
//
//  private def holderOfTheTransitProcedure(id: Option[String],
//                                          name: Option[String],
//                                          country: Country,
//                                          address: DynamicAddress,
//                                          additionalContact: Option[AdditionalContactDomain]
//  ) =
//    HolderOfTheTransitProcedureType14(
//      identificationNumber = id,
//      TIRHolderIdentificationNumber = None,
//      name = name,
//      Address = Some(AddressType17(address.numberAndStreet, address.postalCode, address.city, country.code.code)),
//      ContactPerson = additionalContact.map(
//        x => ContactPersonType05(x.name, x.telephoneNumber, None)
//      )
//    )
}
