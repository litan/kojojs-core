package kojo

import pixiscalajs.PIXI.Graphics

import scala.scalajs.js

/**
 * What differs between PIXI 4 and PIXI 5 where the Kojo code touches the parts of a `Graphics`.
 * In PIXI 4 the parts (`graphicsData`) are on the Graphics itself and have `fillColor`, `lineColor`,
 * `lineWidth`...; in PIXI 5 they are on its `geometry` and the styles are objects (`fillStyle.color`,
 * `lineStyle.width`...).
 */
object PixiCompat {
  /** Is it PIXI 5 or later? If PIXI is not loaded (unit tests in Node) 4 is assumed. */
  lazy val isV5: Boolean = {
    val version =
      if (js.typeOf(js.Dynamic.global.PIXI) == "undefined") "4"
      else js.Dynamic.global.PIXI.VERSION.asInstanceOf[js.UndefOr[String]].getOrElse("4")
    version.takeWhile(_.isDigit).toIntOption.exists(_ >= 5)
  }

  private def dyn(o: Any): js.Dynamic = o.asInstanceOf[js.Dynamic]

  /**
   * The parts of the drawing. In PIXI 5 `finishPoly()` must come FIRST: a path drawn with moveTo/lineTo does
   * not go into graphicsData by itself, it waits in `currentPath`; without it the array comes EMPTY and
   * the loops that set the paint, the pen and the width quietly touch nothing.
   */
  def parts(gr: Graphics): js.Array[js.Dynamic] = {
    val d = dyn(gr)
    val a = if (isV5) { d.finishPoly(); d.geometry.graphicsData } else d.graphicsData
    a.asInstanceOf[js.Array[js.Dynamic]]
  }

  /**
   * Call it before a `lineTo` of a path that is drawn a piece at a time with renders in between (the turtle).
   * In PIXI 5 `moveTo` keeps the path waiting in `currentPath`, and every render calls `finishPoly()`: a
   * path of one point (two numbers) is emptied, and a longer one is pushed into the geometry and
   * `currentPath` becomes null. The next `lineTo` then goes on from nothing: from (0, 0) if the path is null, or
   * from no point at all if it was emptied, so no line shows. If the path is gone, this starts it again at
   * the point where the pen was (`lastX`, `lastY`). In PIXI 4 the path is on the Graphics itself and nothing is
   * lost; it does nothing there.
   */
  def continuePath(gr: Graphics, lastX: Double, lastY: Double): Unit =
    if (isV5) {
      val path = dyn(gr).currentPath
      val gone = js.isUndefined(path) || path == null || path.points.asInstanceOf[js.Array[Double]].length < 2
      if (gone) gr.moveTo(lastX, lastY)
    }

  /** Makes PIXI draw the parts again after their styles were changed. */
  def refresh(gr: Graphics): Unit = {
    val d = dyn(gr)
    if (isV5) d.geometry.invalidate()
    else {
      d.dirty = d.dirty.asInstanceOf[Double] + 1
      d.clearDirty = d.clearDirty.asInstanceOf[Double] + 1
    }
  }

  def setFill(gr: Graphics, color: Double, alpha: Double): Unit = {
    parts(gr).foreach { gd =>
      if (isV5) {
        gd.fillStyle.color = color
        gd.fillStyle.alpha = alpha
        gd.fillStyle.visible = alpha > 0
      }
      else {
        gd.fillColor = color
        gd.fillAlpha = alpha
      }
    }
    refresh(gr)
  }

  def setPen(gr: Graphics, color: Double, alpha: Double): Unit = {
    parts(gr).foreach { gd =>
      if (isV5) {
        gd.lineStyle.color = color
        gd.lineStyle.alpha = alpha
        gd.lineStyle.visible = true
      }
      else {
        gd.lineColor = color
        gd.lineAlpha = alpha
      }
    }
    refresh(gr)
  }

  def setPenWidth(gr: Graphics, width: Double): Unit = {
    parts(gr).foreach { gd =>
      if (isV5) gd.lineStyle.width = width else gd.lineWidth = width
    }
    refresh(gr)
  }
}
