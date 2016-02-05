package org.matruss.burner

import akka.actor.{Status, Actor}

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{ImplicitMaterializer, Sink, Source}

import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}

/*
 This actor does two main things: interacts with Dropbox via API and updates picture
 voting information (in Dropbox as well).

 Key assumption is, that list of pictures can grow or shrink independently of this app,
 which means that it has constantly sync with it to reflect reality.

 Here is a life cyle :
 (1) Pre-start : get list of live pictures, get list of previous votes,
 populate latter with missing pictures and zero voting count, delete pictures
 no longer "live", store result in cache.
 (2) When getting vote request : pre-fetch list of pictures from Dropbox, add
 missing entries with zero count to the cache, apply vote and store it in the cache.
 (3) Pre-stop : delete old cache in Dropbox, store new one under the same path.

 Other assumptions :
 (1) Syncing is "cheap", meaning voting doesn't happen two often and user doesn't have
 too many pictures, so it's blocking and done at the every request.
 If this is not the case, then we'd need to update logic, syncing asyncronously
 and applying vote after it's done.
 (2) Authentication with Dropbox is done already in the launcher.
 (3) Dropbox is always up, pre-start, pre-stop actions always succeed. It's weak
 assumption, for preventing failures it should probably be done together with syncing
 on disk.

 Obvious drawbacks :
 (1) Cache key is a picture name, and vote consists only of picture name. If it's
 picture name plus some other text, it won't be found in a cache and vote would not
 be applied.
 (2)  Any text message will be procesessed in the current settings, which
 means that any non-voting SMS would trigger cache sync. It is bad, however, I could
 not find a way around it : if key (i.e payload SMS text) is missing it might just mean that
 picture have been added recently and after sync it could be found. Potentially we
 could track picture adding activity via media event catching, thus avoiding synching
 all together but this is sound a bit too convoluted for test application.
*/

class VoteProcessingActor(token:String) extends Actor with ImplicitMaterializer {

  import Defaults._

  implicit def conf = context.system.settings.config.getConfig(Root)
  implicit val system = context.system
  implicit val ec = context.dispatcher

  import akka.event.Logging
  implicit def log = Logging(system, this)

  private[this] val dpHost =
    conf.as[Option[String]]("dropbox.server").getOrElse(DropboxHost)
  private[this] val dpPort =
    conf.as[Option[Int]]("dropbox.port").getOrElse(DropboxPort)
  private[this] val dpPath =
    conf.as[Option[String]]("dropbox.path").getOrElse(DropboxPath)
  private[this] val requestTimeout =
    conf.as[Option[FiniteDuration]]("burner.startup_timeout").getOrElse(RequestTimeout)

  lazy val dpConnection = Http().outgoingConnection(dpHost,dpPort)

  private[this] def fetchFromDropbox:Future[String] = {
    def dpRequest(req:HttpRequest):Future[HttpResponse] =
      Source.single(req).via(dpConnection).runWith(Sink.head)

    val request = RequestBuilding.Get(dpPath).withHeaders(RawHeader("Authorization:Bearer", token))
    log.info("Headers: {}", request.headers.mkString("\n"))
    log.info("Request URI: {}", request.getUri().toString)
    dpRequest( request ) flatMap { rsp =>
      rsp.status match {
        case OK => Unmarshal(rsp.entity).to[String]
        case x => {
          log.error("Connection failed with status {}, message {}", x, rsp.entity.toString )
          Future.failed(new RuntimeException("Error"))
        }
      }
    }
  }

  private[this] def getRemotes:Seq[String] = {
    val fromDropbox = Await.result(fetchFromDropbox, requestTimeout)

    // get all the picture names from fromDropbox into Seq
    val remotes:Seq[String] = ???
    remotes
  }

  private[this] def syncLight:Map[String,Int] = {
    val update = cache
    getRemotes foreach { pic =>  if ( !update.contains(pic) ) update + (pic -> 0) }
    update
  }

  private[this] def syncDeep:Map[String,Int] = {
    val update = cache
    val fromDropbox = Await.result(fetchFromDropbox, requestTimeout)

    val remotes:Seq[String] = getRemotes
    update foreach { case(k,v) => if ( !remotes.contains(k) ) update - k }
    remotes foreach { pic =>  if ( !update.contains(pic) ) update + (pic -> 0) }

    update
  }

  private[this] var cache:Map[String,Int] = Map()

  def receive:Receive = {
    case StoreVote(pic,_) => {
      cache = syncLight
      if ( cache.contains(pic) ) {
        val counter = cache.get(pic).get + 1
        cache + (pic -> counter)
        sender() ! Status.Success
      }
      else sender() ! Status.Failure
    }
    case FetchPictureList(_) => {
      cache = syncLight
      val list = cache map { case(k,v) => Picture(k,v) }
      sender() ! PictureSeq( list.toSeq )
    }
    case x => sender() ! Status.Failure( new RuntimeException("Unknown message"))
  }

  // todo ! causing actor fail at start up
  override def preStart(): Unit = { cache = syncDeep }

  // todo ! need to add shutdown hook to properly save cache
}

sealed trait PictureRequests

case class FetchPictureList( testFailed:Boolean = false ) extends PictureRequests
case class StoreVote( picture:String, testFailed:Boolean = false ) extends PictureRequests
