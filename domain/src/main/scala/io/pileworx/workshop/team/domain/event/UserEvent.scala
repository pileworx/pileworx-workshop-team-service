package io.pileworx.workshop.team.domain.event

import io.pileworx.workshop.team.domain.command.{CreateUser, UpdateUser}

trait UserEvent extends Event

final case class UserCreated(
  username: String,
  email: String,
  firstName: String,
  middleName: Option[String],
  lastName: String) extends UserEvent

object UserCreated {
  def apply(cmd: CreateUser): UserCreated = new UserCreated(
    cmd.username,
    cmd.email,
    cmd.firstName,
    cmd.middleName,
    cmd.lastName)
}

final case class UserUpdated(
  email: String,
  firstName: String,
  middleName: Option[String],
  lastName: String) extends UserEvent

object UserUpdated {
  def apply(cmd: UpdateUser): UserUpdated = new UserUpdated(
    cmd.email,
    cmd.firstName,
    cmd.middleName,
    cmd.lastName)
}