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

import models.Sort.Order
import models.Sort.Order.{Ascending, Descending}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Indexes.{ascending, descending}

sealed trait Sort {
  self: Order =>
  val field: String
  val orderBy: String
  val convertParams: String
  val toBson: Bson

  override def toString: String = s"$field.$orderBy"
}

object Sort {

  sealed trait Order {
    val orderBy: String
  }

  object Order {

    sealed trait Ascending extends Order {
      override val orderBy: String = "asc"
    }

    sealed trait Descending extends Order {
      override val orderBy: String = "dsc"
    }
  }

  case object SortByLRNAsc extends Sort with Ascending {
    val field: String = "lrn"
    val convertParams = s"$field.$orderBy"
    val toBson: Bson  = ascending(field)
  }

  case object SortByLRNDesc extends Sort with Descending {
    val field: String = "lrn"
    val convertParams = s"$field.$orderBy"
    val toBson: Bson  = descending(field)
  }

  case object SortByCreatedAtAsc extends Sort with Ascending {
    val field: String = "createdAt"
    val convertParams = s"$field.$orderBy"
    val toBson: Bson  = ascending(field)
  }

  case object SortByCreatedAtDesc extends Sort with Descending {
    val field: String = "createdAt"
    val convertParams = s"$field.$orderBy"
    val toBson: Bson  = descending(field)
  }

  def apply(sortParams: Option[String]): Sort = sortParams match {
    case Some(SortByLRNAsc.convertParams)       => SortByLRNAsc
    case Some(SortByLRNDesc.convertParams)      => SortByLRNDesc
    case Some(SortByCreatedAtAsc.convertParams) => SortByCreatedAtAsc
    case _                                      => SortByCreatedAtDesc
  }

//  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[Sort] = new QueryStringBindable[Sort] {
//
//    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Sort]] =
//      Option(stringBinder.bind("sortBy", params) match {
//        case Some(Right(SortByLRNAsc.convertParams))        => Right(SortByLRNAsc)
//        case Some(Right(SortByLRNDesc.convertParams))       => Right(SortByLRNDesc)
//        case Some(Right(SortByCreatedAtAsc.convertParams))  => Right(SortByCreatedAtAsc)
//        case Some(Right(SortByCreatedAtDesc.convertParams)) => Right(SortByCreatedAtDesc)
//        case _                                              => Left("Invalid sort parameters")
//      })
//
//    override def unbind(key: String, value: Sort): String = stringBinder.unbind("sortBy", value.convertParams)
//
//  }
}
