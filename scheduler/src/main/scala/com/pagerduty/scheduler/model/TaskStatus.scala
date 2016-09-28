package com.pagerduty.scheduler.model

import java.time.LocalDateTime
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.{ read, write }

/**
 * Task status.
 */
case class TaskStatus(
    numberOfAttempts: Int,
    completionResult: CompletionResult,
    nextAttemptAt: Option[LocalDateTime]
) {
  def isComplete = completionResult.isComplete

  implicit val formats = DefaultFormats + new TaskKeyTimeSerializer + new CompletionResultSerializer

  def toJson: String = write(this)
}

object TaskStatus {
  val Dropped: TaskStatus = {
    TaskStatus(numberOfAttempts = 0, CompletionResult.Dropped, nextAttemptAt = None)
  }
  val NeverAttempted: TaskStatus = {
    TaskStatus(numberOfAttempts = 0, CompletionResult.Incomplete, nextAttemptAt = None)
  }
  def successful(numberOfAttempts: Int): TaskStatus = {
    TaskStatus(numberOfAttempts, CompletionResult.Success, nextAttemptAt = None)
  }
  def failed(numberOfAttempts: Int): TaskStatus = {
    TaskStatus(numberOfAttempts, CompletionResult.Failure, nextAttemptAt = None)
  }

  implicit val formats = DefaultFormats + new TaskKeyTimeSerializer + new CompletionResultSerializer

  def fromJson(taskStatus: String): TaskStatus = read[TaskStatus](taskStatus)
}
