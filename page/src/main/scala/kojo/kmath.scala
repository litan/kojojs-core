package kojo

import pixiscalajs.PIXI.Point

object Kmath {
  implicit def seqToArrD(seq: Seq[Double]): Array[Double] = seq.toArray
  implicit def seqToArrI(seq: Seq[Int]): Array[Double] = seq.map { _.toDouble }.toArray

  def constrain(value: Double, min: Double, max: Double) = MathImpl.constrain(value, min, max)
  def map(value: Double, start1: Double, stop1: Double, start2: Double, stop2: Double) = MathImpl.map(value, start1, stop1, start2, stop2)
  def lerp(start: Double, stop: Double, amt: Double) = MathImpl.lerp(start, stop, amt)

  def distance(x1: Double, y1: Double, x2: Double, y2: Double): Double =
    math.sqrt(math.pow(x2 - x1, 2) + math.pow(y2 - y1, 2))
  def distance(p1: Point, p2: Point): Double = distance(p1.x, p1.y, p2.x, p2.y)

  def angle(x1: Double, y1: Double, x2: Double, y2: Double): Double = math.atan2(y2 - y1, x2 - x1).toDegrees
  def angle(p1: Point, p2: Point): Double = angle(p1.x, p1.y, p2.x, p2.y)

  private lazy val log2_e = math.log(2)
  def log2(n: Double) = math.log(n) / log2_e
}

private object MathImpl {
  def constrain(value: Double, min: Double, max: Double) = {
    if (value < min) min
    else if (value > max) max
    else value
  }

  def map(value: Double, low1: Double, high1: Double, low2: Double, high2: Double) = {
    val range1: Double = high1 - low1
    val range2: Double = high2 - low2
    low2 + range2 * (value - low1) / range1
  }

  def lerp(value1: Double, value2: Double, amt: Double) = {
    require(amt >= 0d && amt <= 1d)
    val range: Double = value2 - value1
    value1 + amt * range
  }
}
