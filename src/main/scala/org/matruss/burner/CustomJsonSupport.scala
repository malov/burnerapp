package org.matruss.burner

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object PictureJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val clientFormat = jsonFormat2(Picture)
}

object BurnerEventJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val clientFormat = jsonFormat4(BurnerEvent)
}
