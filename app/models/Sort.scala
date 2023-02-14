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
import play.api.mvc.{JavascriptLiteral, PathBindable, QueryStringBindable}

trait Sort {
  val field: String
  val orderBy: String

  val toBSON: Bson = orderBy match {
    case "asc"  => sort(ascending(field))
    case "desc" => sort(descending(field))
  }
  val convertParams: String = s"$field.$orderBy"
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

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[Sort] = new QueryStringBindable[Sort] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Sort]] = stringBinder.bind("sortBy", params) map {
      case Right(SortByLRNAsc.convertParams)        => Right(SortByLRNAsc)
      case Right(SortByLRNDesc.convertParams)       => Right(SortByLRNDesc)
      case Right(SortByCreatedAtAsc.convertParams)  => Right(SortByCreatedAtAsc)
      case Right(SortByCreatedAtDesc.convertParams) => Right(SortByCreatedAtDesc)
      case _                                        => Left("Invalid sort parameters")
    }

    override def unbind(key: String, value: Sort): String = stringBinder.unbind("sortBy", value.convertParams)
  }
}
