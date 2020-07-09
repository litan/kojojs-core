package kojo

import scala.collection.mutable.ArrayBuffer

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry

import kojo.Utils.newCoordinate
import kojo.doodle.Color

object TurtlePicture {
  def apply(fn: Turtle => Unit)(implicit kojoWorld: KojoWorld): TurtlePicture = {
    val tp = new TurtlePicture(fn)
    tp.make()
    tp
  }
}

class TurtlePicture private[kojo] (fn: Turtle => Unit)(implicit val kojoWorld: KojoWorld)
  extends Picture with ReadyPromise {
  val turtle = new Turtle(0, 0, true)
  val picLayer = turtle.turtleLayer
  val tnode = picLayer
  val noColor = Color(0, 0, 0, 0)

  def make(): Unit = {
    turtle.setAnimationDelay(0)
    turtle.setFillColor(noColor)
    invisible()
    fn(turtle)
    turtle.sync { () =>
      makeDone()
      visible()
    }
  }

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  def realDraw(): Unit = {
    kojoWorld.addLayer(tnode)
  }

  def erase(): Unit = {
    ready.foreach { _ =>
      kojoWorld.removeLayer(picLayer)
      //      turtle.turtlePath.destroy()
    }
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
    Utils.Gf.createLineString(cab.toJSArray)
  }

  def setFillColor(c: Color): Unit = {
    ready.foreach { u =>
      val gds = turtle.turtlePath.graphicsData
      gds.foreach { gd =>
        gd.fillColor = c.toRGBDouble
        gd.fillAlpha = c.alpha.get
      }
      turtle.turtlePath.dirty += 1
      turtle.turtlePath.clearDirty += 1
      kojoWorld.render()
    }
  }

  def setPenColor(c: Color): Unit = {
    ready.foreach { u =>
      val gds = turtle.turtlePath.graphicsData
      gds.foreach { gd =>
        gd.lineColor = c.toRGBDouble
        gd.lineAlpha = c.alpha.get
      }
      turtle.turtlePath.dirty += 1
      turtle.turtlePath.clearDirty += 1
      kojoWorld.render()
    }
  }

  def setPenThickness(t: Double): Unit = {
    ready.foreach { u =>
      val gds = turtle.turtlePath.graphicsData
      gds.foreach { gd =>
        gd.lineWidth = t
      }
      turtle.turtlePath.dirty += 1
      turtle.turtlePath.clearDirty += 1
      kojoWorld.render()
    }
  }

  def copy = TurtlePicture(fn)
}
