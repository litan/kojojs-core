package com.vividsolutions.jts.geom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("jsts.geom.GeometryFactory")
class GeometryFactory extends js.Object {
  def createLineString(coords: js.Array[Coordinate]): LineString = js.native
}
