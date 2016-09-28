package com.pagerduty.scheduler.admin.http

import com.pagerduty.scheduler.admin.model.AdminTask
import com.pagerduty.scheduler.model.{ CompletionResult, TaskKey }
import java.time.LocalDateTime
import org.json4s.{ CustomSerializer, MappingException }
import org.json4s.JsonAST.JString

case class PutTaskRequest(task: AdminTask)

case class PutTaskResponse(task: Option[AdminTask], errors: Seq[String])

class CompletionStatusSerializer extends CustomSerializer[CompletionResult](format => (
  {
    case JString(status) =>
      lazy val error = s"Can't convert '$status' to CompletionStatus"
      CompletionResult.fromString(status).getOrElse(throw new MappingException(error))
  },
  {
    case cs: CompletionResult => JString(cs.toString)
  }
))

class TimeSerializer extends CustomSerializer[LocalDateTime](format => (
  {
    case JString(timeString) =>
      LocalDateTime.parse(timeString, AdminServlet.TimeFormat)
  },
  {
    case t: LocalDateTime => JString(t.format(AdminServlet.TimeFormat))
  }
))

class TaskKeySerializer extends CustomSerializer[TaskKey](format => (
  {
    case JString(keyString) =>
      TaskKey.fromString(keyString)
  },
  {
    case key: TaskKey => JString(key.toString)
  }
))
