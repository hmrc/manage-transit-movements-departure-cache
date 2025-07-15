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

package controllers

import itbase.RepositorySpecBase
import repositories.{CacheRepository, LockRepository}
import uk.gov.hmrc.mongo.test.CleanMongoCollectionSupport

class SessionControllerSpec extends RepositorySpecBase with CleanMongoCollectionSupport {

  private val cacheRepository = app.injector.instanceOf[CacheRepository]
  private val lockRepository  = app.injector.instanceOf[LockRepository]

  "DELETE /user-answers/:lrn" when {

    val url = s"$baseUrl/manage-transit-movements-departure-cache/user-answers/$lrn"

    "document and locks exist" should {
      "remove document and locks and respond with 200 status" in {
        cacheRepository.set(emptyMetadata, None).futureValue
        lockRepository.lock("session1", eoriNumber, lrn).futureValue
        lockRepository.lock("session2", eoriNumber, lrn).futureValue

        val response = wsClient
          .url(url)
          .delete()
          .futureValue

        response.status shouldEqual 200

        cacheRepository.get(lrn, eoriNumber).futureValue shouldEqual None
        lockRepository.findLocks(eoriNumber, lrn).futureValue shouldEqual None
      }
    }

    "document exists and no locks exist" should {
      "remove document and respond with 200 status" in {
        cacheRepository.set(emptyMetadata, None).futureValue

        val response = wsClient
          .url(url)
          .delete()
          .futureValue

        response.status shouldEqual 200

        cacheRepository.get(lrn, eoriNumber).futureValue shouldEqual None
        lockRepository.findLocks(eoriNumber, lrn).futureValue shouldEqual None
      }
    }

    "document does not exist" should {
      "respond with 200 status" in {
        val response = wsClient
          .url(url)
          .delete()
          .futureValue

        response.status shouldEqual 200
      }
    }
  }
}
