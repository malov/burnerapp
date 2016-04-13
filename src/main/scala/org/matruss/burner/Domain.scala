package org.matruss.burner

trait Domain

case class BurnerEvent(`type`:String, payload:String, fromNumber:String, toNumber:String) extends Domain {
  def isText:Boolean = `type` equalsIgnoreCase "inboundText"
}

case class Picture(name:String, vote:Int) extends Domain
case class PictureSeq(seq:Seq[Picture]) extends Domain

trait DropboxEntry extends Domain

case class DpPhotoInfo(lat_long:List[Double], time_taken:String) extends DropboxEntry
case class DpPicture(
                     size:String,
                     rev:String,
                     thumb_exists:Boolean,
                     bytes:Long,
                     modified:String,
                     client_mtime:String,
                     path:String,
                     photo_info:DpPhotoInfo,
                     is_dir:Boolean,
                     icon:String,
                     root:String,
                     mime_type:String,
                     revision:Long
                     ) extends DropboxEntry

case class DpFolder(
                   size:String,
                   hash:String,
                   bytes:Int,
                   thumb_exists:Boolean,
                   rev:String,
                   modified:String,
                   path:String,
                   is_dir:Boolean,
                   icon:String,
                   root:String,
                   contents:List[DpPicture],
                   revision:Long
                   ) extends DropboxEntry
