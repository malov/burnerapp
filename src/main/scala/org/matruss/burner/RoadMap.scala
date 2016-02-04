package org.matruss.burner

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives

object RoadMap extends Directives {

  val BurnerRoad = {
    path("burner"/"report") {
      post { complete { HttpResponse(OK) } }
    } ~
    path("burner"/"event") {
      get { complete { HttpResponse(OK) } }
    }
  }

  val DropboxRoad = null
}
