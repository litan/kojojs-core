package kojo

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LineString
import com.vividsolutions.jts.geom.PrecisionModel

import pixiscalajs.PIXI.Matrix
import pixiscalajs.PIXI.Point
import pixiscalajs.PIXI.Rectangle

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

  def transformRectangle(rect: Rectangle, m: Matrix): Rectangle = {
    val bottomLeft = Point(rect.x, rect.y)
    val topRight = Point(rect.x + rect.width, rect.y + rect.height)
    val newBottomLeft = m.apply(bottomLeft)
    val newTopRight = m.apply(topRight)
    new Rectangle(newBottomLeft.x, newBottomLeft.y, newTopRight.x - newBottomLeft.x, newTopRight.y - newBottomLeft.y)
  }
}
