package org.matruss.burner

import akka.actor.Actor
import akka.actor.SupervisorStrategy.Restart
import akka.http.scaladsl.Http
import akka.stream.scaladsl.ImplicitMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server._

import scala.concurrent.Await
import scala.concurrent.duration._
import net.ceedubs.ficus.Ficus._

import scala.util.control.NonFatal

class HttpListenerActor(val roads:Route) extends Actor with ImplicitMaterializer {

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
