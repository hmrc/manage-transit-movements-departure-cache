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

sealed trait Phase {
  val version: String
}

object Phase {

  case object Transition extends Phase {
    override val version: String = "2.0"
  }

  case object PostTransition extends Phase {
    override val version: String = "2.1"
  }

  def apply(header: String): Option[Phase] = header match {
    case Transition.version     => Some(Transition)
    case PostTransition.version => Some(PostTransition)
    case _                      => None
  }
}
