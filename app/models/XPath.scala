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

import play.api.libs.json.{__, Reads}

case class Section(errorPath: String, relatedJourney: String) {
  override def toString: String = this.errorPath
}

case class XPath(value: String) {

  private val sections = Seq(
    Section("TransitOperation", ".preTaskList"),
    Section("Authorisation", ".transportDetails"),
    Section("CustomsOfficeOfDeparture", ".preTaskList"),
    Section("CustomsOfficeOfDestinationDeclared", ".routeDetails"),
    Section("CustomsOfficeOfTransitDeclared", ".routeDetails"),
    Section("CustomsOfficeOfExitForTransitDeclared", ".routeDetails"),
    Section("HolderOfTheTransitProcedure", ".traderDetails"),
    Section("Representative", ".traderDetails"),
    Section("Guarantee", ".guaranteeDetails"),
    Section("Consignment", ".traderDetails")
  )

  def isAmendable: Boolean = {
    val regex = "^/CC015C/(.+)$".r
    value match {
      case regex(section) if sections.map(_.errorPath).exists(section.startsWith) => true
      case _                                                                      => false
    }
  }

  def sectionError: Option[(String, Status.Value)] = {
    val regex = """/CC015C/(?<middle>[^/]+)/.*""".r
    value match {
      case regex(section) =>
        sections.find(
          x => x.errorPath == section
        ) match {
          case Some(section) => Some(section.relatedJourney, Status(Status.Error.id))
          case None          => None
        }
      case _ => None
    }
  }

}

object XPath {

  implicit val reads: Reads[XPath] = __.read[String].map(XPath(_))
}
