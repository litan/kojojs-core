package kojo

import kojo.Speed.Speed
import kojo.doodle.Color
import pixiscalajs.PIXI.{Graphics, Polygon}

trait TurtleAPI extends VertexShapeSupport {
  def forward(n: Double): Unit
  def hop(n: Double): Unit
  def turn(angle: Double): Unit
  def setAnimationDelay(delay: Long): Unit
  def setSlowness(delay: Long): Unit = setAnimationDelay(delay)
  def setPenThickness(t: Double): Unit
  def setPenColor(color: Color): Unit
  def setPenFontSize(n: Int): Unit
  def setFillColor(color: Color): Unit
  def setPosition(x: Double, y: Double): Unit
  def setHeading(theta: Double): Unit
  def moveTo(x: Double, y: Double): Unit
  def lineTo(x: Double, y: Double) = moveTo(x, y)
  def arc2(r: Double, a: Double): Unit
  def write(text: String): Unit
  def savePosHe(): Unit
  def restorePosHe(): Unit
  def clear(): Unit
  def cleari(): Unit
  def pause(seconds: Double): Unit
  def penUp(): Unit
  def penDown(): Unit
  def invisible(): Unit
  def visible(): Unit

  def right(): Unit
  def left(): Unit
  def right(angle: Double): Unit
  def left(angle: Double): Unit
  def back(n: Double): Unit
  def forward(): Unit
  def hop(): Unit
  def back(): Unit
  def setSpeed(speed: Speed): Unit
  def left(angle: Double, radius: Double): Unit
  def right(angle: Double, radius: Double): Unit
  def turn(angle: Double, radius: Double): Unit
  def arc(radius: Double, angle: Double): Unit
  def circle(radius: Double): Unit

  def shapeDone(path: Graphics): Unit = {
    path.graphicsData.foreach { gd =>
      val tpe = gd.`type`
      if (tpe == 0) {
        var first = true
        val points = gd.shape.asInstanceOf[Polygon].points.grouped(2).foreach { xy =>
          val (x, y) = (xy(0), xy(1))
          if (first) {
            setPosition(x, y)
            first = false
          }
          else {
            moveTo(x, y)
          }
        }
      }
      else {
        println(s"Warning: geometry not supported for graphic type: $tpe")
      }
    }
  }
}
