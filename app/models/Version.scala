/*
 * Copyright 2025 HM Revenue & Customs
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

import generated.*

import scala.util.{Failure, Success, Try}

sealed trait Version {
  val id: PhaseIDtype
}

object Version {

  case object Phase5 extends Version {
    override val id: PhaseIDtype = NCTS5u461
  }

  case object Phase6 extends Version {
    override val id: PhaseIDtype = NCTS6
  }

  def apply(value: Option[String]): Try[Version] =
    value match {
      case Some("2.0")        => Success(Phase6)
      case Some("1.0") | None => Success(Phase5)
      case Some(x)            => Failure(new Exception(s"$x is not a valid version"))
    }
}
