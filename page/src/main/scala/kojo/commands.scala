package kojo

sealed trait Command

case class Forward(n: Double) extends Command

case class Turn(angle: Double) extends Command

case class SetAnimationDelay(delay: Long) extends Command

case class SetPenThickness(t: Double) extends Command

case class SetPenColor(color: Int) extends Command

case class SetFillColor(color: Int) extends Command

case class Hop(n: Double) extends Command
