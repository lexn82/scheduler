package com.pagerduty.scheduler.model

import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.{ LocalDateTime, ZoneOffset }
import java.util.UUID
import org.scalatest.{ Matchers, WordSpecLike }

class TaskKeySpec extends WordSpecLike with Matchers {
  "TaskKey" should {

    "parse from a string" in {
      val taskKey = TaskKey(LocalDateTime.now(ZoneOffset.UTC), UUID.randomUUID().toString, UUID.randomUUID.toString)
      val taskKeyString = taskKey.toString()

      val parsedTaskKey = TaskKey.fromString(taskKeyString)

      parsedTaskKey should equal(taskKey)
    }

    "parse from a string with all sorts of weird characters (except commas)" in {
      val taskKey = TaskKey(LocalDateTime.now(ZoneOffset.UTC), "{}()-_=+!@#$%^&*:\"|\\:;./<>?~", "|\\:;./<>?~'")
      val formattedTimeString = TaskKey.TimeFormat.format(taskKey.scheduledTime)
      val string = s"TaskKey($formattedTimeString,${taskKey.orderingId},${taskKey.uniquenessKey})"

      val parsedTaskKey = TaskKey.fromString(string)

      parsedTaskKey should equal(taskKey)
    }

    def parseTaskFromTime(timeStr: String) = {
      val taskStr = s"TaskKey($timeStr,OrderingId,UniquenessKey)"
      TaskKey.fromString(taskStr)
    }

    "parse several varieties of time strings" in {
      val now = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS)

      val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      val parsedTaskKey1 = parseTaskFromTime(now.format(formatter1))
      parsedTaskKey1.scheduledTime.compareTo(now) should equal(0)

      val formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val parsedTaskKey2 = parseTaskFromTime(now.format(formatter2))
      parsedTaskKey2.scheduledTime.compareTo(now.truncatedTo(ChronoUnit.SECONDS)) should equal(0)
    }

    "have somewhat evenly distributed partition IDs within the correct range" in {
      val numKeys = 100000

      val numPartitions = 64

      var partitionCounts = Map[Int, Int]()

      (0 until numKeys) foreach { n =>
        val key = TaskKey(LocalDateTime.now(ZoneOffset.UTC), UUID.randomUUID().toString, UUID.randomUUID.toString)
        val id = key.partitionId(numPartitions)
        id should be >= 0
        id should be < numPartitions
        partitionCounts = if (partitionCounts.contains(id)) {
          val currentCount = partitionCounts(id)
          partitionCounts + (id -> (currentCount + 1))
        } else {
          partitionCounts + (id -> 1)
        }
      }

      val expectedAvgCount = numKeys / numPartitions
      val acceptableDev = expectedAvgCount / 3

      partitionCounts foreach {
        case (_, count) =>
          // this check could fail with a very low probability, if it does, adjust acceptableDev
          count should be((numKeys / numPartitions) +- acceptableDev)
      }
    }
  }
}
