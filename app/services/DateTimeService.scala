/*
 * Copyright 2024 HM Revenue & Customs
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

import config.AppConfig

import java.time.temporal.ChronoUnit.DAYS
import java.time.{Clock, Duration, Instant, LocalDateTime}
import javax.inject.Inject

class DateTimeService @Inject() (
  clock: Clock,
  config: AppConfig
) {

  def now: LocalDateTime = LocalDateTime.now(clock)

  def timestamp: Instant = Instant.now(clock)

  def expiresInDays(createdAt: Instant): Long =
    Duration.between(timestamp, createdAt.plus(config.mongoTtlInDays, DAYS)).toDays + 1
}
