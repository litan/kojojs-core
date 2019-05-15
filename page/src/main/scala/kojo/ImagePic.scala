package kojo

import scala.collection.mutable.ArrayBuffer

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry

import kojo.Utils.newCoordinate
import kojo.doodle.Color
import pixiscalajs.PIXI
class ImagePic(url: String, envelope: Option[Picture])(implicit val kojoWorld: KojoWorld) extends Picture with ReadyPromise {
  val imgLayer = new PIXI.Container
  var sprite: PIXI.Sprite = _
  val tnode = imgLayer

  TurtleImageHelper.addAndLoad(url, url, init)

  private def init(loader: PIXI.loaders.Loader, any: Any): Unit = {
    sprite = new PIXI.Sprite(loader.resources(url).texture)
    sprite.setTransform(0, sprite.height, 1, -1, 0, 0, 0, 0, 0)
    imgLayer.addChild(sprite)
    envelope match {
      case None => makeDone()
      case Some(p) =>
        import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
        p.ready.foreach { _ => makeDone() }
    }

    kojoWorld.render()
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
}
