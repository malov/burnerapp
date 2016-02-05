package org.matruss.burner

import akka.actor.{Status, Actor}
import akka.pattern.pipe

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{ImplicitMaterializer, Sink, Source}

import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}

class VoteProcessingActor extends Actor with ImplicitMaterializer {

  import Defaults._

  implicit def conf = context.system.settings.config.getConfig(Root)
  implicit val system = context.system
  implicit val ec = context.dispatcher

  private[this] val dpHost =
    conf.as[Option[String]]("dropbox.server").getOrElse(DropboxHost)
  private[this] val dpPort =
    conf.as[Option[Int]]("dropbox.port").getOrElse(DropboxPort)
  private[this] val dpPath =
    conf.as[Option[String]]("dropbox.path").getOrElse(DropboxPath)

  lazy val dpConnection = Http().outgoingConnection(dpHost,dpPort)

  private[this] def fetchFromDropbox:Future[String] = {
    def dpRequest(req:HttpRequest):Future[HttpResponse] =
      Source.single(req).via(dpConnection).runWith(Sink.head)

    dpRequest( RequestBuilding.Get(dpPath) ) flatMap { rsp =>
      rsp.status match {
        case OK => Unmarshal(rsp.entity).to[String]
        case _ => Future.failed(new RuntimeException("Error"))
      }
    }
  }

  private[this] val requestTimeout =
    conf.as[Option[FiniteDuration]]("burner.startup_timeout").getOrElse(RequestTimeout)

  private[this] def sync:Map[String,Int] = {
    val update = cache
    val fromDropbox = Await.result(fetchFromDropbox, requestTimeout)
    // get all the picture names from fromDropbox into Seq
    val remotePictures:Seq[String] = ???
    remotePictures foreach { pic =>
      if ( !update.contains(pic) ) update + (pic -> 0)
    }
    update
  }

  private[this] var cache:Map[String,Int] = Map()

  def receive:Receive = {
    case StoreVote(pic) => {
      cache = sync
      if ( cache.contains(pic) ) {
        val counter = cache.get(pic).get + 1
        cache + (pic -> counter)
        sender() ! Status.Success
      }
      else sender() ! Status.Failure
    }
    case FetchPictureList() => fetchFromDropbox pipeTo sender()
  }
}

sealed trait PictureRequests

case class FetchPictureList() extends PictureRequests
case class StoreVote(picture:String) extends PictureRequests
