package kojo

import kojo.doodle.Color

sealed trait Command

case class Forward(n: Double) extends Command

case class Turn(angle: Double) extends Command

case class SetAnimationDelay(delay: Long) extends Command

case class SetPenThickness(t: Double) extends Command

case class SetPenColor(color: Color) extends Command

case class SetFillColor(color: Color) extends Command

case class Hop(n: Double) extends Command
