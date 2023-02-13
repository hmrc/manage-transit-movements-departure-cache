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
import gettables.sections.AuthorisationsSection
import models.UserAnswers
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

object Authorisations {

  def transform(uA: UserAnswers): Seq[AuthorisationType03] =
    uA.get(AuthorisationsSection)
      .map {
        _.value.zipWithIndex.map {
          case (value, i) => value.as[AuthorisationType03](AuthorisationType03.reads(i))
        }
      }
      .getOrElse(Seq.empty)
      .toSeq
}

object AuthorisationType03 {

  def apply(
    typeValue: String,
    referenceNumber: String
  )(
    sequenceNumber: String
  ): AuthorisationType03 = new AuthorisationType03(sequenceNumber, typeValue, referenceNumber)

  def reads(index: Int): Reads[AuthorisationType03] = (
    (__ \ "authorisationType").read[String] and
      (__ \ "authorisationReferenceNumber").read[String]
  ).tupled.map((AuthorisationType03.apply _).tupled).map(_(index.toString))
}
