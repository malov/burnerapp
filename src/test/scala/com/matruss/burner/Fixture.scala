package com.matruss.burner

import akka.actor.{ActorSystem, Status, Actor}
import com.typesafe.config.ConfigFactory
import org.matruss.burner._

trait Fixture

class MockVoterActor extends Actor with Fixture {

  def receive:Receive = {
    case StoreVote(pic, flag) => flag match {
      case false => sender() ! Status.Success("OK")
      case true => sender() ! Status.Failure(new RuntimeException("Failed to deal with picture"))
    }
    case FetchPictureList(flag) => flag match {
      case false => sender() ! Fixture.pseq
      case true => sender() ! Status.Failure(new RuntimeException("Failed to fetch data from Dropbox"))
    }
    case x => sender() ! Status.Failure( new RuntimeException("Unknown message"))
  }
}

object MockActorSystem  {
  def apply(name:String):ActorSystem = ActorSystem(name, ConfigFactory.load() )
}

object Fixture {
  val pseq = {
    val seq = Seq( Picture("my.jpeg", 100) )
    PictureSeq(seq)
  }
}