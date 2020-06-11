package kojo

import kojo.Speed.Speed
import kojo.doodle.Color

trait TurtleAPI {
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
  def left(angle: Double, rad: Double): Unit
  def right(angle: Double, rad: Double): Unit
  def turn(angle: Double, rad: Double): Unit
  def arc(r: Double, a: Double): Unit
  def circle(r: Double): Unit
}
