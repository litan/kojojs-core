package kojo.syntax

import kojo.doodle.{Normalized, UnsignedByte, Angle}
import kojo.syntax.normalized._
import kojo.syntax.uByte._
import angle._

object Builtins {
  val Color = kojo.doodle.Color

  implicit def int2ubyte(n: Int): UnsignedByte = n.uByte

  implicit def double2norm(n: Double): Normalized = n.normalized

  implicit def int2norm(n: Int): Normalized = n.normalized

  implicit def double2angle(n: Double): Angle = n.degrees

  implicit def int2angle(n: Int): Angle = n.degrees
}
