package kojo

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LineString
import com.vividsolutions.jts.geom.PrecisionModel
import pixiscalajs.PIXI
import pixiscalajs.PIXI.Matrix
import pixiscalajs.PIXI.Point
import pixiscalajs.PIXI.Rectangle

import scala.concurrent.{Future, Promise}
import org.scalajs.dom.html.{Image => DomImage}

object Utils {
  def doublesEqual(d1: Double, d2: Double, tol: Double): Boolean = {
    if (d1 == d2) return true
    else if (math.abs(d1 - d2) < tol) return true
    else return false
  }

  def deg2radians(angle: Double) = angle * math.Pi / 180

  def rad2degrees(angle: Double) = angle * 180 / math.Pi

  lazy val Gf = new GeometryFactory

  lazy val pmodel = new PrecisionModel(14)
  def newCoordinate(x: Double, y: Double) = {
    val coord = new Coordinate(x, y)
    pmodel.makePrecise(coord)
    coord
  }

  def printMatrix(m: pixiscalajs.PIXI.Matrix) {
    println(m.tx, m.ty, m.a, m.d, m.b, m.c)
  }

  def printLineString(geom: Geometry): Unit = {
    println(LineString.asString(geom.asInstanceOf[LineString]))
  }

  def distance(p1: Point, p2: Point) =
    math.sqrt(math.pow(p2.x - p1.x, 2) + math.pow(p2.y - p1.y, 2))

  def transformRectangle(rect: Rectangle, m: Matrix): Rectangle = {
    // assume rectangle stays a rectangle!
    val bottomLeft = Point(rect.x, rect.y)
    val bottomRight = Point(rect.x + rect.width, rect.y)
    val topLeft = Point(rect.x, rect.y + rect.height)
    val topRight = Point(rect.x + rect.width, rect.y + rect.height)
    val newBottomLeft = m.apply(bottomLeft)
    val newBottomRight = m.apply(bottomRight)
    val newTopLeft = m.apply(topLeft)
    val newTopRight = m.apply(topRight)
    val minx = math.min(math.min(math.min(newBottomLeft.x, newTopLeft.x), newTopRight.x), newBottomRight.x)
    val miny = math.min(math.min(math.min(newBottomLeft.y, newTopLeft.y), newTopRight.y), newBottomRight.y)
    val maxx = math.max(math.max(math.max(newBottomLeft.x, newTopLeft.x), newTopRight.x), newBottomRight.x)
    val maxy = math.max(math.max(math.max(newBottomLeft.y, newTopLeft.y), newTopRight.y), newBottomRight.y)
    new Rectangle(minx, miny, maxx - minx, maxy - miny)
  }

  def printRectangle(r: Rectangle): Unit = {
    println(s"Rectangle(${r.x}, ${r.y}, ${r.width}, ${r.height})")
  }

  def image(url: String)(implicit kojoWorld: KojoWorld): SubImage = {
    val ret = Promise[DomImage]()
    def init(loader: PIXI.loaders.Loader, any: Any): Unit = {
      val img = loader.resources(url)
      ret.success(img.data.asInstanceOf[DomImage])
    }
    AssetLoader.addAndLoad(url, url, init)
    SubImage(ret.future, None)
  }

  def notSupported(name: String, reason: String) = throw new UnsupportedOperationException(s"$name - operation not available $reason:\n${toString}")
}
