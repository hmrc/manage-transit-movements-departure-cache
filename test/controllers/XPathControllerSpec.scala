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

package controllers

import base.SpecBase
import generators.Generators
import models.XPath
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsBoolean, JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.XPathService

import scala.concurrent.Future

class XPathControllerSpec extends SpecBase with Generators {

  private lazy val mockXPathService = mock[XPathService]

  private val xPaths = arbitrary[Seq[XPath]].sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockXPathService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[XPathService].toInstance(mockXPathService))

  "isDeclarationAmendable" should {

    "return 200 with true" when {
      "declaration is amendable" in {
        when(mockXPathService.isDeclarationAmendable(any(), any(), any())).thenReturn(Future.successful(true))

        val request = FakeRequest(POST, routes.XPathController.isDeclarationAmendable(lrn).url)
          .withBody(JsArray(xPaths.map(_.value).map(JsString)))

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(true)
        verify(mockXPathService).isDeclarationAmendable(eqTo(lrn), eqTo(eoriNumber), eqTo(xPaths))
      }
    }

    "return 200 with false" when {
      "declaration is not amendable" in {
        when(mockXPathService.isDeclarationAmendable(any(), any(), any())).thenReturn(Future.successful(false))

        val request = FakeRequest(POST, routes.XPathController.isDeclarationAmendable(lrn).url)
          .withBody(JsArray(xPaths.map(_.value).map(JsString)))

        val result = route(app, request).value

        status(result) shouldBe OK
        contentAsJson(result) shouldBe JsBoolean(false)
        verify(mockXPathService).isDeclarationAmendable(eqTo(lrn), eqTo(eoriNumber), eqTo(xPaths))
      }
    }

    "return 400" when {
      "request body is not an array of xpaths" in {
        val request = FakeRequest(POST, routes.XPathController.isDeclarationAmendable(lrn).url)
          .withBody(Json.obj("foo" -> "bar"))

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        verify(mockXPathService, never()).isDeclarationAmendable(any(), any(), any())
      }
    }
  }

}
