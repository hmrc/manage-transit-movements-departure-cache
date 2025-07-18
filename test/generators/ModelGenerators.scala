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

package generators

import models.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {
  self: Generators =>

  lazy val stringMaxLength = 36

  implicit lazy val arbitrarySubmissionState: Arbitrary[SubmissionState] = Arbitrary {
    val values = Seq(
      SubmissionState.NotSubmitted,
      SubmissionState.Submitted,
      SubmissionState.RejectedPendingChanges
    )
    Gen.oneOf(values)
  }

  implicit lazy val arbitraryMovementReferenceNumber: Arbitrary[MovementReferenceNumber] =
    Arbitrary {
      for {
        mrn <- stringsWithMaxLength(22: Int, Gen.alphaNumChar)
      } yield new MovementReferenceNumber(Some(mrn))
    }

  implicit lazy val arbitraryXPath: Arbitrary[XPath] = Arbitrary {
    val validXPaths = Seq(
      "/CC015C/TransitOperation/bindingItinerary",
      "/CC015C/TransitOperation/reducedDatasetIndicator",
      "/CC015C/TransitOperation/limitDate",
      "/CC015C/Authorisation[1]/referenceNumber",
      "/CC015C/CustomsOfficeOfDestinationDeclared[1]/country",
      "/CC015C/CustomsOfficeOfTransitDeclared[1]/country",
      "/CC015C/CustomsOfficeOfExitForTransitDeclared[1]/country",
      "/CC015C/HolderOfTheTransitProcedure[1]/country",
      "/CC015C/Representative/name",
      "/CC015C/Guarantee/name",
      "/CC015C/Consignment/Carrier/name",
      "/CC015C/Consignment/AdditionalSupplyChainActor[1]/type",
      "/CC015C/Consignment/DepartureTransportMeans[1]/type",
      "/CC015C/Consignment/Consignor/name",
      "/CC015C/Consignment/Consignee/name",
      "/CC015C/Consignment/LocationOfGoods/country",
      "/CC015C/Consignment/TransportEquipment[1]/equipmentType",
      "/CC015C/Consignment/ActiveBorderTransportMeans[1]/borderModeOfTransport",
      "/CC015C/Consignment/PlaceOfLoading[1]/country",
      "/CC015C/Consignment/PlaceOfUnloading[1]/country",
      "/CC015C/Consignment/PreviousDocument[1]/type",
      "/CC015C/Consignment/SupportingDocument[1]/type",
      "/CC015C/Consignment/TransportDocument[1]/type",
      "/CC015C/Consignment/HouseConsignment[1]/ConsignmentItem[1]/PreviousDocument/type",
      "/CC015C/Consignment/HouseConsignment[1]/ConsignmentItem[1]/SupportingDocument/type",
      "/CC015C/Consignment/HouseConsignment[1]/ConsignmentItem[1]/TransportDocument/type",
      "/CC015C/Consignment/HouseConsignment[1]/ConsignmentItem[1]/additionalInformation",
      "/CC015C/Consignment/countryOfDispatch",
      "/CC015C/Consignment/countryOfDestination",
      "/CC015C/Consignment/containerIndicator",
      "/CC015C/Consignment/inlandModeOfTransport",
      "/CC015C/Consignment/modeOfTransportAtTheBorder",
      "/CC015C/Consignment/referenceNumberUCR",
      "/CC015C/Consignment/CountryOfRoutingOfConsignment/country",
      "/CC015C/Consignment/TransportCharges/paymentMethod"
    )
    for {
      value <- Gen.oneOf(validXPaths)
    } yield XPath(value)
  }

  implicit lazy val arbitraryFunctionalError: Arbitrary[FunctionalError] =
    Arbitrary {
      for {
        errorPointer           <- arbitrary[XPath]
        errorCode              <- Gen.alphaNumStr
        errorReason            <- Gen.alphaNumStr
        originalAttributeValue <- Gen.option(Gen.alphaNumStr)
      } yield FunctionalError(errorPointer, errorCode, errorReason, originalAttributeValue)
    }

  implicit lazy val arbitraryVersion: Arbitrary[Phase] =
    Arbitrary {
      Gen.oneOf(Phase.Phase5, Phase.Phase6)
    }
}
