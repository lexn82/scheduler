package com.pagerduty.scheduler.dao

import com.pagerduty.eris.serializers._
import java.time.{ LocalDateTime, ZoneOffset }
import java.util.Date

object TimeSerializer {
  def toDate(ldt: LocalDateTime): Date = Date.from(ldt.toInstant(ZoneOffset.UTC))
  def toLocalDateTime(d: Date): LocalDateTime = LocalDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC)
}

class TimeSerializer extends ProxySerializer[LocalDateTime, Date](
  toRepresentation = TimeSerializer.toDate(_),
  fromRepresentation = TimeSerializer.toLocalDateTime(_),
  DateSerializer
)
