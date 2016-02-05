package org.matruss.burner

trait Domain

case class BurnerEvent(`type`:String, payload:String, fromNumber:String, toNumber:String) extends Domain {
  def isText:Boolean = `type` equalsIgnoreCase "inboundText"
}

case class Picture(name:String, vote:Int) extends Domain
case class PictureSeq(seq:Seq[Picture]) extends Domain
