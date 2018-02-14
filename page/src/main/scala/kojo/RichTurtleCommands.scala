package kojo

trait RichTurtleCommands {
  def turn(angle: Double): Unit
  def forward(n: Double): Unit
  def hop(n: Double): Unit
  def setAnimationDelay(i: Long)

  def left(angle: Double): Unit = turn(angle)
  def right(angle: Double)      = turn(-angle)
  def left(): Unit              = left(90)
  def right(): Unit             = right(90)
  def back(n: Double)           = forward(-n)

  def forward(): Unit = forward(25)
  def hop(): Unit     = hop(25)
  def back(): Unit    = back(25)

  trait Speed
  case object slow      extends Speed
  case object medium    extends Speed
  case object fast      extends Speed
  case object superFast extends Speed

  def setSpeed(speed: Speed) = speed match {
    case `slow`      => setAnimationDelay(1000)
    case `medium`    => setAnimationDelay(100)
    case `fast`      => setAnimationDelay(10)
    case `superFast` => setAnimationDelay(0)
  }
//  def arc2(r: Double, a: Double): Unit = throw new UnsupportedOperationException("making arcs is not supported")
//  def left(angle: Double, rad: Double): Unit = arc2(rad, angle)
//  def right(angle: Double, rad: Double): Unit = { if (angle == 0) return; right(180); arc2(rad, -angle) }
//  def turn(angle: Double, rad: Double): Unit = if (angle < 0) right(-angle, rad) else left(angle, rad)
//  def arc(r: Double, a: Double): Unit = turn(a, r)
}
