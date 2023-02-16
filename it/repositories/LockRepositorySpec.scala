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

import models.Lock
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.LocalDateTime
import scala.concurrent.Future

class LockRepositorySpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with GuiceOneServerPerSuite
    with DefaultPlayMongoRepositorySupport[Lock] {

  override protected def repository: DefaultLockRepository =
    app.injector.instanceOf[DefaultLockRepository]

  val dateNow: LocalDateTime = LocalDateTime.now()

  override def beforeEach(): Unit = {
    super.beforeEach()
    dropCollection()
  }

  "lock" when {

    "applying new lock" should {

      "add new lock" in {

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", dateNow, dateNow)

        val result = repository.lock(lock1).futureValue

        result shouldBe true
      }

      "update lock when sessionId is the same as lock" in {

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", dateNow, dateNow.minusDays(1))
        val lock2: Lock = Lock("session1", "eoriNumber", "lrn", dateNow, dateNow)

        insert(lock1).futureValue

        val lockResult = repository.lock(lock2).futureValue

        val numberOfDocs: Long = repository.collection.countDocuments().head().futureValue

        lockResult shouldBe true
        numberOfDocs shouldBe 1
      }

      "not add new lock if lock already exists and sessionId is different" in {

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", dateNow, dateNow)
        val lock2: Lock = Lock("session2", "eoriNumber", "lrn", dateNow, dateNow)

        insert(lock1).futureValue

        val result = repository.lock(lock2).futureValue

        val numberOfDocs: Long = repository.collection.countDocuments().head().futureValue

        result shouldBe false
        numberOfDocs shouldBe 1
      }
    }
  }

  "findLocks" when {

    "looking for locks" should {

      "find and return existing lock" in {

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", dateNow, dateNow)

        insert(lock1).futureValue

        val result = repository.findLocks(lock1.eoriNumber, lock1.lrn).futureValue

        result.value shouldBe lock1
      }

      "return None for no lock" in {

        val result = repository.findLocks("eoriNumber", "lrn").futureValue

        result shouldBe None
      }
    }
  }

  "unlock" when {

    "when unlocking a document" should {

      "return true for successful unlock" in {

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", dateNow, dateNow)

        insert(lock1).futureValue

        val result = repository.unlock(lock1.eoriNumber, lock1.lrn, "session1").futureValue

        val numberOfDocs: Long = repository.collection.countDocuments().head().futureValue

        result shouldBe true
        numberOfDocs shouldBe 0
      }
    }
  }
}
