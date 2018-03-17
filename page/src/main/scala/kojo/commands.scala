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

case class SetPosition(x: Double, y: Double) extends Command

case class SetHeading(theta: Double) extends Command

case class MoveTo(x: Double, y: Double) extends Command

case class Arc2(r: Double, a: Double) extends Command

case class Write(text: String) extends Command

case class SetPenFontSize(n: Int) extends Command

case object SavePosHe extends Command

case object RestorePosHe extends Command

case object Clear extends Command

case class Pause(seconds: Double) extends Command

case object PenUp extends Command

case object PenDown extends Command

case object PopQ extends Command
