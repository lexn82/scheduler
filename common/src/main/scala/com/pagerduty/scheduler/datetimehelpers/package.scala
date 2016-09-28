package com.pagerduty.scheduler

import java.time.{ LocalDateTime, ZoneOffset }
import java.time.temporal.ChronoField
import scala.concurrent.duration._

package object datetimehelpers {

  implicit class LocalDateTimeExt(ldt: LocalDateTime) {
    def inHours: Int = (ldt.toEpochSecond(ZoneOffset.UTC) / 3600).toInt

    def +(duration: Duration): LocalDateTime = ldt.plusNanos(duration.toNanos)
    def -(duration: Duration): LocalDateTime = ldt.minusNanos(duration.toNanos)
  }

  implicit class JavaDurationExt(d: java.time.Duration) {
    def toScalaDuration: FiniteDuration = Duration.fromNanos(d.toNanos)
  }
}
