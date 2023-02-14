package repositories

import com.mongodb.MongoWriteException
import config.AppConfig
import models.Lock
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DefaultLockRepository @Inject() (mongoC: MongoComponent, appConfig: AppConfig)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[Lock](
    mongoComponent = mongoC,
    collectionName = "draft-locks",
    domainFormat = Lock.format,
    indexes = LockRepository.indexes(appConfig)
  ) {


  def lock(newLock: Lock): Future[Boolean] =
    collection
      .insertOne(newLock)
      .head()
      .map(insertOneResult => insertOneResult.wasAcknowledged())
      .recoverWith {
        case mEx: MongoWriteException if mEx.getError.getCode == 11000 =>
          getExistingLock(newLock._id, newLock.eoriNumber, newLock.lrn)
            .map {
              _.getOrElse(throw new Exception("Expected afa to be locked, but no lock was found"))
            }
            .map { existingLock =>
              existingLock._id == newLock._id
            }
      }

  def getExistingLock(eoriNumber: String, sessionId: String, lrn: String): Future[Option[Lock]] = {

    val filter = Filters.and(
      Filters.eq("sessionId", sessionId),
      Filters.eq("eoriNumber", eoriNumber),
      Filters.eq("lrn", lrn),
    )

    collection.find(filter).headOption()
  }


  def unlock = ???

  def checkLock = ???

}

object LockRepository {

  def indexes(appConfig: AppConfig): Seq[IndexModel] = {
    val userAnswersCreatedAtIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("createdAt"),
      indexOptions = IndexOptions().name("draft-lock-created-at-index").expireAfter(appConfig.lockTTLInMins, TimeUnit.MINUTES)
    )

    Seq(userAnswersCreatedAtIndex)
  }
}
