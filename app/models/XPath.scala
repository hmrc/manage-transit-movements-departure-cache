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

import models.JourneyTask._
import play.api.libs.json.{__, Reads}

import scala.util.Try
import scala.util.matching.Regex

case class XPath(value: String) {

  private val sections = Seq(
    "TransitOperation",
    "Authorisation",
    "CustomsOfficeOfDeparture",
    "CustomsOfficeOfDestinationDeclared",
    "CustomsOfficeOfTransitDeclared",
    "CustomsOfficeOfExitForTransitDeclared",
    "HolderOfTheTransitProcedure",
    "Representative",
    "Guarantee",
    "Consignment"
  )

  val preTaskList: JourneyTask = TraderDetails

  def isAmendable: Boolean = {
    val regex = "^/CC015C/(.+)$".r
    value match {
      case regex(section) if sections.exists(section.startsWith) => true
      case _                                                     => false
    }
  }

  def sectionError: Option[(String, Status.Value)] =
    this.task match {
      case Some(task) => Some((task.taskName, Status(Status.Error.id)))
      case _          => None
    }

  def task: Option[JourneyTask] = {
    val pf: PartialFunction[String, JourneyTask] = _.replace("/CC015C/", "") match {
      case x if x.matches("^TransitOperation/declarationType$")          => PreTaskList
      case x if x.matches("^TransitOperation/tirCarnetReference$")       => PreTaskList
      case x if x.matches("^TransitOperation/securityDetailsType$")      => PreTaskList
      case x if x.matches("^TransitOperation/routing$")                  => RouteDetails
      case x if x.matches("^Authorisation(.+)$")                         => TransportDetails
      case x if x.matches("^CustomsOfficeOfDeparture(.+)$")              => PreTaskList
      case x if x.matches("^CustomsOfficeOfDestinationDeclared(.+)$")    => RouteDetails
      case x if x.matches("^CustomsOfficeOfTransitDeclared(.+)$")        => RouteDetails
      case x if x.matches("^CustomsOfficeOfExitForTransitDeclared(.+)$") => RouteDetails
      case x if x.matches("^HolderOfTheTransitProcedure(.+)$")           => TraderDetails
      case x if x.matches("^Representative(.+)$")                        => TraderDetails
      case x if x.matches("^Guarantee(.+)$")                             => GuaranteeDetails
    }

    Try(pf.apply(value)).toOption
  }

}

object XPath {

  implicit val reads: Reads[XPath] = __.read[String].map(XPath(_))
}
