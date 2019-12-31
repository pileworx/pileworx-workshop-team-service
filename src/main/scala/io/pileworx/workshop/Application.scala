package io.pileworx.workshop

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import io.pileworx.workshop.common.akka.AkkaImplicits

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Application extends App with AkkaImplicits {

  val routes = Route(null)
  val httpPort = if(sys.env.contains("HTTP_PORT")) sys.env("HTTP_PORT").asInstanceOf[Int] else 8080
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "0.0.0.0", httpPort)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println("Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}