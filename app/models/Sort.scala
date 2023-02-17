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

import models.Sort.Field._
import models.Sort.Order._
import models.Sort.{Field, Order}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Indexes._

sealed trait Sort {
  val field: Field
  val order: Order
  lazy val convertParams: String = this.toString
  def toBson: Bson               = order.sortBy(field.toString)
  override def toString: String  = s"$field.$order"
}

object Sort {

  sealed trait Order {
    def sortBy(fields: String*): Bson
  }

  object Order {

    case object Ascending extends Order {
      override def sortBy(fields: String*): Bson = ascending(fields: _*)
      override def toString: String              = "asc"
    }

    case object Descending extends Order {
      override def sortBy(fields: String*): Bson = descending(fields: _*)
      override def toString: String              = "dsc"
    }
  }

  sealed trait Field

  object Field {

    case object LRN extends Field {
      override def toString: String = "lrn"
    }

    case object CreatedAt extends Field {
      override def toString: String = "createdAt"
    }
  }

  case object SortByLRNAsc extends Sort {
    override val field: Field = LRN
    override val order: Order = Ascending
  }

  case object SortByLRNDesc extends Sort {
    override val field: Field = LRN
    override val order: Order = Descending
  }

  case object SortByCreatedAtAsc extends Sort {
    override val field: Field = CreatedAt
    override val order: Order = Ascending
  }

  case object SortByCreatedAtDesc extends Sort {
    override val field: Field = CreatedAt
    override val order: Order = Descending
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
