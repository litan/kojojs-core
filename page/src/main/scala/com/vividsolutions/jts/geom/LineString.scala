package com.vividsolutions.jts.geom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("jsts.geom.LineString")
class LineString extends Geometry {}

object LineString {
  def asString(ls: LineString): String = {
    val result = new StringBuilder
    val coords = ls.getCoordinates()
    coords.foreach { c =>
      result.append(s"(${c.x}, ${c.y}), ")
    }
    result.toString()
  }
}
