package kojo

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.PrecisionModel

object Utils {
  def doublesEqual(d1: Double, d2: Double, tol: Double): Boolean = {
    if (d1 == d2) return true
    else if (math.abs(d1 - d2) < tol) return true
    else return false
  }

  def deg2radians(angle: Double) = angle * math.Pi / 180

  def rad2degrees(angle: Double) = angle * 180 / math.Pi

  lazy val pmodel = new PrecisionModel(14)
  def newCoordinate(x: Double, y: Double) = {
    val coord = new Coordinate(x, y)
    pmodel.makePrecise(coord)
    coord
  }
}
