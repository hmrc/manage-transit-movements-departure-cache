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

import itbase.LockRepositorySpecBase
import models.Lock

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

class LockRepositorySpec extends LockRepositorySpecBase {

  val now: Instant = Instant.now()

  "lock" when {

    "applying new lock" should {

      "add new lock" in {

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", now, now)

        val result = repository.lock(lock1).futureValue

        result shouldBe true
      }

      "update lock when sessionId is the same as lock" in {

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", now, now.minus(1, DAYS))
        val lock2: Lock = Lock("session1", "eoriNumber", "lrn", now, now)

        insert(lock1).futureValue

        val lockResult = repository.lock(lock2).futureValue

        val numberOfDocs: Long = repository.collection.countDocuments().head().futureValue

        lockResult shouldBe true
        numberOfDocs shouldBe 1
      }

      "not add new lock if lock already exists and sessionId is different" in {

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", now, now)
        val lock2: Lock = Lock("session2", "eoriNumber", "lrn", now, now)

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

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", now, now)

        insert(lock1).futureValue

        val result = repository.findLocks(lock1.eoriNumber, lock1.lrn).futureValue

        result.value.sessionId shouldBe lock1.sessionId
        result.value.eoriNumber shouldBe lock1.eoriNumber
        result.value.lrn shouldBe lock1.lrn
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

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", now, now)

        insert(lock1).futureValue

        val result = repository.unlock(lock1.eoriNumber, lock1.lrn, "session1").futureValue

        val numberOfDocs: Long = repository.collection.countDocuments().head().futureValue

        result shouldBe true
        numberOfDocs shouldBe 0
      }
    }
  }
}
