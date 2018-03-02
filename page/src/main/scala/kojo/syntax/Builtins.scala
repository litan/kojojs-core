package kojo.syntax

import kojo.doodle.{Angle, Normalized, UnsignedByte}
import kojo.syntax.angle._
import kojo.syntax.normalized._
import kojo.syntax.uByte._
import org.scalajs.dom.window

object Builtins {
  val Color   = kojo.doodle.Color
  val noColor = Color(0, 0, 0, 0)

  implicit def int2ubyte(n: Int): UnsignedByte = n.uByte

  implicit def double2norm(n: Double): Normalized = n.normalized

  implicit def int2norm(n: Int): Normalized = n.normalized

  implicit def double2angle(n: Double): Angle = n.degrees

  implicit def int2angle(n: Int): Angle = n.degrees

  val Random = new java.util.Random

  def random(upperBound: Int) = Random.nextInt(upperBound)

  def randomDouble(upperBound: Int) = Random.nextDouble * upperBound

  def randomBoolean = Random.nextBoolean

  def randomFrom[T](seq: Seq[T]) = seq(random(seq.length))

  def randomColor = Color(random(256), random(256), random(256))

  def randomTransparentColor = Color(random(256), random(256), random(256), 100 + random(156))

  def readln(prompt: String): String = {
    val ret = window.prompt(prompt, "Type here")
    if (ret == null)
      throw new RuntimeException("Read failed.")
    else
      ret
  }

  def readInt(prompt: String): Int = readln(prompt).toInt

  def readDouble(prompt: String): Double = readln(prompt).toDouble
}
