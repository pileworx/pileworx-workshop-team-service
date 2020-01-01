package io.pileworx.workshop.team.domain.command

import akka.actor.typed.ActorRef

trait UserCmd[Reply <: CommandReply] extends CommandWithReply[Reply]

final case class CreateUser(
  username: String,
  email: String,
  firstName: String,
  middleName: Option[String],
  lastName: String,
  replyTo: ActorRef[OperationResult]) extends UserCmd[OperationResult]

final case class UpdateUser(
  email: String,
  firstName: String,
  middleName: Option[String],
  lastName: String,
  replyTo: ActorRef[OperationResult]) extends UserCmd[OperationResult]