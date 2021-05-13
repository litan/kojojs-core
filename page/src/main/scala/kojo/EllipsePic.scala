package kojo

import com.vividsolutions.jts.geom.Geometry
import kojo.Utils.newCoordinate
import pixiscalajs.PIXI.Graphics

class EllipsePic(rx: Double, ry: Double)(implicit val kojoWorld: KojoWorld) extends VectorGraphicsPic {
  def makePic(graphics: Graphics): Unit = {
    path.drawEllipse(0, 0, rx, ry)
  }

  def initGeom(): Geometry = {
    def x(t: Double) = rx * math.cos(t.toRadians)
    def y(t: Double) = ry * math.sin(t.toRadians)
    val cab = for (i <- 1 to 360) yield newCoordinate(x(i), y(i))
    import scala.scalajs.js.JSConverters._
    Utils.Gf.createLineString(cab.toJSArray)
  }

  def copy: Picture = new EllipsePic(rx, ry)
}
