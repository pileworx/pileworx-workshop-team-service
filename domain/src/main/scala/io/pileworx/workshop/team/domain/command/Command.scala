package io.pileworx.workshop.team.domain.command

import akka.actor.typed.ActorRef
import io.pileworx.workshop.team.common.akka.serializable.Cbor

trait Command extends Cbor
trait CommandWithReply[Reply <: CommandReply] extends Command {
  def replyTo: ActorRef[Reply]
}

trait CommandReply extends Cbor
trait OperationResult extends CommandReply
case object Confirmed extends OperationResult
final case class Rejected(reason: String) extends OperationResult