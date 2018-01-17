package kojo

trait RichTurtleCommands {
  def turn(angle: Double): Unit
  def forward(n: Double): Unit

  def left(angle: Double): Unit = turn(angle)
  def right(angle: Double)      = turn(-angle)
  def left(): Unit              = left(90)
  def right(): Unit             = right(90)
  def back(n: Double)           = forward(-n)

  def forward(): Unit = forward(25)
  def back(): Unit    = back(25)

//  def arc2(r: Double, a: Double): Unit = throw new UnsupportedOperationException("making arcs is not supported")
//  def left(angle: Double, rad: Double): Unit = arc2(rad, angle)
//  def right(angle: Double, rad: Double): Unit = { if (angle == 0) return; right(180); arc2(rad, -angle) }
//  def turn(angle: Double, rad: Double): Unit = if (angle < 0) right(-angle, rad) else left(angle, rad)
//  def arc(r: Double, a: Double): Unit = turn(a, r)
}
