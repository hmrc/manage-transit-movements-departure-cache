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

import com.mongodb.client.model.Filters.{regex, and => mAnd, eq => mEq}
import config.AppConfig
import models.UserAnswers
import org.bson.conversions.Bson
import org.mongodb.scala.model.Indexes.{ascending, compoundIndex, descending}
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CacheRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UserAnswers](
      mongoComponent = mongoComponent,
      collectionName = CacheRepository.collectionName,
      domainFormat = UserAnswers.mongoFormat,
      indexes = CacheRepository.indexes(appConfig)
    ) {

  def get(lrn: String, eoriNumber: String): Future[Option[UserAnswers]] = {
    val filter = Filters.and(
      Filters.eq("lrn", lrn),
      Filters.eq("eoriNumber", eoriNumber)
    )
    val update  = Updates.set("lastUpdated", LocalDateTime.now())
    val options = FindOneAndUpdateOptions().upsert(false).sort(Sorts.descending("createdAt"))

    collection
      .findOneAndUpdate(filter, update, options)
      .toFutureOption()
  }

  def set(userAnswers: UserAnswers): Future[Boolean] = {
    val filter = Filters.and(
      Filters.eq("lrn", userAnswers.lrn),
      Filters.eq("eoriNumber", userAnswers.eoriNumber)
    )
    val updatedUserAnswers = userAnswers.copy(lastUpdated = LocalDateTime.now())

    collection
      .replaceOne(filter, updatedUserAnswers, ReplaceOptions().upsert(true))
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
    limit: Option[Int] = None,
    skip: Option[Int] = None
  ): Future[Seq[UserAnswers]] = {

    val skipIndex: Int   = skip.getOrElse(0)
    val returnLimit: Int = limit.getOrElse(appConfig.maxRowsReturned)
    val skipLimit: Int   = skipIndex * returnLimit
    val lrnRegex         = lrn.map(_.replace(" ", "")).getOrElse("")

    val selector: Bson = mAnd(
      mEq("eoriNumber", eoriNumber),
      regex("lrn", lrnRegex)
    )

    val aggregates = Seq(
      Aggregates.filter(selector),
      Aggregates.sort(descending("createdAt")),
      Aggregates.skip(skipLimit),
      Aggregates.limit(returnLimit)
    )

    collection.aggregate[UserAnswers](aggregates).toFuture()
  }

}

object CacheRepository {

  val collectionName: String = "user-answers"

  def indexes(appConfig: AppConfig): Seq[IndexModel] = {
    val userAnswersCreatedAtIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("createdAt"),
      indexOptions = IndexOptions().name("user-answers-created-at-index").expireAfter(appConfig.mongoTtlInDays, TimeUnit.DAYS)
    )

    val eoriNumberAndLrnCompoundIndex: IndexModel = IndexModel(
      keys = compoundIndex(ascending("eoriNumber"), ascending("lrn")),
      indexOptions = IndexOptions().name("eoriNumber-lrn-index")
    )

    Seq(userAnswersCreatedAtIndex, eoriNumberAndLrnCompoundIndex)
  }
}
