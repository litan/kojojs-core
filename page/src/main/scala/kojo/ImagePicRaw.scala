package kojo

import com.vividsolutions.jts.geom.{Coordinate, Geometry}
import kojo.Utils.newCoordinate
import kojo.doodle.Color
import org.scalajs.dom.raw.Event
import org.scalajs.dom.html.Image
import pixiscalajs.PIXI
import pixiscalajs.PIXI.Texture

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.util.{Failure, Success}

class ImagePicRaw(subImg: SubImage, envelope: Option[Picture])(implicit val kojoWorld: KojoWorld) extends Picture with ReadyPromise {
  val imgLayer = new PIXI.Container
  var sprite: PIXI.Sprite = _
  val tnode = imgLayer

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  subImg.img.onComplete {
    case Success(image) =>
      val texture = Texture.from(image)
      subImg.section.foreach { sec =>
        texture.frame = new PIXI.Rectangle(sec.x, sec.y, sec.w, sec.h)
        texture._updateUvs()
      }
      sprite = new PIXI.Sprite(texture)
      sprite.setTransform(0, sprite.height, 1, -1, 0, 0, 0, 0, 0)
      imgLayer.addChild(sprite)
      envelope match {
        case None => makeDone()
        case Some(p) =>
          p.ready.foreach { _ => makeDone() }
      }

      kojoWorld.render()

    case Failure(exception) =>
  }

  override def realDraw(): Unit = {
    kojoWorld.addLayer(imgLayer)
  }

  def erase(): Unit = {
    kojoWorld.removeLayer(imgLayer)
  }

  override def setFillColor(c: Color): Unit = {
  }

  override def setPenColor(c: Color): Unit = {
  }

  override def setPenThickness(t: Double): Unit = {
  }

  override def initGeom(): Geometry = envelope match {
    case None =>
      val cab = new ArrayBuffer[Coordinate]
      val bounds = sprite.getLocalBounds()
      cab += newCoordinate(bounds.x, bounds.y)
      cab += newCoordinate(bounds.x, bounds.y + bounds.height)
      cab += newCoordinate(bounds.x + bounds.width, bounds.y + bounds.height)
      cab += newCoordinate(bounds.x + bounds.width, bounds.y)
      cab += newCoordinate(bounds.x, bounds.y)
      import scala.scalajs.js.JSConverters._
      //    pgTransform.getInverse().transform(Gf.createLineString(cab.toJSArray))
      Utils.Gf.createLineString(cab.toJSArray)
    case Some(p) =>
      p.picGeom
  }

  def copy: Picture = new ImagePicRaw(subImg, envelope)
}
