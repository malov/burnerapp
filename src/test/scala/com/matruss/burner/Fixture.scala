package com.matruss.burner

import akka.actor.{Status, Actor}
import org.matruss.burner.{FetchPictureList, StoreVote}

trait Fixture

class MockVoterActor extends Actor with Fixture {

  def receive:Receive = {
    case StoreVote(pic, flag) => flag match {
      case false => sender() ! Status.Success("OK")
      case true => sender() ! Status.Failure(new RuntimeException("Failed to deal with picture"))
    }
    case FetchPictureList(flag) => flag match {
      case false => sender() ! Status.Success("OK")
      case true => sender() ! Status.Failure(new RuntimeException("Failed to fetch data from Dropbox"))
    }
    case x => sender() ! Status.Failure( new RuntimeException("Unknown message"))
  }
}

class MockListenerActor extends Actor with Fixture {

  def receive:Receive = {
    case x =>
  }
}