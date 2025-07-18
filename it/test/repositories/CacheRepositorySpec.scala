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

import itbase.CacheRepositorySpecBase
import models.*
import models.Sort.{SortByCreatedAtAsc, SortByCreatedAtDesc, SortByLRNAsc, SortByLRNDesc}
import org.mongodb.scala.*
import org.mongodb.scala.bson.{BsonDocument, BsonString}
import org.mongodb.scala.model.Filters
import play.api.libs.json.Json

import java.time.Instant
import java.time.temporal.ChronoUnit.*

class CacheRepositorySpec extends CacheRepositorySpecBase {

  private val lrn1 = "ABCD1111111111111"
  private val lrn2 = "ABCD2222222222222"
  private val lrn3 = "ABCD3333333333333"
  private val lrn4 = "EFGH3333333333333"

  private val eori1 = "EoriNumber1"
  private val eori2 = "EoriNumber2"
  private val eori3 = "EoriNumber3"
  private val eori4 = "EoriNumber4"

  private val userAnswers1 = emptyUserAnswers.copy(metadata = Metadata(lrn1, eori1))
  private val userAnswers2 = emptyUserAnswers.copy(metadata = Metadata(lrn2, eori2))
  private val userAnswers3 = emptyUserAnswers.copy(metadata = Metadata(lrn3, eori3))
  private val userAnswers4 = emptyUserAnswers.copy(metadata = Metadata(lrn1, eori4), createdAt = Instant.now())
  private val userAnswers5 = emptyUserAnswers.copy(metadata = Metadata(lrn2, eori4), createdAt = Instant.now().minus(1, HOURS))
  private val userAnswers6 = emptyUserAnswers.copy(metadata = Metadata(lrn4, eori4))
  private val userAnswers7 = emptyUserAnswers.copy(metadata = Metadata(lrn1, eori1, SubmissionState.Submitted))
  private val userAnswers8 = emptyUserAnswers.copy(metadata = Metadata(lrn1, eori1, SubmissionState.RejectedPendingChanges))

  override def beforeEach(): Unit = {
    super.beforeEach()
    insert(userAnswers1).futureValue
    insert(userAnswers2).futureValue
    insert(userAnswers4).futureValue
    insert(userAnswers5).futureValue
  }

  private def findOne(lrn: String, eoriNumber: String): Option[UserAnswers] =
    find(
      Filters.and(
        Filters.eq("lrn", lrn),
        Filters.eq("eoriNumber", eoriNumber)
      )
    ).futureValue.headOption

  "get" must {

    "return UserAnswers when given an LocalReferenceNumber and EoriNumber" in {

      val result = repository.get(userAnswers1.lrn, userAnswers1.eoriNumber).futureValue

      result.value.lrn shouldEqual userAnswers1.lrn
      result.value.eoriNumber shouldEqual userAnswers1.eoriNumber
      result.value.metadata shouldEqual userAnswers1.metadata
    }

    "return None when no UserAnswers match LocalReferenceNumber" in {

      val result = repository.get(userAnswers3.lrn, userAnswers1.eoriNumber).futureValue

      result should not be defined
    }

    "return None when no UserAnswers match EoriNumber" in {

      val result = repository.get(userAnswers1.lrn, userAnswers3.eoriNumber).futureValue

      result should not be defined
    }
  }

