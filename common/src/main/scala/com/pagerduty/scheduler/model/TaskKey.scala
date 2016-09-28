package com.pagerduty.scheduler.model

import com.pagerduty.scheduler.Partitioner
import com.pagerduty.scheduler.model.Task.PartitionId
import java.time.format.DateTimeFormatter
import java.time.{ LocalDateTime, ZoneOffset }
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

case class TaskKey(
    scheduledTime: LocalDateTime,
    orderingId: Task.OrderingId,
    uniquenessKey: Task.UniquenessKey
) extends Ordered[TaskKey] {
  def asTuple: (LocalDateTime, Task.OrderingId, Task.UniquenessKey) = TaskKey.unapply(this).get

  def compare(that: TaskKey): Int = {
    val firstComparison = this.asTuple._1.compareTo(that.asTuple._1)
    if (firstComparison != 0) return firstComparison
    val secondComparison = this.asTuple._2.compare(that.asTuple._2)
    if (secondComparison != 0) return secondComparison
    val thirdComparison = this.asTuple._3.compare(that.asTuple._3)
    if (thirdComparison != 0) return thirdComparison
    0
  }

  override def toString: String = {
    val formattedTimeString = TaskKey.TimeFormat.format(scheduledTime)
    s"TaskKey($formattedTimeString,$orderingId,$uniquenessKey)"
  }

  /**
   * This method calculates a Kafka partitionId for the task key. It is a direct copy of the
   * partitioning logic found in org.apache.kafka.clients.producer.internals.DefaultPartitioner.
   */
  def partitionId(numPartitions: Int): PartitionId = {
    val partitionKeyBytes = orderingId.getBytes("UTF8")

    Partitioner.partitionId(partitionKeyBytes, numPartitions)
  }
}

object TaskKey {
  val ScheduledTimeFormat = "yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'"
  private[scheduler] val TimeFormat = DateTimeFormatter.ofPattern(ScheduledTimeFormat)

  def apply(
    formattedScheduledTime: String,
    orderingId: Task.OrderingId,
    uniquenessKey: Task.UniquenessKey
  ): TaskKey = {
    TaskKey(LocalDateTime.parse(formattedScheduledTime, TimeFormat), orderingId, uniquenessKey)
  }

  def fromString(key: String): TaskKey = {
    val keyString = "TaskKey\\((.+)\\)".r.unapplySeq(key).get.head
    val Array(time, orderingId, uniquenessKey) = keyString.split(",")
    TaskKey(time, orderingId, uniquenessKey)
  }

  def lowerBound(
    scheduledTime: LocalDateTime,
    orderingId: Option[Task.OrderingId] = None,
    uniquenessKey: Option[Task.UniquenessKey] = None
  ): TaskKey = {
    val oId = orderingId.getOrElse("")
    val uKey = uniquenessKey.getOrElse("")
    TaskKey(scheduledTime, oId, uKey)
  }
}

class TaskKeyTimeSerializer extends CustomSerializer[LocalDateTime](format => ({
  case JString(s) => LocalDateTime.parse(s, TaskKey.TimeFormat)
}, {
  case t: LocalDateTime => JString(t.format(TaskKey.TimeFormat))
}))
