package org.matruss.burner

import akka.actor.{ActorSelection, Actor}
import akka.actor.SupervisorStrategy.Restart
import akka.http.scaladsl.Http
import akka.stream.scaladsl.ImplicitMaterializer
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import net.ceedubs.ficus.Ficus._

import scala.util.control.NonFatal

/*
 I chose to define all the routing in a separate trait RoadMap. Reason is testability:
 this actor doesn't receive any messages from outside world except through the HTTP
 connections, all its behaviour is inside routing trait.
*/

class HttpListenerActor extends Actor with ImplicitMaterializer with RoadMap {

  import Defaults._

  implicit def conf = context.system.settings.config.getConfig(Root)
  implicit val system = context.system
  implicit val ec = context.dispatcher

  implicit val timeout = Timeout(
    conf.as[Option[FiniteDuration]]("actors.ask_timeout").getOrElse(AskTimeoutDefault)
  )

  import akka.event.Logging
  implicit def log = Logging(system, this)

  private[this] val interface = conf.as[Option[String]]("burner.server").getOrElse(InterfaceHost)
  private[this] val port = conf.as[Option[Int]]("burner.port").getOrElse(InterfacePort)
  private[this] val startupTimeout =
    conf.as[Option[FiniteDuration]]("burner.startup_timeout").getOrElse(StartupTimeout)

  protected val voter:ActorSelection = context.actorSelection("/user/voter")

  try {
    Await.result(Http().bindAndHandle(roads, interface, port), startupTimeout)
    log.info("Burner HTTP service started listening to interface {}, port {}", interface, port)
  }
  catch {
    case NonFatal(err) => {
      log.error("Received non-fatal error {}, restarting...", err.getMessage)
      Restart
    }
    case fatal: Throwable => {
      log.error("Fatal error {}, initiating system shutdown ...", fatal.getMessage)
      system.shutdown()
    }
  }
  // we're not explicetly receiving anything
  override def receive: Receive = Actor.emptyBehavior
}
