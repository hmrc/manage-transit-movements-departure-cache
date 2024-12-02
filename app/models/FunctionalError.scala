/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import scala.annotation.tailrec

case class FunctionalError(
  errorPointer: XPath,
  errorCode: String,
  errorReason: String,
  originalAttributeValue: Option[String]
) {

  def section: Option[String] = errorPointer.task.map(_.toString)

  def invalidDataItem: String = {
    @tailrec
    def rec(path: List[String], acc: String = ""): String = path match {
      case Nil                                                        => acc
      case "" :: tail                                                 => rec(tail, acc)
      case "CC015C" :: tail                                           => rec(tail, acc)
      case "Consignment" :: tail                                      => rec(tail, acc)
      case head :: tail if head.matches("""HouseConsignment\[\d*]""") => rec(tail, acc)
      case head :: tail =>
        val indexedPattern = "(.*)\\[(\\d*)]".r
        head match {
          case indexedPattern(path, index) => rec(tail, combine(acc, separate(s"$path $index:")))
          case _                           => rec(tail, combine(acc, separate(if (tail.isEmpty) head else s"$head:")))
        }
    }

    def separate(str: String): String = {
      @tailrec
      def rec(chars: List[Char], acc: String = ""): String = chars match {
        case Nil =>
          acc
        case head :: tail if acc.isEmpty =>
          rec(tail, head.toUpper.toString)
        case head :: next :: tail if head.isUpper && next.isUpper =>
          val f: Char => Boolean = _.isUpper
          rec(tail.dropWhile(f), acc + " " + head + next + tail.takeWhile(f).mkString)
        case head :: tail if head.isUpper =>
          rec(tail, acc + " " + head.toLower)
        case head :: tail =>
          rec(tail, acc + head)
      }
      rec(str.toList)
    }

    def combine(str1: String, str2: String): String =
      if (str1.isEmpty) str2 else s"$str1 $str2"

    rec(errorPointer.value.split('/').toList).trim
  }
}

object FunctionalError {

  implicit val reads: Reads[FunctionalError] = Json.reads[FunctionalError]

  implicit val writes: Writes[FunctionalError] = (
    (__ \ "error").write[String] and
      (__ \ "businessRuleId").write[String] and
      (__ \ "section").writeNullable[String] and
      (__ \ "invalidDataItem").write[String] and
      (__ \ "invalidAnswer").writeNullable[String]
  )(
    functionalError =>
      (functionalError.errorCode, functionalError.errorReason, functionalError.section, functionalError.invalidDataItem, functionalError.originalAttributeValue)
  )
}
