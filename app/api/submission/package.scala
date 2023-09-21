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

import generated.{Flag, Number0, Number1}
import play.api.libs.json._
import scalaxb.XMLCalendar

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import javax.xml.datatype.XMLGregorianCalendar
import scala.language.implicitConversions

package object submission {

  lazy val preTaskListPath: JsPath = __ \ "preTaskList"

  lazy val traderDetailsPath: JsPath = __ \ "traderDetails"
  lazy val consignmentPath: JsPath   = traderDetailsPath \ "consignment"

  lazy val routeDetailsPath: JsPath = __ \ "routeDetails"

  lazy val transportDetailsPath: JsPath     = __ \ "transportDetails"
  lazy val preRequisitesPath: JsPath        = transportDetailsPath \ "preRequisites"
  lazy val authorisationsPath: JsPath       = transportDetailsPath \ "authorisationsAndLimit" \ "authorisations"
  lazy val equipmentsAndChargesPath: JsPath = transportDetailsPath \ "equipmentsAndCharges"
  lazy val equipmentsPath: JsPath           = equipmentsAndChargesPath \ "equipments"

  lazy val guaranteesPath: JsPath = __ \ "guaranteeDetails"

  lazy val documentsPath: JsPath = __ \ "documents" \ "documents"

  lazy val itemsPath: JsPath         = __ \ "items"
  lazy val itemConsigneePath: JsPath = __ \ "consignee"

  lazy val reducedDatasetIndicatorReads: Reads[Boolean] =
    (consignmentPath \ "approvedOperator").readWithDefault[Boolean](false)

  implicit class RichJsPath(path: JsPath) {

    def readArray[T](implicit reads: Int => Reads[T]): Reads[Seq[T]] =
      path.readWithDefault[Seq[T]](Nil) {
        case value: JsArray => JsSuccess(value.readValuesAs[T])
        case _              => throw new Exception(s"$path did not contain an array")
      }

    def readFilteredArray[T](f: JsValue => Boolean)(implicit reads: Int => Reads[T]): Reads[Seq[T]] =
      path.readWithDefault[Seq[T]](Nil) {
        case value: JsArray => JsSuccess(value.readFilteredValuesAs[T](f))
        case _              => throw new Exception(s"$path did not contain an array")
      }

    def readCommonValuesInNestedArrays[T](subPath: String)(implicit reads: Int => Reads[T]): Reads[Seq[T]] =
      path.readWithDefault[Seq[T]](Nil) {
        case JsArray(values) =>
          JsSuccess {
            val nestedValues = values.foldLeft[Seq[Seq[JsValue]]](Nil) {
              (acc, value) =>
                value.transform((__ \ subPath).json.pick) match {
                  case JsSuccess(JsArray(values), _) => acc :+ values.toSeq
                  case _                             => acc
                }
            }

            val commonValues = if (nestedValues.nonEmpty) {
              nestedValues.reduceLeft(_ intersect _)
            } else {
              Nil
            }

            commonValues.readValuesAs[T]
          }
        case _ => throw new Exception(s"$path did not contain an array")
      }
  }

  implicit class RichJsArray(arr: JsArray) {

    def values: Seq[JsValue] = arr.value.toSeq

    def zipWithIndex: Seq[(JsValue, Int)] = values.zipWithIndex

    def readValuesAs[T](implicit reads: Int => Reads[T]): Seq[T] =
      values.readValuesAs

    def readFilteredValuesAs[T](f: JsValue => Boolean)(implicit reads: Int => Reads[T]): Seq[T] =
      values.readFilteredValuesAs(f)

    def mapWithSequenceNumber[T](f: (JsValue, Int) => T): Seq[T] =
      values.mapWithSequenceNumber(f)
  }

  implicit class RichJsValues(values: Seq[JsValue]) {

    def mapWithSequenceNumber[T](f: (JsValue, Int) => T): Seq[T] =
      values.zipWithSequenceNumber.map {
        case (value, index) => f(value, index)
      }

    def readValuesAs[T](implicit reads: Int => Reads[T]): Seq[T] =
      readFilteredValuesAs {
        _ => true
      }

    def readFilteredValuesAs[T](f: JsValue => Boolean)(implicit reads: Int => Reads[T]): Seq[T] =
      values.filter(f).mapWithSequenceNumber {
        case (value, index) => value.as[T](reads(index))
      }
  }

  implicit class RichSeq[T](seq: Seq[T]) {

    def zipWithSequenceNumber: Seq[(T, Int)] = seq.zipWithIndex.map {
      case (value, i) => (value, i + 1)
    }
  }

  implicit class RichIterable[A](iterable: Iterable[A]) {

    def groupByPreserveOrder[K](f: A => K): Seq[(K, Iterable[A])] = {
      val keys   = iterable.map(f).toSeq.distinct
      val groups = iterable.groupBy(f)
      keys.map {
        key => key -> groups(key)
      }
    }
  }

  implicit class RichOptionalJsArray(arr: Option[JsArray]) {

    def readValuesAs[T](implicit reads: Int => Reads[T]): Seq[T] =
      arr.map(_.readValuesAs[T]).getOrElse(Nil)
  }

  implicit class RichOptionalJsObject(obj: Option[JsObject]) {

    def readValueAs[T](implicit reads: Reads[T]): Option[T] =
      obj.map(_.as[T])
  }

  implicit def boolToFlag(x: Option[Boolean]): Option[Flag] =
    x.map(boolToFlag)

  implicit def boolToFlag(x: Boolean): Flag =
    if (x) Number1 else Number0

  implicit def localDateToXMLGregorianCalendar(date: Option[LocalDate]): Option[XMLGregorianCalendar] =
    date.map(localDateToXMLGregorianCalendar)

  implicit def localDateToXMLGregorianCalendar(date: LocalDate): XMLGregorianCalendar =
    stringToXMLGregorianCalendar(date.toString)

  implicit def stringToXMLGregorianCalendar(date: Option[String]): Option[XMLGregorianCalendar] =
    date.map(stringToXMLGregorianCalendar)

  implicit def stringToXMLGregorianCalendar(date: String): XMLGregorianCalendar =
    XMLCalendar(date.replace("Z", ""))

  implicit def localDateTimeToXMLGregorianCalendar(localDateTime: LocalDateTime): XMLGregorianCalendar = {
    val formatterNoMillis: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    localDateTime.format(formatterNoMillis)
  }

  implicit def successfulReads[T](value: T): Reads[T] = Reads {
    _ => JsSuccess(value)
  }

}
