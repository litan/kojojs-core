package kojo

import com.vividsolutions.jts.geom.Geometry

import kojo.doodle.Color
import pixiscalajs.PIXI

class TextPic(text: Any, fontSize: Int, color: Color)(implicit val kojoWorld: KojoWorld)
  extends Picture with ReadyPromise {
  val textNode = {
    val pixiText = new PIXI.Text(text.toString)
    pixiText.setTransform(0, 0, 1, -1, 0, 0, 0, 0, 0)
    pixiText.style.fontSize = fontSize
    pixiText.style.fill = color.toCanvas
    pixiText
  }

  val textLayer = new PIXI.Container
  textLayer.addChild(textNode)
  val tnode = textLayer
  makeDone()

  override def realDraw(): Unit = {
    kojoWorld.addLayer(textLayer)
  }

  def erase(): Unit = {
    kojoWorld.removeLayer(textLayer)
  }

  override def setFillColor(c: Color): Unit = {
    textNode.style.fill = c.toCanvas
    kojoWorld.render()
  }

  override def setPenColor(c: Color): Unit = {
    textNode.style.fill = c.toCanvas
    kojoWorld.render()
  }

  override def setPenThickness(t: Double): Unit = {
    textNode.style.strokeThickness = t
    kojoWorld.render()
  }

  override def initGeom(): Geometry = {
    null
  }

  def update(text: Any): Unit = {
    textNode.text = text.toString
    kojoWorld.render()
  }

  def copy: Picture = new TextPic(text, fontSize, color)
}
