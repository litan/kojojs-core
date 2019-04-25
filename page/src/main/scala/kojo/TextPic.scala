package kojo

import com.vividsolutions.jts.geom.Geometry

import kojo.doodle.Color
import pixiscalajs.PIXI

class TextPic(text: Any, fontSize: Int, color: Color)(implicit val turtleWorld: TurtleWorld) extends Picture {
  val textNode = {
    val pixiText = new PIXI.Text(text.toString)
    pixiText.setTransform(0, 0, 1, -1, 0, 0, 0, 0, 0)
    pixiText.style.fontSize = fontSize
    pixiText.style.fill = color.toCanvas
    pixiText
  }

  var made = true

  val textLayer = new PIXI.Container
  textLayer.addChild(textNode)
  val tnode = textLayer

  override def realDraw(): Unit = {
    turtleWorld.addTurtleLayer(textLayer)
  }

  def erase(): Unit = {
    turtleWorld.removeTurtleLayer(textLayer)
  }

  override def setFillColor(c: Color): Unit = {
    textNode.style.fill = c.toCanvas
    turtleWorld.render()
  }

  override def setPenColor(c: Color): Unit = {
    textNode.style.stroke = c.toCanvas
    turtleWorld.render()
  }

  override def setPenThickness(t: Double): Unit = {
    textNode.style.strokeThickness = t
    turtleWorld.render()
  }

  override def initGeom(): Geometry = {
    null
  }

  def update(text: Any): Unit = {
    textNode.text = text.toString
    turtleWorld.render()
  }
}
