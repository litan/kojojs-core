package com.vividsolutions.jts.geom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("jsts.geom.AffineTransformation")
class AffineTransformation extends js.Object {
  def this(matrix: js.Array[Double]) = this()
  def transform(g: Geometry): Geometry = js.native
  def getInverse(): AffineTransformation = js.native
}
