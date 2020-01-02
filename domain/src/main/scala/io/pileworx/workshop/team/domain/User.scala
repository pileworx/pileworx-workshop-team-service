package io.pileworx.workshop.team.domain

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, Effect}
import io.pileworx.workshop.team.common.akka.serializable.Cbor
import io.pileworx.workshop.team.domain.User.ReplyEffect
import io.pileworx.workshop.team.domain.command.{Confirmed, CreateUser, Rejected, UpdateUser, UserCmd}
import io.pileworx.workshop.team.domain.event.{UserCreated, UserEvent, UserUpdated}

case class User(
  username: String,
  email: String,
  name: Name
) extends Cbor {
  def applyCommand(cmd: UserCmd[_]): ReplyEffect = cmd match {
    case create: CreateUser => Effect.reply(create.replyTo)(Rejected("User already exists."))
    case update: UpdateUser => Effect.persist(UserUpdated(update)).thenReply(update.replyTo)(_ => Confirmed)
  }

  def applyEvent(event: UserEvent): User = event match {
    case created: UserCreated => throw new IllegalStateException(s"Unexpected event [$created] in state [User]")
    case updated: UserUpdated => update(updated)
  }

  private def update(e: UserUpdated) = copy(
    email = e.email,
    name = Name(e.firstName, e.lastName, e.middleName))
}

object User {

  type ReplyEffect = akka.persistence.typed.scaladsl.ReplyEffect[UserEvent, Option[User]]

  val TypeKey: EntityTypeKey[UserCmd[_]] = EntityTypeKey[UserCmd[_]]("User")

  def apply(persistenceId: PersistenceId): Behavior[UserCmd[_]] = {
    EventSourcedBehavior.withEnforcedReplies[UserCmd[_], UserEvent, Option[User]](
      persistenceId,
      None,
      (state, cmd) => state match {
        case None => onFirstCommand(cmd)
        case Some(user) => user.applyCommand(cmd)
      },
      (state, event) => state match {
        case None => Some(onFirstEvent(event))
        case Some(user) => Some(user.applyEvent(event))
      }
    )
  }

  def onFirstCommand(cmd: UserCmd[_]): ReplyEffect = cmd match {
    case create: CreateUser => Effect.persist(UserCreated(create)).thenReply(create.replyTo)(_ => Confirmed)
    case _ => Effect.unhandled.thenNoReply()
  }

  def onFirstEvent(event: UserEvent): User = event match {
    case created: UserCreated => create(created)
    case _ => throw new IllegalStateException(s"unexpected event [$event] in state [NewUser]")
  }

  private def create(created: UserCreated): User = {
    User(
      created.username,
      created.email,
      Name(
        created.firstName,
        created.lastName,
        created.middleName))
  }
}

case class Name(first: String, last: String, middle: Option[String] = None)