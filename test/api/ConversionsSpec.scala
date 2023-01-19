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

package api

import Gettables._
import base.SpecBase
import generated.TransitOperationType06
import models.UserAnswers
import play.api.libs.json.{JsObject, Json}
import scalaxb._

import scala.xml.{NodeSeq, XML}

class ConversionsSpec extends SpecBase {

  "transitOperationType" when {

    "can be parsed from PreTaskListDomain" in {

      val testData = Json
        .parse("""{
                                  |  "preTaskList": {
                                  |    "officeOfDeparture": {
                                  |      "id": "D8bZSS9wQhMooUn",
                                  |      "name": "1JQ3YrJ2aHra6DgGoyniBNYy"
                                  |    },
                                  |    "procedureType": "simplified",
                                  |    "declarationType": "T2",
                                  |    "securityDetailsType": "entrySummaryDeclaration",
                                  |    "detailsConfirmed": true
                                  |  },
                                  |  "traderDetails": {
                                  |    "holderOfTransit": {
                                  |      "eoriYesNo": false,
                                  |      "name": "8o4iwz5UzgpOYRHu",
                                  |      "country": {
                                  |        "code": "QX",
                                  |        "description": "ZXFioiNknvWLKWkhsc5f3vJV"
                                  |      },
                                  |      "address": {
                                  |        "numberAndStreet": "KDMxpWcrhYmMXQriyWubJXrRWh39pvTrXFWbbzOe2CbxvImgGYAEaC",
                                  |        "city": "ivb4KG8TrybpBSDwccSg2t0PE9JJ",
                                  |        "postalCode": "EjXN8YNQt8d8G"
                                  |      },
                                  |      "addContact": false
                                  |    },
                                  |    "actingAsRepresentative": true,
                                  |    "representative": {
                                  |      "eori": "CW4yfKpDGQaa1eNEzHtUdFiTHjmILJQMxnt0d7B8COJluFfWzc2VidYMbZFB0NBAycflFVczhXWZZ3u7DUwM",
                                  |      "name": "dJkEu1cmAo0bmu1WXPS",
                                  |      "capacity": "direct",
                                  |      "telephoneNumber": "m0dNF3NL7Rxee1zjo60nugCXReaJIQQ4OR0f"
                                  |    },
                                  |    "consignment": {
                                  |      "approvedOperator": false,
                                  |      "consignor": {
                                  |        "eoriYesNo": true,
                                  |        "eori": "bdf6J3goc2jLsY8",
                                  |        "name": "AvfatXKWnRBA7A0xkSGmBRhSnSYKbFKa9kPNpkBII7MfiB5LBCRKVhneNr6GihUxs",
                                  |        "country": {
                                  |          "code": "XZ",
                                  |          "description": "cCadhO0n3NVuqKSwHqBsRlYAOWUAcqRoO2"
                                  |        },
                                  |        "address": {
                                  |          "numberAndStreet": "3LaSpKvnZmvcMwDtb7EHR6emoX0oZ2bo5V1ZfnfgdH2",
                                  |          "city": "6O",
                                  |          "postalCode": "fmV7pGlo"
                                  |        },
                                  |        "addContact": false
                                  |      },
                                  |      "moreThanOneConsignee": true
                                  |    }
                                  |  },
                                  |  "routeDetails": {
                                  |    "routing": {
                                  |      "countryOfDestination": {
                                  |        "code": "WK",
                                  |        "description": "fyYQn4b"
                                  |      },
                                  |      "officeOfDestination": {
                                  |        "id": "ShmyNcCwLxyrUpR142W9IFbE",
                                  |        "name": "yADhpUzrsUWQlDuZk1QgDVbdzFIj",
                                  |        "phoneNumber": "19ZbweMvGUNREGkkuaQVuyjT2K4H7l77U3Ic9QRBCMozflk6iDUvF2S8SXe"
                                  |      },
                                  |      "bindingItinerary": false,
                                  |      "countriesOfRouting": [
                                  |        {
                                  |          "countryOfRouting": {
                                  |            "code": "WZ",
                                  |            "description": "hYdZmDI"
                                  |          }
                                  |        }
                                  |      ]
                                  |    },
                                  |    "transit": {
                                  |      "officesOfTransit": [
                                  |        {
                                  |          "officeOfTransitCountry": {
                                  |            "code": "ZX",
                                  |            "description": "gpTxnHMrWO"
                                  |          },
                                  |          "officeOfTransit": {
                                  |            "id": "fEx2zFF5dBa3Qxp",
                                  |            "name": "v0Uv1mBQ1t59qif0rfYESOfjqywr",
                                  |            "phoneNumber": "NPXjplIU3AoQyaKbBB404Sgl0jgupqxctScg6"
                                  |          },
                                  |          "addOfficeOfTransitETAYesNo": false
                                  |        }
                                  |      ]
                                  |    },
                                  |    "locationOfGoods": {
                                  |      "typeOfLocation": "approvedPlace",
                                  |      "qualifierOfIdentification": "postalCode",
                                  |      "identifier": {
                                  |        "postalCode": {
                                  |          "streetNumber": "17zQcCxLRqZix23xT",
                                  |          "postalCode": "J78MqcN0NIMfn7",
                                  |          "country": {
                                  |            "code": "VN",
                                  |            "description": "193VDAbT5tsU1Y6Wb"
                                  |          }
                                  |        },
                                  |        "addContact": false
                                  |      }
                                  |    },
                                  |    "loading": {
                                  |      "addUnLocodeYesNo": true,
                                  |      "unLocode": {
                                  |        "unLocodeExtendedCode": "tSzEpyqrEmDQIizIQaTpnd",
                                  |        "name": "k1UZC"
                                  |      },
                                  |      "addLocationYesNo": true,
                                  |      "additionalInformation": {
                                  |        "country": {
                                  |          "code": "WY",
                                  |          "description": "xZVNadnJW34k0pHiCmMf0avxinds"
                                  |        },
                                  |        "location": "TtxJHvnhf8O2Sg9GojkGSFmmHRhL4yEtGFlA0h6pm4QsBXhLnew01K5vgDCW9Br9XWvq5PeG"
                                  |      }
                                  |    },
                                  |    "unloading": {
                                  |      "addUnLocodeYesNo": true,
                                  |      "unLocode": {
                                  |        "unLocodeExtendedCode": "aWvoz3oV3",
                                  |        "name": "G7VPBQclcNgVd11HQ3775"
                                  |      },
                                  |      "addExtraInformationYesNo": false
                                  |    }
                                  |  }
                                  |}""".stripMargin)
        .as[JsObject]
      val uA: UserAnswers = emptyUserAnswers.copy(data = testData)

      val expected: NodeSeq = XML.loadString(
        """<TransitOperation""" +
          """ xmlns:tns="http://ncts.dgtaxud.ec" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">""" +
          s"""<LRN>${uA.lrn}</LRN>""" +
          s"""<declarationType>${uA.get(DeclarationTypeGettable).get}</declarationType>""" +
          """<additionalDeclarationType>A</additionalDeclarationType>""" +
          s"""<security>${uA.get(SecurityDetailsTypeGettable).get}</security>""" +
          s"""<reducedDatasetIndicator>${uA.get(ApprovedOperatorGettable).get match {
            case true => 1
            case _    => 0
          }}</reducedDatasetIndicator>""" +
          """<bindingItinerary>1</bindingItinerary>""" +
          """</TransitOperation>""".stripMargin
      )

      val tryConv: Either[String, TransitOperationType06] = Conversions.transitOperation(uA)

      val xml: NodeSeq = tryConv match {
        case Left(value)  => throw new Error(value)
        case Right(value) => toXML[TransitOperationType06](value, "TransitOperation", generated.defaultScope)
      }

      xml shouldBe expected

    }
  }

}
