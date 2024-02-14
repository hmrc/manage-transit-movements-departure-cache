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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import models.UserAnswers
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import services.MessageIdentificationService

class HeaderSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks {

  private lazy val mockMessageIdentificationService = mock[MessageIdentificationService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[MessageIdentificationService].toInstance(mockMessageIdentificationService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMessageIdentificationService)
  }

  private val header: Header = app.injector.instanceOf[Header]

  "Conversions" when {

    "message is called" must {

      val json: JsValue = Json.parse(s"""
          |{
          |  "_id" : "$uuid",
          |  "lrn" : "$lrn",
          |  "eoriNumber" : "$eoriNumber",
          |  "isSubmitted" : "notSubmitted",
          |  "data" : {
          |    "preTaskList" : {
          |      "officeOfDeparture" : {
          |        "id" : "GB000011",
          |        "name" : "Birmingham Airport",
          |        "phoneNumber" : "+44 (0)121 781 7850"
          |      }
          |    }
          |  },
          |  "tasks" : {},
          |  "createdAt" : "2022-09-05T15:58:44.188Z",
          |  "lastUpdated" : "2022-09-07T10:33:23.472Z"
          |}
          |""".stripMargin)

      val uA: UserAnswers = json.as[UserAnswers]

      "convert to API format" when {
        "declaration (IE015)" in {
          forAll(Gen.alphaNumStr) {
            messageIdentification =>
              when(mockMessageIdentificationService.randomIdentifier).thenReturn(messageIdentification)

              val result = header.message(uA, CC015C)

              result.messageSender shouldBe uA.eoriNumber
              result.messagE_1Sequence2.messageRecipient shouldBe "NTA.GB"
              result.messagE_1Sequence2.messageIdentification shouldBe messageIdentification
              result.messagE_TYPESequence3.messageType shouldBe CC015C
              result.correlatioN_IDENTIFIERSequence4.correlationIdentifier shouldBe None
          }
        }

        "amendment (IE013)" in {
          forAll(Gen.alphaNumStr) {
            messageIdentification =>
              when(mockMessageIdentificationService.randomIdentifier).thenReturn(messageIdentification)

              val result = header.message(uA, CC013C)

              result.messageSender shouldBe uA.eoriNumber
              result.messagE_1Sequence2.messageRecipient shouldBe "NTA.GB"
              result.messagE_1Sequence2.messageIdentification shouldBe messageIdentification
              result.messagE_TYPESequence3.messageType shouldBe CC013C
              result.correlatioN_IDENTIFIERSequence4.correlationIdentifier shouldBe None
          }
        }
      }
    }
  }
}
