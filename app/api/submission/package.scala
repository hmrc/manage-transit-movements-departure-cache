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

import play.api.libs.json.{JsArray, JsValue, Reads}

package object submission {

  implicit class RichJsArray(arr: JsArray) {

    def zipWithIndex: List[(JsValue, Int)] = arr.value.toList.zipWithIndex
  }

  implicit class RichOptionalJsArray(arr: Option[JsArray]) {

    def readValuesAs[T](implicit reads: Int => Reads[T]): Seq[T] =
      arr.mapWithIndex {
        case (value, index) => value.as[T](reads(index))
      }

    def mapWithIndex[T](f: (JsValue, Int) => T): Seq[T] =
      arr
        .map {
          _.zipWithIndex.map {
            case (value, i) => f(value, i)
          }
        }
        .getOrElse(Nil)
  }

}
