package org.matruss.burner

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

// todo need to combine it in a trait and mix it in, however initial naive attempt
// todo to do it caused object construction problem, will figure it later
object PictureJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val clientFormat = jsonFormat2(Picture)
}

object BurnerEventJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val clientFormat = jsonFormat4(BurnerEvent)
}

object DpPhotoInfoJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val clientFormat = jsonFormat2(DpPhotoInfo)
}

object DpPictureJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val clientFormat = jsonFormat13(DpPicture)
}

object DpFolderJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val clientFormat = jsonFormat12(DpFolder)
}
