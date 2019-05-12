package kojo

import com.vividsolutions.jts.geom.Geometry

import kojo.Utils.newCoordinate
import kojo.doodle.Color
import pixiscalajs.PIXI.Graphics

class CirclePic(r: Double)(implicit val kojoWorld: KojoWorld)
  extends Picture with ReadyPromise {
  val path = new Graphics()
  val noColor = Color(0, 0, 0)
  path.lineStyle(2, Color.red.toRGBDouble, 1.0)
  path.beginFill(noColor.toRGBDouble, 0.0)
  path.drawCircle(0, 0, r)
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
    def x(t: Double) = r * math.cos(t.toRadians)
    def y(t: Double) = r * math.sin(t.toRadians)
    val cab = for (i <- 1 to 360) yield newCoordinate(x(i), y(i))
    import scala.scalajs.js.JSConverters._
    Utils.Gf.createLineString(cab.toJSArray)
  }
}
