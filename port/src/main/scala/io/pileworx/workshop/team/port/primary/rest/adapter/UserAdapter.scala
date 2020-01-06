package io.pileworx.workshop.team.port.primary.rest.adapter

import akka.actor.typed.ActorRef
import io.pileworx.workshop.team.domain.command.{CreateUser, OperationResult}
import io.pileworx.workshop.team.port.primary.rest.CreateUserRest

object UserAdapter {
  def toCmd(rCmd: CreateUserRest, replyTo: ActorRef[OperationResult]): CreateUser = {
    CreateUser(rCmd.username, rCmd.email, rCmd.firstName, rCmd.middleName, rCmd.lastName, replyTo)
  }
}
