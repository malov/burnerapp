package org.matruss.burner

import akka.actor.ActorSelection
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Route, Directives}

import scala.util.control.NonFatal

// import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.ExecutionContext

trait RoadMap extends Directives {

  implicit def ec:ExecutionContext
  // implicit def materializer: ActorMaterializer
  implicit def timeout:Timeout

  protected def voter:ActorSelection

  def roads:Route = {

    import BurnerEventJson._
    import RoadMap._

    path("burner"/"report") {
      get {
        complete {
          val q = voter ? FetchPictureList()
          q.mapTo[PictureSeq].map( buildResponse ).recover( produceError )
        }
      }
    } ~
    path("burner"/"event") {
      post {
        entity(as[BurnerEvent]) { ev =>
          ev.isText match {
            case true => complete {
              voter ? StoreVote(ev.payload) map { _ => HttpResponse(OK) } recover { produceError }
            }
            case false => complete { HttpResponse(BadRequest) }
          }
        }
      }
    }
  }
  private def produceError: PartialFunction[Throwable, HttpResponse] = {
    case NonFatal(cause) => HttpResponse(InternalServerError)
    case cause => throw cause
  }
}

object RoadMap {
  import spray.json._

  def buildResponse(pic:PictureSeq):HttpResponse = {
    import PictureJson._
    val body = pic.seq.map(_.toJson.compactPrint) mkString "\n"
    HttpResponse(OK, entity = body)
  }
}