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

package services

import base.SpecBase
import config.AppConfig
import org.mockito.Mockito.when

import java.time.temporal.ChronoUnit.DAYS
import java.time.{Clock, Instant}

class DateTimeServiceSpec extends SpecBase {

  private val fakeClock  = Clock.systemUTC()
  private val mockConfig = mock[AppConfig]

  private val service = new DateTimeService(fakeClock, mockConfig)

  "expiresInDays" should {

    "return correct days for a date today" in {
      when(mockConfig.mongoTtlInDays).thenReturn(30)
      service.expiresInDays(Instant.now()) shouldEqual 30L
    }

    "return correct days for a date 5 days ago" in {
      when(mockConfig.mongoTtlInDays).thenReturn(30)
      service.expiresInDays(Instant.now().minus(5, DAYS)) shouldEqual 25L
    }
  }

}
