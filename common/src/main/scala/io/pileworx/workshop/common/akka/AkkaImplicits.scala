package io.pileworx.workshop.common.akka

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

trait AkkaImplicits {
  implicit val system: ActorSystem = ActorSystem("workshop-actor-system")
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(30 seconds)
  val config: Config = ConfigFactory.load()
}