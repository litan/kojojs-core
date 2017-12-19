package kojo.syntax

import kojo.doodle.{Normalized, UnsignedByte}
import kojo.syntax.normalized._
import kojo.syntax.uByte._

object Builtins {
  val Color = kojo.doodle.Color

  implicit def int2ubyte(n: Int): UnsignedByte = n.uByte

  implicit def double2norm(n: Double): Normalized = n.normalized

}
