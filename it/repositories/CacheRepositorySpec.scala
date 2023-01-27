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

import itbase.ItSpecBase
import models.UserAnswers
import org.mongodb.scala.bson.{BsonDocument, BsonInt64, BsonString}
import org.mongodb.scala.model.Filters
import org.mongodb.scala.{Document, MongoWriteException}
import play.api.libs.json.Json

import java.util.UUID

class CacheRepositorySpec extends ItSpecBase {

  private lazy val userAnswers1 = emptyUserAnswers.copy(lrn = "ABCD1111111111111", eoriNumber = "EoriNumber1")
  private lazy val userAnswers2 = emptyUserAnswers.copy(lrn = "ABCD2222222222222", eoriNumber = "EoriNumber2")
  private lazy val userAnswers3 = emptyUserAnswers.copy(lrn = "ABCD3333333333333", eoriNumber = "EoriNumber3")

  private lazy val userAnswers4 = emptyUserAnswers.copy(lrn = "ABCD1111111111111", eoriNumber = "EoriNumber4")
  private lazy val userAnswers5 = emptyUserAnswers.copy(lrn = "ABCD2222222222222", eoriNumber = "EoriNumber4")
  private lazy val userAnswers6 = emptyUserAnswers.copy(lrn = "EFGH3333333333333", eoriNumber = "EoriNumber4")

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

      result.value.lrn shouldBe userAnswers1.lrn
      result.value.eoriNumber shouldBe userAnswers1.eoriNumber
      result.value.data shouldBe userAnswers1.data
    }

    "return None when no UserAnswers match LocalReferenceNumber" in {

      val result = repository.get(userAnswers3.lrn, userAnswers1.eoriNumber).futureValue

      result shouldBe None
    }

    "return None when no UserAnswers match EoriNumber" in {

      val result = repository.get(userAnswers1.lrn, userAnswers3.eoriNumber).futureValue

      result shouldBe None
    }
  }

  "set" must {

    "create new document when given valid UserAnswers" in {

      findOne(userAnswers3.lrn, userAnswers3.eoriNumber) should not be defined

      val setResult = repository.set(userAnswers3).futureValue

      setResult shouldBe true

      val getResult = findOne(userAnswers3.lrn, userAnswers3.eoriNumber).get

      getResult.lrn shouldBe userAnswers3.lrn
      getResult.eoriNumber shouldBe userAnswers3.eoriNumber
      getResult.data shouldBe userAnswers3.data
    }

    "update document when it already exists" in {

      val firstGet = findOne(userAnswers1.lrn, userAnswers1.eoriNumber).get

      val setResult = repository.set(userAnswers1.copy(data = Json.obj("foo" -> "bar"))).futureValue

      setResult shouldBe true

      val secondGet = findOne(userAnswers1.lrn, userAnswers1.eoriNumber).get

      firstGet.id shouldBe secondGet.id
      firstGet.lrn shouldBe secondGet.lrn
      firstGet.eoriNumber shouldBe secondGet.eoriNumber
      firstGet.data shouldNot equal(secondGet.data)
      firstGet.createdAt shouldBe secondGet.createdAt
      firstGet.lastUpdated isBefore secondGet.lastUpdated shouldBe true
    }

    "fail when attempting to set using an existing LocalReferenceNumber and EoriNumber with a different Id" in {

      val setResult = repository.set(userAnswers1.copy(id = UUID.randomUUID()))

      whenReady(setResult.failed) {
        e =>
          e shouldBe a[MongoWriteException]
      }
    }
  }

  "remove" must {

    "remove document when given a valid LocalReferenceNumber and EoriNumber" in {

      findOne(userAnswers1.lrn, userAnswers1.eoriNumber) shouldBe defined

      val removeResult = repository.remove(userAnswers1.lrn, userAnswers1.eoriNumber).futureValue

      removeResult shouldBe true

      findOne(userAnswers1.lrn, userAnswers1.eoriNumber) should not be defined
    }

    "not fail if document does not exist" in {

      findOne(userAnswers3.lrn, userAnswers3.eoriNumber) should not be defined

      val removeResult = repository.remove(userAnswers3.lrn, userAnswers3.eoriNumber).futureValue

      removeResult shouldBe true
    }
  }

  "getAll" must {

    "when given no params" should {

      "return sequence of userAnswers" in {

        val result = repository.getAll(userAnswers4.eoriNumber).futureValue

        result.length shouldBe 2
        result.head.lrn shouldBe userAnswers4.lrn
        result.head.eoriNumber shouldBe userAnswers4.eoriNumber
        result(1).lrn shouldBe userAnswers5.lrn
        result(1).eoriNumber shouldBe userAnswers5.eoriNumber
      }

      "return empty sequence when given an EoriNumber with no entries" in {

        val result = repository.getAll(userAnswers3.eoriNumber).futureValue

        result shouldBe Seq.empty
      }
    }

    "when given an lrn param" should {

      "return sequence of userAnswers that match a full LRN" in {

        insert(userAnswers6).futureValue

        val result = repository.getAll(userAnswers4.eoriNumber, Some(userAnswers4.lrn)).futureValue

        result.length shouldBe 1
        result.head.lrn shouldBe userAnswers4.lrn
        result.head.eoriNumber shouldBe userAnswers4.eoriNumber
      }

      "return sequence of userAnswers that match a partial LRN" in {

        insert(userAnswers6).futureValue

        val result = repository.getAll(userAnswers4.eoriNumber, Some("ABCD")).futureValue

        result.length shouldBe 2
        result.head.lrn shouldBe userAnswers4.lrn
        result.head.eoriNumber shouldBe userAnswers4.eoriNumber
        result(1).lrn shouldBe userAnswers5.lrn
        result(1).eoriNumber shouldBe userAnswers5.eoriNumber
      }

      "return empty sequence when given an EoriNumber with no entries" in {

        val result = repository.getAll(userAnswers4.eoriNumber, Some("INVALID_SEARCH")).futureValue

        result shouldBe Seq.empty
      }
    }
  }

  "ensureIndexes" must {
    "ensure the correct indexes" in {
      val indexes = repository.collection.listIndexes().toFuture().futureValue
      indexes.length shouldBe 3

      indexes.head.get("name").get shouldBe BsonString("_id_")

      def findIndex(name: String): Document = indexes.find(_.get("name").get == BsonString(name)).get

      val createdAtIndex = findIndex("user-answers-created-at-index")
      createdAtIndex.get("key").get shouldBe BsonDocument("createdAt" -> 1)
      createdAtIndex.get("expireAfterSeconds").get shouldBe BsonInt64(2592000)

      val eoriLrnIndex = findIndex("eoriNumber-lrn-index")
      eoriLrnIndex.get("key").get shouldBe BsonDocument("eoriNumber" -> 1, "lrn" -> 1)
    }
  }
}
