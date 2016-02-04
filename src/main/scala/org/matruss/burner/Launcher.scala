package org.matruss.burner

import scopt.OptionParser

object Launcher extends App {
  
  private val parser = new OptionParser[VotingAppParams]("VotingApp") {

    head("Voting app for burner")
    opt[Unit]('d', "verbose") optional() action { (_, c) => c.copy(verbose = true) } text("Verbose mode")
  }
  parser.parse(args, VotingAppParams() ) match {
    case Some(params) => ???
    case None => lastRites(1,"Failed to parse input parameters, exiting ..." )
  }

  private def lastRites(code:Int, message:String) {
    System.out.println(message)
    System.exit(code)
  }
}

case class VotingAppParams( verbose:Boolean = false )

