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

package repositories

import com.mongodb.client.model.Filters.{and as mAnd, eq as mEq, regex}
import config.AppConfig
import models.*
import org.bson.conversions.Bson
import org.mongodb.scala.*
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Indexes.{ascending, compoundIndex}
import play.api.libs.json.{JsObject, Writes}
import services.DateTimeService
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CacheRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: AppConfig,
  dateTimeService: DateTimeService
)(implicit ec: ExecutionContext, sensitiveFormats: SensitiveFormats)
    extends PlayMongoRepository[UserAnswers](
      mongoComponent = mongoComponent,
      collectionName = CacheRepository.collectionName,
      domainFormat = UserAnswers.sensitiveFormat,
      indexes = CacheRepository.indexes(appConfig)
    ) {

  def get(lrn: String, eoriNumber: String): Future[Option[UserAnswers]] = {
    val filter = Filters.and(
      Filters.eq("lrn", lrn),
      Filters.eq("eoriNumber", eoriNumber)
    )
    val update  = Updates.set("lastUpdated", dateTimeService.timestamp)
    val options = FindOneAndUpdateOptions().upsert(false).sort(Sorts.descending("createdAt"))

    collection
      .findOneAndUpdate(filter, update, options)
      .toFutureOption()
  }

  def set(data: Metadata, departureId: Option[String]): Future[Boolean] = {
    val now = dateTimeService.timestamp
    val filter = Filters.and(
      Filters.eq("lrn", data.lrn),
      Filters.eq("eoriNumber", data.eoriNumber)
    )

    implicit val dataWrites: Writes[JsObject] = sensitiveFormats.jsObjectWrites

    val updates: Seq[Bson] = Seq(
      Some(Updates.setOnInsert("lrn", data.lrn)),
      Some(Updates.setOnInsert("eoriNumber", data.eoriNumber)),
      Some(Updates.set("data", Codecs.toBson(data.data))),
      Some(Updates.set("tasks", Codecs.toBson(data.tasks))),
      Some(Updates.setOnInsert("createdAt", now)),
      Some(Updates.setOnInsert("isTransitional", false)),
      Some(Updates.set("lastUpdated", now)),
      Some(Updates.setOnInsert("_id", Codecs.toBson(UUID.randomUUID()))),
      Some(Updates.set("isSubmitted", data.isSubmitted.asString)),
      departureId.map(Updates.set("departureId", _))
    ).flatten

    val combineUpdates: Bson = Updates.combine(updates*)
    val options              = UpdateOptions().upsert(true)

    collection
      .updateOne(filter, combineUpdates, options)
      .toFuture()
      .map(_.wasAcknowledged())
  }

  def remove(lrn: String, eoriNumber: String): Future[Boolean] = {
    val filter = Filters.and(
      Filters.eq("lrn", lrn),
      Filters.eq("eoriNumber", eoriNumber)
    )

    collection
      .deleteOne(filter)
      .toFuture()
      .map(_.wasAcknowledged())
  }

  def getAll(
    eoriNumber: String,
    lrn: Option[String] = None,
    state: Option[SubmissionState] = None,
    limit: Option[Int] = None,
    skip: Option[Int] = None,
    sortBy: Option[String] = None
  ): Future[UserAnswersSummary] = {

    val skipIndex: Int   = skip.getOrElse(0)
    val returnLimit: Int = limit.getOrElse(appConfig.maxRowsReturned)
    val skipLimit: Int   = skipIndex * returnLimit

    val eoriFilter: Bson          = mEq("eoriNumber", eoriNumber)
    val lrnFilter: Option[Bson]   = lrn.map(_.replace(" ", "")).map(regex("lrn", _))
    val stateFilter: Option[Bson] = state.map(_.asString).map(mEq("isSubmitted", _))

    val filters = Seq(Some(eoriFilter), lrnFilter, stateFilter).flatten

    val primaryFilter = Aggregates.filter(mAnd(filters*))

    val aggregates: Seq[Bson] = Seq(
      primaryFilter,
      Aggregates.sort(Sort(sortBy).toBson),
      Aggregates.skip(skipLimit),
      Aggregates.limit(returnLimit)
    )

    for {
      totalDocuments         <- collection.countDocuments(eoriFilter).toFuture()
      totalMatchingDocuments <- collection.aggregate[UserAnswers](Seq(primaryFilter)).toFuture().map(_.length)
      aggregateResult        <- collection.aggregate[UserAnswers](aggregates).toFuture()
    } yield UserAnswersSummary(
      eoriNumber,
      aggregateResult,
      totalDocuments.toInt,
      totalMatchingDocuments
    )
  }
}

object CacheRepository {

  val collectionName: String = "user-answers"

  def indexes(appConfig: AppConfig): Seq[IndexModel] = {
    val userAnswersCreatedAtIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("createdAt"),
      indexOptions = IndexOptions().name("user-answers-created-at-index").expireAfter(appConfig.mongoTtlInDays.toLong, TimeUnit.DAYS)
    )

    val eoriNumberAndLrnCompoundIndex: IndexModel = IndexModel(
      keys = compoundIndex(ascending("eoriNumber"), ascending("lrn")),
      indexOptions = IndexOptions().name("eoriNumber-lrn-index")
    )

    val _idAndLrnIndex: IndexModel = IndexModel(
      keys = compoundIndex(ascending("lrn"), ascending("_id")),
      indexOptions = IndexOptions().name("_id-lrn-index")
    )

    Seq(userAnswersCreatedAtIndex, eoriNumberAndLrnCompoundIndex, _idAndLrnIndex)
  }

}
