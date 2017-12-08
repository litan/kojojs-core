package kojo

object TurtleHelper {

  def posAfterForward(x: Double, y: Double, theta: Double, n: Double): (Double, Double) = {
    val delX = math.cos(theta) * n
    val delY = math.sin(theta) * n
    (x + delX, y + delY)
  }

  def thetaTowards(px: Double, py: Double, x: Double, y: Double, oldTheta: Double): Double = {
    val (x0, y0) = (px, py)
    val delX     = x - x0
    val delY     = y - y0
    if (Utils.doublesEqual(delX, 0, 0.001)) {
      if (Utils.doublesEqual(delY, 0, 0.001)) oldTheta
      else if (delY > 0) math.Pi / 2
      else 3 * math.Pi / 2
    } else if (Utils.doublesEqual(delY, 0, 0.001)) {
      if (delX > 0) 0
      else math.Pi
    } else {
      var nt2 = math.atan(delY / delX)
      if (delX < 0 && delY > 0) nt2 += math.Pi
      else if (delX < 0 && delY < 0) nt2 += math.Pi
      else if (delX > 0 && delY < 0) nt2 += 2 * math.Pi
      nt2
    }
  }

  def thetaAfterTurn(angle: Double, oldTheta: Double) = {
    var newTheta = oldTheta + Utils.deg2radians(angle)
    if (newTheta < 0) newTheta = newTheta % (2 * math.Pi) + 2 * math.Pi
    else if (newTheta > 2 * math.Pi) newTheta = newTheta % (2 * math.Pi)
    newTheta
  }

  def distance(x0: Double, y0: Double, x: Double, y: Double): Double = {
    val delX = x - x0
    val delY = y - y0
    math.sqrt(delX * delX + delY * delY)
  }

  def delayFor(dist: Double, animationDelay: Long): Long = {
    if (animationDelay < 1) {
      return animationDelay
    }

    // _animationDelay is delay for 100 steps;
    // Here we calculate delay for specified distance
    val speed = 100f / animationDelay
    val delay = Math.abs(dist) / speed
    delay.round
  }
}