  "set" must {

    "create new document when given valid UserAnswers" in {

      findOne(userAnswers3.lrn, userAnswers3.eoriNumber) should not be defined

      val setResult = repository.set(userAnswers3.metadata, None).futureValue

      setResult shouldEqual true

      val getResult = findOne(userAnswers3.lrn, userAnswers3.eoriNumber).get

      getResult.lrn shouldEqual userAnswers3.lrn
      getResult.eoriNumber shouldEqual userAnswers3.eoriNumber
      getResult.metadata shouldEqual userAnswers3.metadata
    }

    "create new document when given valid UserAnswers with departureId" in {

      findOne(userAnswers3.lrn, userAnswers3.eoriNumber) should not be defined
      val depId = "departureId123"

      val setResult = repository.set(userAnswers3.metadata, Some(depId)).futureValue

      setResult shouldEqual true

      val getResult = findOne(userAnswers3.lrn, userAnswers3.eoriNumber).get

      getResult.lrn shouldEqual userAnswers3.lrn
      getResult.eoriNumber shouldEqual userAnswers3.eoriNumber
      getResult.metadata shouldEqual userAnswers3.metadata
      getResult.departureId.get shouldEqual depId
    }

    "update document when it already exists" in {

      val firstGet = findOne(userAnswers1.lrn, userAnswers1.eoriNumber).get

      val metadata = userAnswers1.metadata.copy(
        data = Json.obj("foo" -> "bar"),
        tasks = Map(".task" -> Status.InProgress)
      )
      val setResult = repository.set(metadata, None).futureValue

      setResult shouldEqual true

      val secondGet = findOne(userAnswers1.lrn, userAnswers1.eoriNumber).get

      firstGet.id shouldEqual secondGet.id
      firstGet.lrn shouldEqual secondGet.lrn
      firstGet.eoriNumber shouldEqual secondGet.eoriNumber
      firstGet.metadata shouldNot equal(secondGet.metadata)
      firstGet.createdAt shouldEqual secondGet.createdAt
      firstGet.lastUpdated `isBefore` secondGet.lastUpdated shouldEqual true
    }
  }

  "remove" must {

    "remove document when given a valid LocalReferenceNumber and EoriNumber" in {

      findOne(userAnswers1.lrn, userAnswers1.eoriNumber) shouldBe defined

      val removeResult = repository.remove(userAnswers1.lrn, userAnswers1.eoriNumber).futureValue

      removeResult shouldEqual true

      findOne(userAnswers1.lrn, userAnswers1.eoriNumber) should not be defined
    }

    "not fail if document does not exist" in {

      findOne(userAnswers3.lrn, userAnswers3.eoriNumber) should not be defined

      val removeResult = repository.remove(userAnswers3.lrn, userAnswers3.eoriNumber).futureValue

      removeResult shouldEqual true
    }
  }

