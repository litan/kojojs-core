package kojo

import scala.collection.mutable.ArrayBuffer

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.PrecisionModel

import kojo.doodle.Color
import pixiscalajs.PIXI
class ImagePic(url: String)(implicit val turtleWorld: TurtleWorld) extends Picture {
  var made = false
  val imgLayer = new PIXI.Container
  var sprite: PIXI.Sprite = _
  val tnode = imgLayer

  TurtleImageHelper.addAndLoad(url, url, init)

  private def init(loader: PIXI.loaders.Loader, any: Any): Unit = {
    sprite = new PIXI.Sprite(loader.resources(url).texture)
    sprite.setTransform(0, sprite.height, 1, -1, 0, 0, 0, 0, 0)
    imgLayer.addChild(sprite)
    made = true
    turtleWorld.render()
  }

  override def realDraw(): Unit = {
    turtleWorld.addTurtleLayer(imgLayer)
    turtleWorld.render()
  }

  def erase(): Unit = {
    turtleWorld.removeTurtleLayer(imgLayer)
    turtleWorld.render()
  }

  override def setFillColor(c: Color): Unit = {
  }

  override def setPenColor(c: Color): Unit = {
  }

  override def setPenThickness(t: Double): Unit = {
  }

  lazy val pmodel = new PrecisionModel(14)
  def newCoordinate(x: Double, y: Double) = {
    val coord = new Coordinate(x, y)
    pmodel.makePrecise(coord)
    coord
  }

  override def initGeom(): Geometry = {
    val cab = new ArrayBuffer[Coordinate]
    val bounds = sprite.getLocalBounds()
    cab += newCoordinate(bounds.x, bounds.y)
    cab += newCoordinate(bounds.x, bounds.y + bounds.height)
    cab += newCoordinate(bounds.x + bounds.width, bounds.y + bounds.height)
    cab += newCoordinate(bounds.x + bounds.width, bounds.y)
    cab += newCoordinate(bounds.x, bounds.y)
    import scala.scalajs.js.JSConverters._
//    pgTransform.getInverse().transform(Gf.createLineString(cab.toJSArray))
    Gf.createLineString(cab.toJSArray)
  }
}
