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

import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Aggregates.sort
import org.mongodb.scala.model.Indexes.{ascending, descending}
import play.api.mvc.{JavascriptLiteral, PathBindable}

trait Sort {
  val field: String
  val orderBy: String

  val toBSON: Bson = orderBy match {
    case "asc"  => sort(ascending(field))
    case "desc" => sort(descending(field))
  }
  override def toString: String = s"$field.$orderBy"
}

object SortByLRNAsc extends Sort {
  val field: String   = "lrn"
  val orderBy: String = "asc"
}

object SortByLRNDesc extends Sort {
  val field: String   = "lrn"
  val orderBy: String = "desc"
}

object SortByCreatedAtAsc extends Sort {
  val field: String   = "createdAt"
  val orderBy: String = "asc"
}

object SortByCreatedAtDesc extends Sort {
  val field: String   = "createdAt"
  val orderBy: String = "desc"
}

object Sort {

  implicit def pathBindable: PathBindable[Sort] = new PathBindable[Sort] {

    override def bind(key: String, value: String): Either[String, Sort] = value match {
      case SortByLRNAsc.toString       => Right(SortByLRNAsc)
      case SortByLRNDesc.toString      => Right(SortByLRNDesc)
      case SortByCreatedAtAsc.toString => Right(SortByCreatedAtAsc)
      case _                           => Right(SortByCreatedAtDesc)
    }

    override def unbind(key: String, value: Sort): String = s"$key=${value.toString}"
  }

}
