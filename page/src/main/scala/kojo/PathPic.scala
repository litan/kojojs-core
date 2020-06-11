package kojo

import scala.collection.mutable.ArrayBuffer

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry

import kojo.Utils.newCoordinate
import kojo.doodle.Color
import pixiscalajs.PIXI.Graphics
import pixiscalajs.PIXI.Polygon

class PathPic(fn: Graphics => Unit)(implicit val kojoWorld: KojoWorld) extends VectorGraphicsPic {
  def makePic(graphics: Graphics): Unit = {
    fn(path)
  }

  def initGeom(): Geometry = {
    val cab = new ArrayBuffer[Coordinate]
    path.graphicsData.foreach { gd =>
      val tpe = gd.`type`
      if (tpe == 0) {
        gd.shape.asInstanceOf[Polygon].points.grouped(2).foreach { xy =>
          cab += newCoordinate(xy(0), xy(1))
        }
      }
      else {
        println(s"Warning: geometry not supported for graphic type: $tpe")
      }
    }
    if (cab.size == 0) {
      cab += newCoordinate(1e4, 1e4)
    }
    if (cab.size == 1) {
      cab += cab(0)
    }
    import scala.scalajs.js.JSConverters._
    Utils.Gf.createLineString(cab.toJSArray)
  }
}
