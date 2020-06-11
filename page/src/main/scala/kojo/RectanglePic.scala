package kojo

import com.vividsolutions.jts.geom.{Coordinate, Geometry}
import kojo.Utils.newCoordinate
import pixiscalajs.PIXI.Graphics

import scala.collection.mutable.ArrayBuffer

class RectanglePic(w: Double, h: Double)(implicit val kojoWorld: KojoWorld) extends VectorGraphicsPic {
  def makePic(graphics: Graphics): Unit = {
    path.drawRect(0, 0, w, h)
  }

  def initGeom(): Geometry = {
    val cab = new ArrayBuffer[Coordinate]
    cab += newCoordinate(0, 0)
    cab += newCoordinate(0, h)
    cab += newCoordinate(w, h)
    cab += newCoordinate(w, 0)
    cab += newCoordinate(0, 0)

    import scala.scalajs.js.JSConverters._
    Utils.Gf.createLineString(cab.toJSArray)
  }
}
