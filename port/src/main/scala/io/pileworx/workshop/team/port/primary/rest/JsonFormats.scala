package io.pileworx.workshop.team.port.primary.rest

import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._

trait JsonFormats {
  implicit val createUserFormat: RootJsonFormat[CreateUserRest] = jsonFormat5(CreateUserRest)
}