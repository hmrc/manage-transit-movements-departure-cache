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

import generated.AuthorisationType03
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

object Authorisations {

  def transform(uA: UserAnswers): Seq[AuthorisationType03] =
    uA.data.as[Seq[AuthorisationType03]](authorisationsReads)

  implicit val authorisationsReads: Reads[Seq[AuthorisationType03]] = for {
    procedureType           <- (preTaskListPath \ "procedureType").read[String]
    reducedDatasetIndicator <- reducedDatasetIndicatorReads
    inlandMode              <- inlandModeReads
    reads                   <- authorisationsPath.readArray[AuthorisationType03](authorisationType03.reads(_, procedureType, reducedDatasetIndicator, inlandMode))
  } yield reads
}

object authorisationType03 {

  def reads(index: Int, procedureType: String, reducedDatasetIndicator: Boolean, inlandMode: String): Reads[AuthorisationType03] = (
    (index.toString: Reads[String]) and
      __.read[String](authorisationTypeReads(index, procedureType, reducedDatasetIndicator, inlandMode)).map(convertTypeValue) and
      (__ \ "authorisationReferenceNumber").read[String]
  )(AuthorisationType03.apply _)

  private def authorisationTypeReads(index: Int, procedureType: String, reducedDatasetIndicator: Boolean, inlandMode: String): Reads[String] =
    (index, procedureType, reducedDatasetIndicator, inlandMode) match {
      case (1, _, true, "maritime" | "rail" | "air") => "TRD"
      case (1, "simplified", true, _)                => "ACR"
      case _                                         => (__ \ "authorisationType").read[String]
    }

  private lazy val convertTypeValue: String => String = {
    case "ACR" => "C521"
    case "SSE" => "C523"
    case "TRD" => "C524"
    case _     => throw new Exception("Invalid authorisation type value")
  }
}
