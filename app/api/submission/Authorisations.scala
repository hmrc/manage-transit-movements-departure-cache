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
    uA.metadata.data.as[Seq[AuthorisationType03]](authorisationsReads)

  implicit val authorisationsReads: Reads[Seq[AuthorisationType03]] =
    authorisationsPath.readArray[AuthorisationType03](authorisationType03.reads)
}

object authorisationType03 {

  def reads(index: Int): Reads[AuthorisationType03] = {
    lazy val authorisationTypeReads: Reads[String] =
      (__ \ "authorisationType" \ "code").read[String] orElse (__ \ "inferredAuthorisationType" \ "code").read[String]

    (
      Reads.pure[BigInt](index) and
        authorisationTypeReads and
        (__ \ "authorisationReferenceNumber").read[String]
    )(AuthorisationType03.apply)
  }
}
