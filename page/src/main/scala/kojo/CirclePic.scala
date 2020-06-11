package kojo

import com.vividsolutions.jts.geom.Geometry
import kojo.Utils.newCoordinate
import pixiscalajs.PIXI.Graphics

class CirclePic(r: Double)(implicit val kojoWorld: KojoWorld) extends VectorGraphicsPic {
  def makePic(graphics: Graphics): Unit = {
    path.drawCircle(0, 0, r)
  }

  def initGeom(): Geometry = {
    def x(t: Double) = r * math.cos(t.toRadians)
    def y(t: Double) = r * math.sin(t.toRadians)
    val cab = for (i <- 1 to 360) yield newCoordinate(x(i), y(i))
    import scala.scalajs.js.JSConverters._
    Utils.Gf.createLineString(cab.toJSArray)
  }
}
