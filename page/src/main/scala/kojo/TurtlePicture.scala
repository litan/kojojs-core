package kojo

import scala.collection.mutable.ArrayBuffer

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.PrecisionModel

import kojo.doodle.Color

object TurtlePicture {
  def apply(fn: Turtle => Unit)(implicit turtleWorld: TurtleWorld): TurtlePicture = {
    val tp = new TurtlePicture
    tp.make(fn)
    tp
  }
}

class TurtlePicture private[kojo] (implicit val turtleWorld: TurtleWorld) extends Picture {
  val turtle = new Turtle(0, 0, true)
  val picLayer = turtle.turtleLayer
  val tnode = picLayer
  var made = false
  val noColor = Color(0, 0, 0, 0)

  def make(fn: Turtle => Unit): Unit = {
    turtle.setAnimationDelay(0)
    turtle.setFillColor(noColor)
    fn(turtle)
    turtle.sync { () =>
      made = true
    }
  }
  def realDraw(): Unit = {
    turtleWorld.addTurtleLayer(picLayer)
    turtleWorld.render()
  }
  def erase(): Unit = {
    turtleWorld.removeTurtleLayer(picLayer)
    turtleWorld.render()
  }

  lazy val pmodel = new PrecisionModel(14)
  def newCoordinate(x: Double, y: Double) = {
    val coord = new Coordinate(x, y)
    pmodel.makePrecise(coord)
    coord
  }

  def initGeom(): Geometry = {
    val cab = new ArrayBuffer[Coordinate]
    val points = turtle.turtlePathPoints
    if (points.size > 1) {
      points.foreach { pt =>
        cab += newCoordinate(pt._1, pt._2)
      }
    }
    if (cab.size == 1) {
      cab += cab(0)
    }
    import scala.scalajs.js.JSConverters._
    Gf.createLineString(cab.toJSArray)
  }

  def setFillColor(c: Color): Unit = {
    turtle.sync { () =>
      val gds = turtle.turtlePath.graphicsData
      gds.foreach { gd =>
        gd.fillColor = c.toRGBDouble
        gd.fillAlpha = c.alpha.get
      }
      turtle.turtlePath.dirty += 1
      turtle.turtlePath.clearDirty += 1
      turtleWorld.render()
    }
  }

  def setPenColor(c: Color): Unit = {
    turtle.sync { () =>
      val gds = turtle.turtlePath.graphicsData
      gds.foreach { gd =>
        gd.lineColor = c.toRGBDouble
        gd.lineAlpha = c.alpha.get
      }
      turtle.turtlePath.dirty += 1
      turtle.turtlePath.clearDirty += 1
      turtleWorld.render()
    }
  }

  def setPenThickness(t: Double): Unit = {
    turtle.sync { () =>
      val gds = turtle.turtlePath.graphicsData
      gds.foreach { gd =>
        gd.lineWidth = t
      }
      turtle.turtlePath.dirty += 1
      turtle.turtlePath.clearDirty += 1
      turtleWorld.render()
    }
  }
}
