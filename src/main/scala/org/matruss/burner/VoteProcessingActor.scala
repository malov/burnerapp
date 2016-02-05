package org.matruss.burner

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{ImplicitMaterializer, Sink, Source}
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future

class VoteProcessingActor extends Actor with ImplicitMaterializer {

  implicit val system = context.system
  implicit val ec = context.dispatcher

  lazy val dpConnection = Http().outgoingConnection("dropbox",443)

  private[this] def fetchFromDropbox:Future[String] = {
    def dpRequest(req:HttpRequest):Future[HttpResponse] =
      Source.single(req).via(dpConnection).runWith(Sink.head)

    dpRequest( RequestBuilding.Get("/path") ) flatMap { rsp =>
      rsp.status match {
        case OK => Unmarshal(rsp.entity).to[String]
        case _ => Future.failed(new RuntimeException("Error"))
      }
    }
  }

  private[this] def fetchFromFile:Future[PictureListWithVotes] = {
    import scala.io.Source
    import scala.collection.mutable.Map

    Future {
      val cache = Map[String,Int]()
      for ( line <- Source.fromFile("picture_cache.txt").getLines ) {
        val rec = line.split(',')
        cache += (rec.head -> rec.tail.head.toInt )
      }
      PictureListWithVotes(cache.toMap)
    }
  }

  lazy private[this] val cache:Map[String,Int] = {
    null
  }

  def receive:Receive = {
    case StoreVote(pic) => {
      if ( cache.contains(pic) ) {
        val counter = cache.get(pic).get + 1
        cache + (pic -> counter)
      }
    }
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

case class PictureList(names:Seq[String])

object PictureListJson
case class PictureListWithVotes(ratings:Map[String,Int])