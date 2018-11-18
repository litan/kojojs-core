package com.vividsolutions.jts.geom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("jsts.geom.PrecisionModel")
class PrecisionModel(scale: Double) extends js.Object {
  def makePrecise(c: Coordinate): Unit = js.native
}
