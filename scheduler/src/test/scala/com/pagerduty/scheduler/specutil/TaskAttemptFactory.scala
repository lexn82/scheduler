package com.pagerduty.scheduler.specutil

import com.pagerduty.scheduler.model._
import com.twitter.conversions.time._
import com.twitter.util.Time

object TaskAttemptFactory {

  def makeTaskAttempt(attemptNumber: Int, taskResult: CompletionResult) = {
    val now = Time.now.floor(1.millisecond)
    val hadException = taskResult match {
      case CompletionResult.Success => false
      case _ => true
    }
    TaskAttempt(
      attemptNumber,
      startedAt = now - 100.millis,
      finishedAt = now - 50.millis,
      taskResult,
      taskResultUpdatedAt = now,
      exceptionClass = if (hadException) Some("YourTaskFailedException") else None,
      exceptionMessage = if (hadException) Some("Something went wrong.") else None,
      exceptionStackTrace = if (hadException) Some("com.pagerduty.app.YourTask.run") else None
    )
  }
}
