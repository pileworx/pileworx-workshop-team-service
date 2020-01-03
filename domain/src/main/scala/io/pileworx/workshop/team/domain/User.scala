package io.pileworx.workshop.team.domain

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import io.pileworx.workshop.team.common.akka.serializable.Cbor
import io.pileworx.workshop.team.domain.command.{Confirmed, CreateUser, Rejected, UpdateUser, UserCmd}
import io.pileworx.workshop.team.domain.event.{UserCreated, UserEvent, UserUpdated}

object User {

  type ReplyEffect = akka.persistence.typed.scaladsl.ReplyEffect[UserEvent, State]

  val TypeKey: EntityTypeKey[UserCmd[_]] = EntityTypeKey[UserCmd[_]]("User")

  def apply(id: String, eventProcessorTags: Set[String]): Behavior[UserCmd[_]] = {
    EventSourcedBehavior.withEnforcedReplies[UserCmd[_], UserEvent, State](
      PersistenceId(TypeKey.name, id),
      State.empty,
      (state, cmd) => applyCommand(id, state, cmd),
      (state, event) => applyEvent(state, event))
      .withTagger(_ => eventProcessorTags)
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 2))
  }

  def applyCommand(id: String, state: State, cmd: UserCmd[_]): ReplyEffect = cmd match {
    case create: CreateUser =>
      if(state.isInstanceOf[EmptyState])
        Effect.persist(UserCreated(create)).thenReply(create.replyTo)(_ => Confirmed)
      else
        Effect.unhandled.thenReply(create.replyTo)(_ => Rejected(s"User $id already exists."))
    case update: UpdateUser => Effect.persist(UserUpdated(update)).thenReply(update.replyTo)(_ => Confirmed)
    case _ => Effect.unhandled.thenNoReply()
  }

  def applyEvent(state: State, event: UserEvent): State = event match {
    case created: UserCreated => State.create(created)
    case updated: UserUpdated => state.update(updated)
  }

  private[User] trait State extends Cbor {
    def update(e: UserUpdated): State
  }

  private[User] case class EmptyState() extends State {
    def update(e: UserUpdated): State = ???
  }

  private[User] case class InitializedState(
    username: String,
    email: String,
    name: Name) extends State {

    def update(e: UserUpdated): State = copy(
      email = e.email,
      name = Name(e.firstName, e.lastName, e.middleName))
  }

  private[User] object State {
    def empty: State = EmptyState()
    def create(created: UserCreated): State = {
      InitializedState(
        created.username,
        created.email,
        Name(
          created.firstName,
          created.lastName,
          created.middleName))
    }
  }

  private[User] case class Name(first: String, last: String, middle: Option[String] = None)
}