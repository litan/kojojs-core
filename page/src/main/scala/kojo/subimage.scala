package kojo

import org.scalajs.dom.html.{Image => DomImage}

import scala.concurrent.Future

case class ImageSection(x: Int, y: Int, w: Int, h: Int)
case class SubImage(img: Future[DomImage], section: Option[ImageSection])

case class SpriteSheet(url: String, tileX: Int, tileY: Int)(implicit kojoWorld: KojoWorld) {
  import java.awt.image.BufferedImage
  val sheet = Utils.image(url)

  def imageAt(x: Int, y: Int): SubImage = {
    SubImage(sheet.img, Some(ImageSection(x * tileX, y * tileY, tileX, tileY)))
  }
}
