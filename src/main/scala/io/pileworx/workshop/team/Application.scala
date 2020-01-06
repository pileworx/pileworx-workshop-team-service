package io.pileworx.workshop.team

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import io.pileworx.workshop.team.domain.User
import io.pileworx.workshop.team.port.primary.rest.{RestServer, UserRestPort}

object Application extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem[Nothing](Guardian(), config.getString("pileworx.application.name"), config)
}

object Guardian {
  def apply(): Behavior[Nothing] = {
    Behaviors.setup[Nothing] { context =>
      val system = context.system
      val httpPort = system.settings.config.getInt("pileworx.http.port")

      User.init(system)

      val userRestPort = new UserRestPort()(context.system)
      new RestServer(userRestPort.userRoutes, httpPort, context.system, system.settings.config).start()

      Behaviors.empty
    }
  }
}