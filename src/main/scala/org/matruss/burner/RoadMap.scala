package org.matruss.burner

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives

object RoadMap extends Directives {

  val BurnerRoad = {
    path("burner"/"report") {
      get { complete { HttpResponse(OK) } }
    } ~
    path("burner"/"event") {
      post { complete { HttpResponse(OK) } }
    }
  }

  val DropboxRoad = null
}
