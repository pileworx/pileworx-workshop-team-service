package io.pileworx.workshop.team.port.primary.rest

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.pileworx.workshop.team.domain.User
import io.pileworx.workshop.team.domain.command.{OperationResult, Rejected}
import io.pileworx.workshop.team.port.primary.rest.adapter.UserAdapter

import scala.concurrent.Future

class UserRestPort()(implicit system: ActorSystem[_]) extends JsonFormats {
  implicit private val timeout: Timeout = Timeout.create(system.settings.config.getDuration("akka.actor.askTimeout"))
  private val sharding = ClusterSharding(system)

  val userRoutes: Route = pathPrefix("users") {
    concat(
      post {
        entity(as[CreateUserRest]) { rCmd =>
          val entityRef = sharding.entityRefFor(User.TypeKey, rCmd.username)
          val reply: Future[OperationResult] = entityRef ? (UserAdapter.toCmd(rCmd, _))
          onSuccess(reply) {
            case Rejected(reason) => complete(BadRequest, reason)
            case _ =>complete(OK)
          }
        }
      }
    )
  }
}

final case class CreateUserRest(
  username: String,
  email: String,
  firstName: String,
  middleName: Option[String],
  lastName: String)

final case class UpdateUserRest(
  email: String,
  firstName: String,
  middleName: Option[String],
  lastName: String)
