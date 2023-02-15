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

package submission

import api.submission.Consignment
import base.SpecBase
import generated._
import models.UserAnswers
import play.api.libs.json.{JsValue, Json}

class ConsignmentSpec extends SpecBase {

  "Consignment" when {

    "transform is called" must {

      "convert to API format" in {

        val json: JsValue = Json.parse(s"""
            |{
            |  "_id" : "$uuid",
            |  "lrn" : "$lrn",
            |  "eoriNumber" : "$eoriNumber",
            |  "data" : {
            |    "traderDetails" : {
            |      "consignment" : {
            |        "consignor" : {
            |          "eori" : "consignor1",
            |          "name" : "Mr Consignor",
            |          "country" : {
            |            "code" : "GB",
            |            "description" : "United Kingdom"
            |          },
            |          "address" : {
            |            "numberAndStreet" : "21 Test Lane",
            |            "city" : "Newcastle upon Tyne",
            |            "postalCode" : "NE1 1NE"
            |          },
            |          "contact" : {
            |            "name" : "Consignor Contact",
            |            "telephoneNumber" : "+44 101 157 0192"
            |          }
            |        },
            |        "consignee" : {
            |          "eori" : "consignee1",
            |          "name" : "Mr Consignee",
            |          "country" : {
            |            "code" : "FR",
            |            "description" : "France"
            |          },
            |          "address" : {
            |            "numberAndStreet" : "21 Test Rue",
            |            "city" : "Paris",
            |            "postalCode" : "PA1 1PA"
            |          }
            |        }
            |      }
            |    },
            |    "routeDetails" : {
            |      "routing" : {
            |        "countryOfDestination" : {
            |          "code" : "IT",
            |          "description" : "Italy"
            |        }
            |      },
            |      "loading" : {
            |        "unLocode" : "UNLOCODE1",
            |        "additionalInformation" : {
            |          "country" : "Loading country",
            |          "location" : "Loading location"
            |        }
            |      },
            |      "unloading" : {
            |        "unLocode" : "UNLOCODE2",
            |        "additionalInformation" : {
            |          "country" : "Unloading country",
            |          "location" : "Unloading location"
            |        }
            |      }
            |    },
            |    "transportDetails" : {
            |      "preRequisites" : {
            |        "uniqueConsignmentReference" : "ucr123",
            |        "countryOfDispatch" : {
            |          "code" : "FR",
            |          "description" : "France"
            |        },
            |        "containerIndicator" : true
            |      },
            |      "inlandMode" : "maritime",
            |      "borderModeOfTransport" : "rail",
            |      "carrierDetails" : {
            |        "identificationNumber" : "carrier1",
            |        "addContactYesNo" : true,
            |        "contact" : {
            |          "name" : "Carrier Contact",
            |          "telephoneNumber" : "+44 808 157 0192"
            |        }
            |      },
            |      "supplyChainActors" : [
            |        {
            |          "supplyChainActorType" : "consolidator",
            |          "identificationNumber" : "sca1"
            |        },
            |        {
            |          "supplyChainActorType" : "freightForwarder",
            |          "identificationNumber" : "sca2"
            |        },
            |        {
            |          "supplyChainActorType" : "manufacturer",
            |          "identificationNumber" : "sca3"
            |        },
            |        {
            |          "supplyChainActorType" : "warehouseKeeper",
            |          "identificationNumber" : "sca4"
            |        }
            |      ]
            |    }
            |  },
            |  "tasks" : {},
            |  "createdAt" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662393524188"
            |    }
            |  },
            |  "lastUpdated" : {
            |    "$$date" : {
            |      "$$numberLong" : "1662546803472"
            |    }
            |  }
            |}
            |""".stripMargin)

        val uA: UserAnswers = json.as[UserAnswers](UserAnswers.mongoFormat)

        val converted: ConsignmentType20 = Consignment.transform(uA)

        converted.countryOfDispatch shouldBe Some("FR")
        converted.countryOfDestination shouldBe Some("IT")
        converted.containerIndicator shouldBe Some(Number1)
        converted.inlandModeOfTransport shouldBe Some("1")
        converted.modeOfTransportAtTheBorder shouldBe Some("2")
        converted.grossMass shouldBe 0
        converted.referenceNumberUCR shouldBe Some("ucr123")

        converted.Carrier shouldBe Some(
          CarrierType04(
            identificationNumber = "carrier1",
            ContactPerson = Some(
              ContactPersonType05(
                name = "Carrier Contact",
                phoneNumber = "+44 808 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.Consignor shouldBe Some(
          ConsignorType07(
            identificationNumber = Some("consignor1"),
            name = Some("Mr Consignor"),
            Address = Some(
              AddressType17(
                streetAndNumber = "21 Test Lane",
                postcode = Some("NE1 1NE"),
                city = "Newcastle upon Tyne",
                country = "GB"
              )
            ),
            ContactPerson = Some(
              ContactPersonType05(
                name = "Consignor Contact",
                phoneNumber = "+44 101 157 0192",
                eMailAddress = None
              )
            )
          )
        )

        converted.Consignee shouldBe Some(
          ConsigneeType05(
            identificationNumber = Some("consignee1"),
            name = Some("Mr Consignee"),
            Address = Some(
              AddressType17(
                streetAndNumber = "21 Test Rue",
                postcode = Some("PA1 1PA"),
                city = "Paris",
                country = "FR"
              )
            )
          )
        )

        converted.AdditionalSupplyChainActor shouldBe Seq(
          AdditionalSupplyChainActorType(
            sequenceNumber = "0",
            role = "CS",
            identificationNumber = "sca1"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "1",
            role = "FW",
            identificationNumber = "sca2"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "2",
            role = "MF",
            identificationNumber = "sca3"
          ),
          AdditionalSupplyChainActorType(
            sequenceNumber = "3",
            role = "WH",
            identificationNumber = "sca4"
          )
        )

        converted.PlaceOfLoading shouldBe Some(
          PlaceOfLoadingType03(
            Some("UNLOCODE1"),
            Some("Loading country"),
            Some("Loading location")
          )
        )

        converted.PlaceOfUnloading shouldBe Some(
          PlaceOfUnloadingType01(
            Some("UNLOCODE2"),
            Some("Unloading country"),
            Some("Unloading location")
          )
        )
      }
    }
  }
}
