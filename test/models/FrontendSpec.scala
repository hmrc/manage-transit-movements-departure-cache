/*
 * Copyright 2022 HM Revenue & Customs
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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.PathBindable

class FrontendSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val pathBindable = implicitly[PathBindable[Frontend]]

  "a Frontend object" should {

    "bind from a url when valid value provided" in {
      forAll(arbitrary[Frontend]) {
        frontend =>
          val result: Either[String, Frontend] = pathBindable.bind("frontend", frontend.binder)
          result.value shouldBe frontend
      }
    }

    "not bind from a url when invalid value provided" in {
      val result: Either[String, Frontend] = pathBindable.bind("frontend", "foo")
      result.isLeft shouldBe true
    }

    "unbind" in {
      forAll(arbitrary[Frontend]) {
        frontend =>
          val result: String = pathBindable.unbind("frontend", frontend)
          result shouldBe frontend.binder
      }
    }
  }
}
