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

class LockRepositorySpec extends LockRepositorySpecBase {

  val now: Instant = Instant.now()

  "lock" when {

    "applying new lock" should {

      "add new lock" in {

        val result = repository.lock("session1", "eoriNumber", "lrn").futureValue

        result shouldEqual true
      }

      "update lock when sessionId is the same as lock" in {

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", now, now)

        insert(lock1).futureValue

        val lockResult = repository.lock("session1", "eoriNumber", "lrn").futureValue

        val numberOfDocs: Long = repository.collection.countDocuments().head().futureValue

        lockResult shouldEqual true
        numberOfDocs shouldEqual 1

        val lock2 = findAll().futureValue.head
        lock2.lastUpdated.isAfter(lock1.lastUpdated) shouldEqual true
      }

      "not add new lock if lock already exists and sessionId is different" in {

        val lock1: Lock = Lock("session1", "eoriNumber", "lrn", now, now)

        insert(lock1).futureValue

        val result = repository.lock("session2", "eoriNumber", "lrn").futureValue

        val numberOfDocs: Long = repository.collection.countDocuments().head().futureValue

        result shouldEqual false
        numberOfDocs shouldEqual 1

        findAll().futureValue.head.sessionId shouldEqual "session1"
      }
    }
  }

  "unlock" when {

    val lrn        = "lrn"
    val eoriNumber = "eoriNumber"

    "when unlocking a document" should {

      "return true for successful unlock" in {

        val sessionId = "session1"

        val lock1: Lock = Lock(sessionId, eoriNumber, lrn, now, now)

        insert(lock1).futureValue

        val result = repository.unlock(eoriNumber, lrn, sessionId).futureValue

        val numberOfDocs: Long = repository.collection.countDocuments().head().futureValue

        result shouldEqual true
        numberOfDocs shouldEqual 0
      }
    }

    "when unlocking multiple documents" should {

      "return true for successful unlock" in {

        val lock1: Lock = Lock("session1", eoriNumber, lrn, now, now)
        val lock2: Lock = Lock("session2", eoriNumber, lrn, now, now)

        insert(lock1).futureValue
        insert(lock2).futureValue

        val result = repository.unlock(lock1.eoriNumber, lock1.lrn).futureValue

        val numberOfDocs: Long = repository.collection.countDocuments().head().futureValue

        result shouldEqual true
        numberOfDocs shouldEqual 0
      }
    }
  }
}
