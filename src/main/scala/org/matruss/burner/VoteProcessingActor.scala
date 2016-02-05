package org.matruss.burner

import akka.actor.Actor
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

class VoteProcessingActor extends Actor {

  def receive:Receive = {
    case StoreVote(pic) => ??? // todo store pictures in a cache
    case FetchPictureList() => ??? // todo call Dropbox endpoint and get picture list
  }
}

sealed trait PictureRequests

case class FetchPictureList() extends PictureRequests
case class StoreVote(picture:String) extends PictureRequests

case class Picture(name:String, vote:Int)
case class PictureSeq(seq:Seq[Picture])

object PictureJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val clientFormat = jsonFormat2(Picture)
}