package com.pagerduty.scheduler.model

import java.time.LocalDateTime
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.{ read, write }

/**
 * Captures results of a task attempt for troubleshooting.
 */
case class TaskAttempt(
    attemptNumber: Int,
    startedAt: LocalDateTime,
    finishedAt: LocalDateTime,
    taskResult: CompletionResult,
    taskResultUpdatedAt: LocalDateTime,
    exceptionClass: Option[String],
    exceptionMessage: Option[String],
    exceptionStackTrace: Option[String]
) {
  implicit val formats = DefaultFormats + new TaskKeyTimeSerializer + new CompletionResultSerializer

  def toJson: String = write(this)
}

object TaskAttempt {
  def apply(
    attemptNumber: Int,
    startedAt: LocalDateTime,
    finishedAt: LocalDateTime,
    taskResult: CompletionResult,
    taskResultUpdatedAt: LocalDateTime,
    exception: Option[Throwable]
  ): TaskAttempt = {
    TaskAttempt(
      attemptNumber,
      startedAt,
      finishedAt,
      taskResult,
      taskResultUpdatedAt,
      exceptionClass = exception.map(_.getClass.getName),
      exceptionMessage = exception.map(_.getMessage),
      exceptionStackTrace = exception.map(_.getStackTraceString)
    )
  }

  implicit val formats = DefaultFormats + new TaskKeyTimeSerializer + new CompletionResultSerializer

  def fromJson(taskAttempt: String): TaskAttempt = read[TaskAttempt](taskAttempt)
}
