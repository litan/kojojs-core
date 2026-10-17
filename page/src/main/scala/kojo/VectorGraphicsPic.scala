package kojo

import kojo.doodle.Color
import pixiscalajs.PIXI.Graphics

trait VectorGraphicsPic extends Picture with ReadyPromise {
  def makePic(graphics: Graphics)
  val path = new Graphics()
  val noColor = Color(0, 0, 0)
  path.lineStyle(2, Color.red.toRGBDouble, 1.0)
  path.beginFill(noColor.toRGBDouble, 0.0)
  makePic(path)
  val tnode = path
  makeDone()

  def realDraw(): Unit = {
    kojoWorld.addLayer(tnode)
  }

  def erase(): Unit = {
    kojoWorld.removeLayer(tnode)
  }

  override def setFillColor(c: Color): Unit = {
    PixiCompat.setFill(path, c.toRGBDouble, c.alpha.get)
    kojoWorld.render()
  }

  override def setPenColor(c: Color): Unit = {
    PixiCompat.setPen(path, c.toRGBDouble, c.alpha.get)
    kojoWorld.render()
  }

  override def setPenThickness(t: Double): Unit = {
    PixiCompat.setPenWidth(path, t)
    kojoWorld.render()
  }
}
