package org.matruss.burner

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class BurnerEvent(`type`:String, payload:String, fromNumber:String, toNumber:String) {
  def isText:Boolean = `type` equalsIgnoreCase "inboundText"
}

object BurnerEventJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val clientFormat = jsonFormat4(BurnerEvent)
}
