package kojo

import scala.collection.mutable.ArrayBuffer

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry

import kojo.Utils.newCoordinate
import kojo.doodle.Color
import pixiscalajs.PIXI.Graphics
import pixiscalajs.PIXI.Polygon

class PathPic(path: Graphics, fn: Graphics => Unit)(implicit val kojoWorld: KojoWorld)
  extends Picture with ReadyPromise {
  val noColor = Color(0, 0, 0)
  path.lineStyle(2, Color.red.toRGBDouble, 1.0)
  path.beginFill(noColor.toRGBDouble, 0.0)
  fn(path)
  val tnode = path
  makeDone()

  override def realDraw(): Unit = {
    kojoWorld.addLayer(tnode)
  }

  def erase(): Unit = {
    kojoWorld.removeLayer(tnode)
  }

  override def setFillColor(c: Color): Unit = {
    val gds = path.graphicsData
    gds.foreach { gd =>
      gd.fillColor = c.toRGBDouble
      gd.fillAlpha = c.alpha.get
    }
    path.dirty += 1
    path.clearDirty += 1
    kojoWorld.render()
  }

  override def setPenColor(c: Color): Unit = {
    val gds = path.graphicsData
    gds.foreach { gd =>
      gd.lineColor = c.toRGBDouble
      gd.lineAlpha = c.alpha.get
    }
    path.dirty += 1
    path.clearDirty += 1
    kojoWorld.render()
  }

  override def setPenThickness(t: Double): Unit = {
    val gds = path.graphicsData
    gds.foreach { gd =>
      gd.lineWidth = t
    }
    path.dirty += 1
    path.clearDirty += 1
    kojoWorld.render()
  }

  override def initGeom(): Geometry = {
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