  "getAll" must {

    "when given no params" should {

      "return UserAnswersSummary" in {

        val result = repository.getAll(userAnswers4.eoriNumber).futureValue

        result match {
          case UserAnswersSummary(eoriNumber, userAnswers, totalMovements, totalMatchingMovements) =>
            eoriNumber shouldEqual userAnswers4.eoriNumber
            totalMovements shouldEqual 2
            totalMatchingMovements shouldEqual 2
            userAnswers.length shouldEqual 2
            userAnswers.head.lrn shouldEqual userAnswers4.lrn
            userAnswers.head.eoriNumber shouldEqual userAnswers4.eoriNumber
            userAnswers(1).lrn shouldEqual userAnswers5.lrn
            userAnswers(1).eoriNumber shouldEqual userAnswers5.eoriNumber
        }
      }

      "return UserAnswersSummary with empty userAnswers when given an EoriNumber with no entries" in {

        val result = repository.getAll(userAnswers3.eoriNumber).futureValue

        result.userAnswers shouldEqual Seq.empty
      }

      "return UserAnswersSummary with only un-submitted declarations" in {

        insert(userAnswers7).futureValue
        insert(userAnswers8).futureValue

        val result = repository.getAll(eori1, state = Some(SubmissionState.NotSubmitted)).futureValue

        result match {
          case UserAnswersSummary(eoriNumber, userAnswers, totalMovements, totalMatchingMovements) =>
            eoriNumber shouldEqual eori1
            totalMovements shouldEqual 3
            totalMatchingMovements shouldEqual 1
            userAnswers.length shouldEqual 1
        }
      }
    }

    "when given an lrn param" should {

      "return UserAnswersSummary that match a full LRN" in {

        insert(userAnswers6).futureValue

        val result = repository.getAll(userAnswers4.eoriNumber, Some(userAnswers4.lrn)).futureValue

        result match {
          case UserAnswersSummary(eoriNumber, userAnswers, totalMovements, totalMatchingMovements) =>
            eoriNumber shouldEqual userAnswers4.eoriNumber
            totalMovements shouldEqual 3
            totalMatchingMovements shouldEqual 1
            userAnswers.length shouldEqual 1
            userAnswers.head.lrn shouldEqual userAnswers4.lrn
            userAnswers.head.eoriNumber shouldEqual userAnswers4.eoriNumber
        }
      }

      "return UserAnswersSummary that match a partial LRN" in {

        insert(userAnswers6).futureValue

        val result = repository.getAll(userAnswers4.eoriNumber, Some("ABCD")).futureValue

        result match {
          case UserAnswersSummary(eoriNumber, userAnswers, totalMovements, totalMatchingMovements) =>
            eoriNumber shouldEqual userAnswers4.eoriNumber
            totalMovements shouldEqual 3
            totalMatchingMovements shouldEqual 2
            userAnswers.length shouldEqual 2
            userAnswers.head.lrn shouldEqual userAnswers4.lrn
            userAnswers.head.eoriNumber shouldEqual userAnswers4.eoriNumber
            userAnswers(1).lrn shouldEqual userAnswers5.lrn
            userAnswers(1).eoriNumber shouldEqual userAnswers5.eoriNumber
        }

      }

      "return UserAnswersSummary with empty sequence of userAnswers when given an EoriNumber with no entries" in {

        val result = repository.getAll(userAnswers4.eoriNumber, Some("INVALID_SEARCH")).futureValue

        result.userAnswers shouldEqual Seq.empty
      }
    }

    "when given limit param" should {

      "return UserAnswersSummary to limit sorted by createdDate" in {

        val userAnswers1 = emptyUserAnswers.copy(metadata = Metadata("XI1111111111111", "AB123")).copy(createdAt = Instant.now())
        val userAnswers2 = emptyUserAnswers.copy(metadata = Metadata("X22222222222222", "AB123"), createdAt = Instant.now().minus(1, HOURS))
        val userAnswers3 = emptyUserAnswers.copy(metadata = Metadata("GB13333333333333", "AB123"), createdAt = Instant.now().minus(2, DAYS))
        val userAnswers4 = emptyUserAnswers.copy(metadata = Metadata("GB24444444444444", "AB123"), createdAt = Instant.now().minus(1, DAYS))

        insert(userAnswers1).futureValue
        insert(userAnswers2).futureValue
        insert(userAnswers3).futureValue
        insert(userAnswers4).futureValue

        val result = repository.getAll(userAnswers1.eoriNumber, limit = Some(2)).futureValue

        result match {
          case UserAnswersSummary(eoriNumber, userAnswers, totalMovements, totalMatchingMovements) =>
            eoriNumber shouldEqual userAnswers1.eoriNumber
            totalMovements shouldEqual 4
            totalMatchingMovements shouldEqual 4
            userAnswers.length shouldEqual 2
            userAnswers.head.lrn shouldEqual userAnswers1.lrn
            userAnswers(1).lrn shouldEqual userAnswers2.lrn
        }

      }

      "return UserAnswersSummary to limit and to lrn param sorted by createdDate" in {

        val userAnswers1 = emptyUserAnswers.copy(metadata = Metadata("XI1111111111111", "AB123"), createdAt = Instant.now())
        val userAnswers2 = emptyUserAnswers.copy(metadata = Metadata("XI2222222222222", "AB123"), createdAt = Instant.now().minus(1, HOURS))
        val userAnswers3 = emptyUserAnswers.copy(metadata = Metadata("XI3333333333333", "AB123"), createdAt = Instant.now().minus(2, HOURS))
        val userAnswers4 = emptyUserAnswers.copy(metadata = Metadata("GB1111111111111", "AB123"), createdAt = Instant.now())
        val userAnswers5 = emptyUserAnswers.copy(metadata = Metadata("GB2222222222222", "AB123"), createdAt = Instant.now().minus(1, DAYS))
        val userAnswers6 = emptyUserAnswers.copy(metadata = Metadata("GB3333333333333", "AB123"), createdAt = Instant.now().minus(2, DAYS))

        insert(userAnswers1).futureValue
        insert(userAnswers2).futureValue
        insert(userAnswers3).futureValue
        insert(userAnswers4).futureValue
        insert(userAnswers5).futureValue
        insert(userAnswers6).futureValue

        val result = repository.getAll(userAnswers1.eoriNumber, Some("GB"), limit = Some(2)).futureValue

        result match {
          case UserAnswersSummary(eoriNumber, userAnswers, totalMovements, totalMatchingMovements) =>
            eoriNumber shouldEqual userAnswers1.eoriNumber
            totalMovements shouldEqual 6
            totalMatchingMovements shouldEqual 3
            userAnswers.length shouldEqual 2
            userAnswers.head.lrn shouldEqual userAnswers4.lrn
            userAnswers(1).lrn shouldEqual userAnswers5.lrn
        }
      }
    }

    "when given skip param" should {

      "return UserAnswersSummary, skipping based on skip param and limit param" in {

        val userAnswers1 = emptyUserAnswers.copy(metadata = Metadata("GB111", "AB123"), createdAt = Instant.now())
        val userAnswers2 = emptyUserAnswers.copy(metadata = Metadata("GB222", "AB123"), createdAt = Instant.now().minus(1, HOURS))
        val userAnswers3 = emptyUserAnswers.copy(metadata = Metadata("GB333", "AB123"), createdAt = Instant.now().minus(1, DAYS))
        val userAnswers4 = emptyUserAnswers.copy(metadata = Metadata("GB444", "AB123"), createdAt = Instant.now().minus(2, DAYS))
        val userAnswers5 = emptyUserAnswers.copy(metadata = Metadata("GB555", "AB123"), createdAt = Instant.now().minus(3, DAYS))
        val userAnswers6 = emptyUserAnswers.copy(metadata = Metadata("GB666", "AB123"), createdAt = Instant.now().minus(4, DAYS))

        insert(userAnswers1).futureValue
        insert(userAnswers2).futureValue
        insert(userAnswers3).futureValue
        insert(userAnswers4).futureValue
        insert(userAnswers5).futureValue
        insert(userAnswers6).futureValue

        val result1 = repository.getAll(userAnswers1.eoriNumber, limit = Some(2), skip = Some(1)).futureValue
        val result2 = repository.getAll(userAnswers1.eoriNumber, limit = Some(2), skip = Some(2)).futureValue
        val result3 = repository.getAll(userAnswers1.eoriNumber, limit = Some(3), skip = Some(1)).futureValue

        result1 match {
          case UserAnswersSummary(eoriNumber, userAnswers, totalMovements, totalMatchingMovements) =>
            eoriNumber shouldEqual userAnswers1.eoriNumber
            totalMovements shouldEqual 6
            totalMatchingMovements shouldEqual 6
            userAnswers.length shouldEqual 2
            userAnswers.head.lrn shouldEqual userAnswers3.lrn
            userAnswers(1).lrn shouldEqual userAnswers4.lrn
        }

        result2 match {
          case UserAnswersSummary(eoriNumber, userAnswers, totalMovements, totalMatchingMovements) =>
            eoriNumber shouldEqual userAnswers1.eoriNumber
            totalMovements shouldEqual 6
            totalMatchingMovements shouldEqual 6
            userAnswers.length shouldEqual 2
            userAnswers.head.lrn shouldEqual userAnswers5.lrn
            userAnswers(1).lrn shouldEqual userAnswers6.lrn
        }

        result3 match {
          case UserAnswersSummary(eoriNumber, userAnswers, totalMovements, totalMatchingMovements) =>
            eoriNumber shouldEqual userAnswers1.eoriNumber
            totalMovements shouldEqual 6
            totalMatchingMovements shouldEqual 6
            userAnswers.length shouldEqual 3
            userAnswers.head.lrn shouldEqual userAnswers4.lrn
            userAnswers(1).lrn shouldEqual userAnswers5.lrn
            userAnswers(2).lrn shouldEqual userAnswers6.lrn
        }
      }

      "return UserAnswersSummary to limit, lrn and skip param sorted by createdDate" in {

        val userAnswers1 = emptyUserAnswers.copy(metadata = Metadata("XI1111111111111", "AB123"), createdAt = Instant.now())
        val userAnswers2 = emptyUserAnswers.copy(metadata = Metadata("XI2222222222222", "AB123"), createdAt = Instant.now().minus(1, HOURS))
        val userAnswers3 = emptyUserAnswers.copy(metadata = Metadata("XI3333333333333", "AB123"), createdAt = Instant.now().minus(2, HOURS))
        val userAnswers4 = emptyUserAnswers.copy(metadata = Metadata("GB1111111111111", "AB123"), createdAt = Instant.now())
        val userAnswers5 = emptyUserAnswers.copy(metadata = Metadata("GB2222222222222", "AB123"), createdAt = Instant.now().minus(1, DAYS))
        val userAnswers6 = emptyUserAnswers.copy(metadata = Metadata("GB3333333333333", "AB123"), createdAt = Instant.now().minus(2, DAYS))

        insert(userAnswers1).futureValue
        insert(userAnswers2).futureValue
        insert(userAnswers3).futureValue
        insert(userAnswers4).futureValue
        insert(userAnswers5).futureValue
        insert(userAnswers6).futureValue

        val result = repository.getAll(userAnswers1.eoriNumber, Some("GB"), limit = Some(2), skip = Some(1)).futureValue

        result match {
          case UserAnswersSummary(eoriNumber, userAnswers, totalMovements, totalMatchingMovements) =>
            eoriNumber shouldEqual userAnswers1.eoriNumber
            totalMovements shouldEqual 6
            totalMatchingMovements shouldEqual 3
            userAnswers.length shouldEqual 1
            userAnswers.head.lrn shouldEqual userAnswers6.lrn
        }
      }
    }

    "when given sortBy param" should {

      val userAnswers1 = emptyUserAnswers.copy(metadata = Metadata("AA1111111111111", "AB123"), createdAt = Instant.now().minus(3, DAYS))
      val userAnswers2 = emptyUserAnswers.copy(metadata = Metadata("BB2222222222222", "AB123"), createdAt = Instant.now().minus(6, DAYS))
      val userAnswers3 = emptyUserAnswers.copy(metadata = Metadata("CC3333333333333", "AB123"), createdAt = Instant.now().minus(5, DAYS))
      val userAnswers4 = emptyUserAnswers.copy(metadata = Metadata("DD1111111111111", "AB123"), createdAt = Instant.now().minus(4, DAYS))
      val userAnswers5 = emptyUserAnswers.copy(metadata = Metadata("EE2222222222222", "AB123"), createdAt = Instant.now().minus(1, DAYS))
      val userAnswers6 = emptyUserAnswers.copy(metadata = Metadata("FF3333333333333", "AB123"), createdAt = Instant.now().minus(2, DAYS))

      "return UserAnswersSummary, which is sorted by lrn in ascending order when sortBy is lrn.asc" in {

        insert(userAnswers6).futureValue
        insert(userAnswers4).futureValue
        insert(userAnswers5).futureValue
        insert(userAnswers1).futureValue
        insert(userAnswers3).futureValue
        insert(userAnswers2).futureValue

        val result = repository.getAll(userAnswers1.eoriNumber, sortBy = Some(SortByLRNAsc.convertParams)).futureValue

        result match {
          case UserAnswersSummary(_, userAnswers, _, _) =>
            userAnswers.head.lrn shouldEqual userAnswers1.lrn
            userAnswers(1).lrn shouldEqual userAnswers2.lrn
            userAnswers(2).lrn shouldEqual userAnswers3.lrn
            userAnswers(3).lrn shouldEqual userAnswers4.lrn
            userAnswers(4).lrn shouldEqual userAnswers5.lrn
            userAnswers(5).lrn shouldEqual userAnswers6.lrn
        }

      }
      "return UserAnswersSummary, which is sorted by lrn in descending order when sortBy is lrn.desc" in {

        insert(userAnswers6).futureValue
        insert(userAnswers4).futureValue
        insert(userAnswers5).futureValue
        insert(userAnswers1).futureValue
        insert(userAnswers3).futureValue
        insert(userAnswers2).futureValue

        val result = repository.getAll(userAnswers1.eoriNumber, sortBy = Some(SortByLRNDesc.convertParams)).futureValue

        result match {
          case UserAnswersSummary(_, userAnswers, _, _) =>
            userAnswers.head.lrn shouldEqual userAnswers6.lrn
            userAnswers(1).lrn shouldEqual userAnswers5.lrn
            userAnswers(2).lrn shouldEqual userAnswers4.lrn
            userAnswers(3).lrn shouldEqual userAnswers3.lrn
            userAnswers(4).lrn shouldEqual userAnswers2.lrn
            userAnswers(5).lrn shouldEqual userAnswers1.lrn
        }

      }
      "return UserAnswersSummary, which is sorted by createdAt in ascending order when sortBy is createdAt.asc" in {

        insert(userAnswers6).futureValue
        insert(userAnswers4).futureValue
        insert(userAnswers5).futureValue
        insert(userAnswers1).futureValue
        insert(userAnswers3).futureValue
        insert(userAnswers2).futureValue

        val result = repository.getAll(userAnswers1.eoriNumber, sortBy = Some(SortByCreatedAtAsc.convertParams)).futureValue

        result match {
          case UserAnswersSummary(_, userAnswers, _, _) =>
            userAnswers.head.lrn shouldEqual userAnswers2.lrn
            userAnswers(1).lrn shouldEqual userAnswers3.lrn
            userAnswers(2).lrn shouldEqual userAnswers4.lrn
            userAnswers(3).lrn shouldEqual userAnswers1.lrn
            userAnswers(4).lrn shouldEqual userAnswers6.lrn
            userAnswers(5).lrn shouldEqual userAnswers5.lrn
        }

      }
      "return UserAnswersSummary, which is sorted by createdAt in descending order when sortBy is createdAt.desc" in {

        insert(userAnswers6).futureValue
        insert(userAnswers4).futureValue
        insert(userAnswers5).futureValue
        insert(userAnswers1).futureValue
        insert(userAnswers3).futureValue
        insert(userAnswers2).futureValue

        val result = repository.getAll(userAnswers1.eoriNumber, sortBy = Some(SortByCreatedAtDesc.convertParams)).futureValue

        result match {
          case UserAnswersSummary(_, userAnswers, _, _) =>
            userAnswers.head.lrn shouldEqual userAnswers5.lrn
            userAnswers(1).lrn shouldEqual userAnswers6.lrn
            userAnswers(2).lrn shouldEqual userAnswers1.lrn
            userAnswers(3).lrn shouldEqual userAnswers4.lrn
            userAnswers(4).lrn shouldEqual userAnswers3.lrn
            userAnswers(5).lrn shouldEqual userAnswers2.lrn
        }

      }

      "return UserAnswersSummary, which is sorted by createdAt in descending order when sortBy is None" in {

        insert(userAnswers6).futureValue
        insert(userAnswers4).futureValue
        insert(userAnswers5).futureValue
        insert(userAnswers1).futureValue
        insert(userAnswers3).futureValue
        insert(userAnswers2).futureValue

        val result = repository.getAll(userAnswers1.eoriNumber, sortBy = None).futureValue

        result match {
          case UserAnswersSummary(_, userAnswers, _, _) =>
            userAnswers.head.lrn shouldEqual userAnswers5.lrn
            userAnswers(1).lrn shouldEqual userAnswers6.lrn
            userAnswers(2).lrn shouldEqual userAnswers1.lrn
            userAnswers(3).lrn shouldEqual userAnswers4.lrn
            userAnswers(4).lrn shouldEqual userAnswers3.lrn
            userAnswers(5).lrn shouldEqual userAnswers2.lrn
        }

      }
    }
  }

  "ensureIndexes" must {
    "ensure the correct indexes" in {
      val indexes = repository.collection.listIndexes().toFuture().futureValue
      indexes.length shouldEqual 4

      indexes.head.get("name").get shouldEqual BsonString("_id_")

      def findIndex(name: String): Document = indexes.find(_.get("name").get == BsonString(name)).get

      val createdAtIndex = findIndex("user-answers-created-at-index")
      createdAtIndex.get("key").get shouldEqual BsonDocument("createdAt" -> 1)
      createdAtIndex.get("expireAfterSeconds").get.asNumber().intValue() shouldEqual 2592000

      val eoriLrnIndex = findIndex("eoriNumber-lrn-index")
      eoriLrnIndex.get("key").get shouldEqual BsonDocument("eoriNumber" -> 1, "lrn" -> 1)

      val idLrnIndex = findIndex("_id-lrn-index")
      idLrnIndex.get("key").get shouldEqual BsonDocument("_id" -> 1, "lrn" -> 1)
    }
  }
}
