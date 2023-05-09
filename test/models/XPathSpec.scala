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

package models

import base.SpecBase
import generators.Generators
import models.JourneyTask._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.JsString

class XPathSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must deserialise" in {
    forAll(Gen.alphaNumStr) {
      xPath =>
        val json   = JsString(xPath)
        val result = json.as[XPath]
        result shouldBe XPath(xPath)
    }
  }

  "isAmendable" must {

    "return true" when {
      "xPath is prepended with /CC015C/TransitOperation" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/TransitOperation" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/Authorisation" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/Authorisation" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/CustomsOfficeOfDeparture" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/CustomsOfficeOfDeparture" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/CustomsOfficeOfDestinationDeclared" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/CustomsOfficeOfDestinationDeclared" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/CustomsOfficeOfTransitDeclared" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/CustomsOfficeOfTransitDeclared" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/CustomsOfficeOfExitForTransitDeclared" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/CustomsOfficeOfExitForTransitDeclared" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/HolderOfTheTransitProcedure" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/HolderOfTheTransitProcedure" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/Representative" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/Representative" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/Guarantee" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/Guarantee" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }

      "xPath is prepended with /CC015C/Consignment" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/Consignment" + subPath
            XPath(xPath).isAmendable shouldBe true
        }
      }
    }

    "return false" when {
      "xPath is prepended with CC015C but doesn't have a recognised section" in {
        forAll(Gen.alphaNumStr) {
          subPath =>
            val xPath = "/CC015C/" + subPath
            XPath(xPath).isAmendable shouldBe false
        }
      }

      "xPath is anything else" in {
        forAll(Gen.alphaNumStr) {
          xPath =>
            XPath(xPath).isAmendable shouldBe false
        }
      }
    }
  }

  "task" when {
    "when /CC015C/TransitOperation/declarationType" must {
      "return PreTaskList" in {
        val xPath = XPath("/CC015C/TransitOperation/declarationType")
        xPath.task.value shouldBe PreTaskList
      }
    }

    "when /CC015C/TransitOperation/tirCarnetReference" must {
      "return PreTaskList" in {
        val xPath = XPath("/CC015C/TransitOperation/tirCarnetReference")
        xPath.task.value shouldBe PreTaskList
      }
    }

    "when /CC015C/TransitOperation/securityDetailsType" must {
      "return PreTaskList" in {
        val xPath = XPath("/CC015C/TransitOperation/securityDetailsType")
        xPath.task.value shouldBe PreTaskList
      }
    }

    "when /CC015C/TransitOperation/routing/bindingItinerary" must {
      "return routeDetails" in {
        val xPath = XPath("/CC015C/TransitOperation/routing/bindingItinerary")
        xPath.task.value shouldBe RouteDetails
      }
    }

    "when /CC015C/Authorisation[1]/referenceNumber" must {
      "return TransportDetails" in {
        val xPath = XPath("/CC015C/Authorisation[1]/referenceNumber")
        xPath.task.value shouldBe TransportDetails
      }
    }

    "when /CC015C/CustomsOfficeOfDeparture/" must {
      "return PreTaskList" in {
        val xPath = XPath("/CC015C/CustomsOfficeOfDeparture/")
        xPath.task.value shouldBe PreTaskList
      }
    }

    "when /CC015C/CustomsOfficeOfDestinationDeclared/" must {
      "return RouteDetails" in {
        val xPath = XPath("/CC015C/CustomsOfficeOfDestinationDeclared/")
        xPath.task.value shouldBe RouteDetails
      }
    }

    "when /CC015C/CustomsOfficeOfTransitDeclared/" must {
      "return RouteDetails" in {
        val xPath = XPath("/CC015C/CustomsOfficeOfTransitDeclared/")
        xPath.task.value shouldBe RouteDetails
      }
    }

    "when /CC015C/CustomsOfficeOfExitForTransitDeclared/" must {
      "return RouteDetails" in {
        val xPath = XPath("/CC015C/CustomsOfficeOfExitForTransitDeclared/")
        xPath.task.value shouldBe RouteDetails
      }
    }

    "when /CC015C/HolderOfTheTransitProcedure/TIRHolderIdentificationNumber" must {
      "return TraderDetails" in {
        val xPath = XPath("/CC015C/HolderOfTheTransitProcedure/TIRHolderIdentificationNumber")
        xPath.task.value shouldBe TraderDetails
      }
    }

    "when /CC015C/Representative/status" must {
      "return TraderDetails" in {
        val xPath = XPath("/CC015C/Representative/status")
        xPath.task.value shouldBe TraderDetails
      }
    }

    "when /CC015C/Consignment/Carrier/identificationNumber" must {
      "return TransportDetails" in {
        val xPath = XPath("/CC015C/Consignment/Carrier/identificationNumber")
        xPath.task.value shouldBe TransportDetails
      }
    }

    "when /CC015C/Consignment/AdditionalSupplyChainActor[1]" must {
      "return TransportDetails" in {
        val xPath = XPath("/CC015C/Consignment/AdditionalSupplyChainActor[1]")
        xPath.task.value shouldBe TransportDetails
      }
    }

    "when /CC015C/Consignment/DepartureTransportMeans[1]" must {
      "return TransportDetails" in {
        val xPath = XPath("/CC015C/Consignment/DepartureTransportMeans[1]")
        xPath.task.value shouldBe TransportDetails
      }
    }

    "when /CC015C/Guarantee[1]/guaranteeType" must {
      "return GuaranteeDetails" in {
        val xPath = XPath("/CC015C/Guarantee[1]/guaranteeType")
        xPath.task.value shouldBe GuaranteeDetails
      }
    }

    "when /CC015C/Consignment/Consignor/name" must {
      "return TraderDetails" in {
        val xPath = XPath("/CC015C/Consignment/Consignor/name")
        xPath.task.value shouldBe TraderDetails
      }
    }

    "when /CC015C/Consignment/Consignee" must {
      "return TraderDetails" in {
        val xPath = XPath("/CC015C/Consignment/Consignee/name")
        xPath.task.value shouldBe TraderDetails
      }
    }

    "when /CC015C/Consignment/TransportEquipment/paymentMethod" must {
      "return TransportDetails" in {
        val xPath = XPath("/CC015C/Consignment/TransportEquipment/paymentMethod")
        xPath.task.value shouldBe TransportDetails
      }
    }

    "when /CC015C/Consignment/ActiveBorderTransportMeans/identificationNumber" must {
      "return TransportDetails" in {
        val xPath = XPath("/CC015C/Consignment/ActiveBorderTransportMeans/identificationNumber")
        xPath.task.value shouldBe TransportDetails
      }
    }

    "when /CC015C/Consignment/LocationOfGoods/typeOfLocation" must {
      "return RouteDetails" in {
        val xPath = XPath("/CC015C/Consignment/LocationOfGoods/typeOfLocation")
        xPath.task.value shouldBe RouteDetails
      }
    }

    "when /CC015C/Consignment/PlaceOfLoading/country" must {
      "return RouteDetails" in {
        val xPath = XPath("/CC015C/Consignment/PlaceOfLoading/country")
        xPath.task.value shouldBe RouteDetails
      }
    }

    "when /CC015C/Consignment/PlaceOfUnloading/country" must {
      "return RouteDetails" in {
        val xPath = XPath("/CC015C/Consignment/PlaceOfUnloading/country")
        xPath.task.value shouldBe RouteDetails
      }
    }

    "when /CC015C/Consignment/PreviousDocument[5]/type" must {
      "return Documents" in {
        val xPath = XPath("/CC015C/Consignment/PreviousDocument[5]/type")
        xPath.task.value shouldBe Documents
      }
    }

    "when /CC015C/Consignment/SupportingDocument[5]/type" must {
      "return Documents" in {
        val xPath = XPath("/CC015C/Consignment/SupportingDocument[5]/type")
        xPath.task.value shouldBe Documents
      }
    }

    "when /CC015C/Consignment/TransportDocument[5]/type" must {
      "return Documents" in {
        val xPath = XPath("/CC015C/Consignment/TransportDocument[5]/type")
        xPath.task.value shouldBe Documents
      }
    }

    "when /CC015C/Consignment/HouseConsignment[5]/ConsingmentItem[10]/consignor" must {
      "return Items" in {
        val xPath = XPath("/CC015C/Consignment/HouseConsignment[50]/ConsingmentItem[10]/consignor")
        xPath.task.value shouldBe Items
      }
    }

    "when /CC015C/Consignment/HouseConsignment[5]/ConsingmentItem[10]/additionalInformation" must {
      "return Items" in {
        val xPath = XPath("/CC015C/Consignment/HouseConsignment[50]/ConsingmentItem[10]/additionalInformation")
        xPath.task.value shouldBe Items
      }
    }

    "when /CC015C/Consignment/HouseConsignment[5]/ConsingmentItem[10]/additionalReference" must {
      "return Items" in {
        val xPath = XPath("/CC015C/Consignment/HouseConsignment[50]/ConsingmentItem[10]/additionalReference")
        xPath.task.value shouldBe Items
      }
    }

    "when /CC015C/Consignment/HouseConsignment[5]/ConsingmentItem[10]/SupportingDocument[22]/type" must {
      "return Documents" in {
        val xPath = XPath("/CC015C/Consignment/HouseConsignment[50]/ConsingmentItem[10]/SupportingDocument[22]/type")
        xPath.task.value shouldBe Documents
      }
    }

    "when /CC015C/Consignment/HouseConsignment[5]/ConsingmentItem[10]/TransportDocument[22]/type" must {
      "return Documents" in {
        val xPath = XPath("/CC015C/Consignment/HouseConsignment[50]/ConsingmentItem[10]/TransportDocument[22]/type")
        xPath.task.value shouldBe Documents
      }
    }

    "when /CC015C/Consignment/HouseConsignment[5]/ConsingmentItem[10]/PreviousDocument[22]/type" must {
      "return Documents" in {
        val xPath = XPath("/CC015C/Consignment/HouseConsignment[50]/ConsingmentItem[10]/PreviousDocument[22]/type")
        xPath.task.value shouldBe Documents
      }
    }

    "when something else" must {
      "return None" in {
        val xPath = XPath("/CC014C")
        xPath.task shouldBe None
      }
    }
  }

  "sectionError" must {

    "return Some((.preTaskList, Status.Value.Error))" when {
      "xPath.task returns PreTaskList" in {
        val xPath = s"/CC015C/TransitOperation/declarationType"
        XPath(xPath).sectionError shouldBe Some((".preTaskList", Status(Status.Error.id)))
      }
    }

    "return Some((.traderDetails, Status.Value.Error))" when {
      "xPath.task returns TraderDetails" in {
        val xPath = s"/CC015C/Consignment/Consignor/name"
        XPath(xPath).sectionError shouldBe Some((".traderDetails", Status(Status.Error.id)))
      }
    }

    "return Some((.routeDetails, Status.Value.Error))" when {
      "xPath.task returns RouteDetails" in {
        val xPath = s"/CC015C/Consignment/PlaceOfLoading/country"
        XPath(xPath).sectionError shouldBe Some((".routeDetails", Status(Status.Error.id)))
      }
    }

    "return Some((.transportDetails, Status.Value.Error))" when {
      "xPath.task returns TransportDetails" in {
        val xPath = s"/CC015C/Authorisation[1]/referenceNumber"
        XPath(xPath).sectionError shouldBe Some((".transportDetails", Status(Status.Error.id)))
      }
    }

    "return Some((.documents, Status.Value.Error))" when {
      "xPath.task returns Documents" in {
        val xPath = s"/CC015C/Consignment/PreviousDocument[1]/type"
        XPath(xPath).sectionError shouldBe Some((".documents", Status(Status.Error.id)))
      }
    }

    "return Some((.items, Status.Value.Error))" when {
      "xPath.task returns Items" in {
        val xPath = s"/CC015C/Consignment/HouseConsignment[2]/ConsingmentItem[99]/additionalInformation"
        XPath(xPath).sectionError shouldBe Some((".items", Status(Status.Error.id)))
      }
    }
  }

  //  "sectionError" ignore {
  //
  //          "return a section paired with an error value" when {
  //            "xPath is appended with an index" in {
  //              forAll(Gen.alphaNumStr, arbitrary[Int]) {
  //                (subPath, index) =>
  //                  val xPath = s"/CC015C/HolderOfTheTransitProcedure[$index]/" + subPath
  //                  XPath(xPath).sectionError shouldBe Some((".traderDetails", Status(Status.Error.id)))
  //              }
  //            }
  //          }
  //
  //          "return Some((.traderDetails, Status.Value))" when {
  //
  //            val section = ".traderDetails"
  //            val error = Status(Status.Error.id)
  //            val expectedResult: Option[(String, Status.Value)] = Some((section, error))
  //
  //            "xPath is prepended with /CC015C/HolderOfTheTransitProcedure/" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/HolderOfTheTransitProcedure/" + subPath
  //                  XPath(xPath).sectionError shouldBe expectedResult
  //              }
  //            }
  //
  //            "xPath is prepended with /CC015C/Representative/" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/Representative/" + subPath
  //                  XPath(xPath).sectionError shouldBe expectedResult
  //              }
  //            }
  //
  //            "xPath is prepended with /CC015C/Consignment/" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/Consignment/" + subPath
  //                  XPath(xPath).sectionError shouldBe expectedResult
  //              }
  //            }
  //          }
  //
  //          "return Some((.routeDetails, Status.Value))" when {
  //
  //            val section = ".routeDetails"
  //            val error = Status(Status.Error.id)
  //            val expectedResult: Option[(String, Status.Value)] = Some((section, error))
  //
  //            "xPath is prepended with /CC015C/CustomsOfficeOfDestinationDeclared/" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/CustomsOfficeOfDestinationDeclared/" + subPath
  //                  XPath(xPath).sectionError shouldBe expectedResult
  //              }
  //            }
  //
  //            "xPath is prepended with /CC015C/CustomsOfficeOfTransitDeclared/" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/CustomsOfficeOfTransitDeclared/" + subPath
  //                  XPath(xPath).sectionError shouldBe expectedResult
  //              }
  //            }
  //
  //            "xPath is prepended with /CC015C/CustomsOfficeOfExitForTransitDeclared/" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/CustomsOfficeOfExitForTransitDeclared/" + subPath
  //                  XPath(xPath).sectionError shouldBe expectedResult
  //              }
  //            }
  //          }
  //
  //          "return Some((.transportDetails, Status.Value))" when {
  //
  //            val section = ".transportDetails"
  //            val error = Status(Status.Error.id)
  //            val expectedResult: Option[(String, Status.Value)] = Some((section, error))
  //
  //            "xPath is prepended with /CC015C/Authorisation/" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/Authorisation/" + subPath
  //                  XPath(xPath).sectionError shouldBe expectedResult
  //              }
  //            }
  //          }
  //
  //          "return Some((.guaranteeDetails, Status.Value))" when {
  //
  //            val section = ".guaranteeDetails"
  //            val error = Status(Status.Error.id)
  //            val expectedResult: Option[(String, Status.Value)] = Some((section, error))
  //
  //            "xPath is prepended with /CC015C/Guarantee/" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/Guarantee/" + subPath
  //                  XPath(xPath).sectionError shouldBe expectedResult
  //              }
  //            }
  //          }
  //
  //          "return Some((.preTaskList, Status.Value))" when {
  //
  //            val section = ".preTaskList"
  //            val error = Status(Status.Error.id)
  //            val expectedResult: Option[(String, Status.Value)] = Some((section, error))
  //
  //            "xPath is prepended with /CC015C/TransitOperation" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/TransitOperation/" + subPath
  //                  XPath(xPath).sectionError shouldBe expectedResult
  //              }
  //            }
  //            "xPath is prepended with /CC015C/CustomsOfficeOfDeparture" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/CustomsOfficeOfDeparture/" + subPath
  //                  XPath(xPath).sectionError shouldBe expectedResult
  //              }
  //            }
  //          }
  //
  //          "return None" when {
  //            "xPath is prepended with CC015C but doesn't have a recognised section" in {
  //              forAll(Gen.alphaNumStr) {
  //                subPath =>
  //                  val xPath = "/CC015C/" + subPath
  //                  XPath(xPath).sectionError shouldBe None
  //              }
  //            }
  //
  //            "xPath is anything else" in {
  //              forAll(Gen.alphaNumStr) {
  //                xPath =>
  //                  XPath(xPath).sectionError shouldBe None
  //              }
  //            }
  //          }
  //        }

}
