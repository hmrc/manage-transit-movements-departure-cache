package models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

final case class Lock(
                       _id: String,
                       eoriNumber: String,
                       lrn: String,
                       createdAt: LocalDateTime
                     )

object Lock {

  implicit lazy val format: OFormat[Lock] = Json.format[Lock]
}
