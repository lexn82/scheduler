package com.pagerduty.scheduler.specutil

import com.pagerduty.eris.TimeUuid
import com.pagerduty.scheduler.datetimehelpers._
import com.pagerduty.scheduler.model.{ Task, TaskKey }
import java.time.{ LocalDateTime, ZoneOffset }
import java.time.temporal.ChronoUnit
import scala.concurrent.duration._

object TaskFactory {
  private def timeNowFlooredToMs: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS)

  def makeTask(scheduledTime: LocalDateTime = timeNowFlooredToMs): Task = {
    // for reasons unknown, various tests are dependent on TimeUuid being used here :-(
    Task(
      orderingId = TimeUuid().toString,
      scheduledTime = scheduledTime,
      uniquenessKey = TimeUuid().toString,
      taskData = Map("taskId" -> TimeUuid().toString)
    )
  }

  def makeTaskKey(scheduledTime: LocalDateTime = timeNowFlooredToMs): TaskKey = {
    makeTask(scheduledTime).taskKey
  }

  def makeTasks(
    count: Int,
    scheduledTime: LocalDateTime = timeNowFlooredToMs,
    spacing: Duration = 0.seconds
  ): IndexedSeq[Task] = {
    for (i <- 0 until count) yield TaskFactory.makeTask(scheduledTime + (spacing * i))
  }

  def makeTasksInConsecutiveBuckets(
    rowTimeBucketDuration: Duration
  ): (Seq[Task], Seq[Task]) = {
    val currentTimeBucket = LocalDateTime.now(ZoneOffset.UTC)
    val nextTimeBucket = currentTimeBucket + rowTimeBucketDuration
    val tasks = for (i <- 0 to 5) yield TaskFactory.makeTask()
    val currentBucketTasks = tasks.map(_.copy(scheduledTime = currentTimeBucket))
    val nextBucketTasks = tasks.map(_.copy(scheduledTime = nextTimeBucket))
    (currentBucketTasks, nextBucketTasks)
  }
}
