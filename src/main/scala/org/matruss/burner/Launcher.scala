package org.matruss.burner

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import com.typesafe.config.ConfigFactory

import net.ceedubs.ficus.Ficus._
import scopt.OptionParser

import scala.concurrent.duration.FiniteDuration

/*
 1. DONE: Refactor Voter logic
 2. Unmarshalling from string to list of pictures
 3. DONE : Tests for RoadMap
 4. DONE : Comments and logic to explain
 5. Account for external failures in vote actors
 5. Test for Vote actor
 6. Access token to dropbox request
 7. If can test : path to pictures might be wrong in Vote actor
 8. Address overlooked todo's
 */
object Launcher extends App {
  
  import Defaults._

  private val zoo = ActorSystem("VoteMe", ConfigFactory.load() )
  private val log = Logging.getLogger(zoo, this)

  implicit val conf = zoo.settings.config.getConfig(Root)

  private val parser = new OptionParser[VotingAppParams]("VoteMe") {

    head("Voting app for burner")
    opt[Unit]('d', "verbose") optional() action { (_, c) => c.copy(verbose = true) } text("Verbose mode")
  }
  parser.parse(args, VotingAppParams() ) match {
    case Some(params) => {
      log.info("Parameters parsed, starting up actor system ...")

      zoo.actorOf( Props(classOf[HttpListenerActor] ), "listener" )
      log.info("HttpListener actor tracking Burner is up")

      zoo.actorOf( Props(classOf[VoteProcessingActor] ), "voter" )
      log.info("VoteProcessing actor is up")
    }
    case None => lastRites(1,"Failed to parse input parameters, exiting ..." )
  }

  private def lastRites(code:Int, message:String) {
    val TerminationTimeout =
      conf.as[Option[FiniteDuration]]("actors.termination_timeout").getOrElse(TerminationTimeoutDefault)

    // todo this way to terminate actor system is deprecated in 2.4, need to fix it
    zoo.shutdown()
    if (code > 0) log.error(message, Array(this.getClass.getSimpleName) )
    else log.info(message, Array(this.getClass.getSimpleName) )

    zoo.awaitTermination(TerminationTimeout)
    System.exit(code)
  }
}

case class VotingAppParams( verbose:Boolean = false )

object Defaults {

  import scala.concurrent.duration._

  // root hierarchy name in application.conf
  val Root = "burner"
  // waiting time for actor ask
  val AskTimeoutDefault = 10.seconds
  // waiting time for terminating all actors
  val TerminationTimeoutDefault = 2000.milliseconds
  // default HTTP server endpoint
  val InterfaceHost = "localhost"
  // default HTTP server port
  val InterfacePort = 10080
  // default root path for Visto REST interface
  val RootHttpPath = "burner"
  // waiting time for inital server binding
  val StartupTimeout = 5000.millisecond
  // dropbox request timeout
  val RequestTimeout = 5.seconds
  val DropboxHost = "api.dropboxapi.com"
  val DropboxPort = 443
  val DropboxPath = "/1/metadata/auto/Photos"
}

