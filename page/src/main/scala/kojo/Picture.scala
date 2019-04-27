package kojo

import com.vividsolutions.jts.geom.AffineTransformation
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.LineString

import kojo.doodle.Color
import pixiscalajs.PIXI
import pixiscalajs.PIXI.Matrix
import pixiscalajs.PIXI.Point
import pixiscalajs.PIXI.interaction.InteractionEvent

trait Picture {
  def tnode: PIXI.DisplayObject
  def made: Boolean
  def updateGeomTransform(): Unit = {
    pgTransform = t2t(tnode.localTransform)
  }
  def realDraw(): Unit
  def draw(): Unit = {
    realDraw()
    updateGeomTransform()
  }

  def invisible(): Unit = {
    tnode.visible = false
    turtleWorld.render()
  }
  def visible(): Unit = {
    tnode.visible = true
    turtleWorld.render()
  }
  def isVisible = tnode.visible

  def erase(): Unit
  def moveToFront() = turtleWorld.moveToFront(tnode)
  def moveToBack() = turtleWorld.moveToBack(tnode)
  def position = tnode.position
  def setOpacity(opac: Double) {
    tnode.alpha = opac
    turtleWorld.render()
  }

  def forwardInputTo(other: Picture): Unit = {

  }

  var pgTransform = new AffineTransformation
  def turtleWorld: TurtleWorld

  private def t2t(t: Matrix): AffineTransformation = {
    import scala.scalajs.js.JSConverters._
    val ms2 = Array.fill(6)(0.0).toJSArray
    ms2(0) = t.a // m00
    ms2(1) = t.c // m01
    ms2(2) = t.tx // m02
    ms2(3) = t.b // m10
    ms2(4) = t.d // m11
    ms2(5) = t.ty // m12
    new AffineTransformation(ms2)
  }

  private def transformDone() = {
    preDrawHook()
    turtleWorld.render()
    updateGeomTransform()
  }

  private def preDrawHook() = {
    if (tnode.parent == null) {
      tnode.transform.onChange()
      tnode.transform.updateLocalTransform()
    }
  }

  def offset(v: Vector2D): Unit = {
    offset(v.x, v.y)
  }

  def translate(v: Vector2D): Unit = {
    translate(v.x, v.y)
  }

  def scale(f: Double): Unit = {
    scale(f, f)
  }

  def rotate(angle: Double): Unit = {
    val angleRads = Utils.deg2radians(angle)
    tnode.rotation += angleRads
    transformDone()
  }

  def translate(dx: Double, dy: Double): Unit = {
    val pos = tnode.position
    val transform = tnode.localTransform
    val localPos = transform.applyInverse(pos)
    localPos.set(localPos.x + dx, localPos.y + dy)
    val globalPos = transform.apply(localPos)
    pos.set(globalPos.x, globalPos.y)
    transformDone()
  }

  def offset(dx: Double, dy: Double): Unit = {
    val pos = tnode.position
    pos.set(pos.x + dx, pos.y + dy)
    transformDone()
  }

  def scale(fx: Double, fy: Double): Unit = {
    tnode.scale = Point(fx, fy)
    transformDone()
  }

  def setPosition(x: Double, y: Double): Unit = {
    tnode.position.set(x, y)
    transformDone()
  }

  def setFillColor(c: Color): Unit
  def setPenColor(c: Color): Unit
  def setPenThickness(t: Double): Unit

  var _picGeom: Geometry = _
  def initGeom(): Geometry
  def picGeom: Geometry = {
    if (!made) {
      return null
    }

    if (_picGeom == null) {
      try {
        _picGeom = initGeom()
      }
      catch {
        case ise: IllegalStateException =>
          throw ise
        case t: Throwable =>
          throw new IllegalStateException("Unable to create geometry for picture - " + t.getMessage, t)
      }
    }
    // TODO: next step is to support pgTransform
    pgTransform.transform(_picGeom)
  }

  def collidesWith(other: Picture): Boolean = {
    if (picGeom == null || other.picGeom == null) {
      false
    }
    else {
      val ret = picGeom.intersects(other.picGeom)
      ret
    }
  }

  def collision(others: Seq[Picture]): Option[Picture] = {
    others.find { this collidesWith _ }
  }

  def intersection(other: Picture): Geometry = {
    picGeom.intersection(other.picGeom)
  }

  def handlerWrapper(fn: (Double, Double) => Unit)(event: InteractionEvent): Unit = {
    val pos = event.data.getLocalPosition(turtleWorld.stage)
    fn(pos.x, pos.y)
  }

  def onMousePress(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    tnode.on("mousedown", handlerWrapper(fn) _)
  }

  def onMouseRelease(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    tnode.on("mouseup", handlerWrapper(fn) _)
  }

//  def onMouseClick(fn: (Double, Double) => Unit): Unit = {
//    tnode.interactive = true
//    tnode.on("mouseclick", handlerWrapper(fn) _)
//  }
//
//  def onMouseMove(fn: (Double, Double) => Unit): Unit = {
//    tnode.interactive = true
//    tnode.on("mousemove", handlerWrapper(fn) _)
//  }
//
//  def onMouseEnter(fn: (Double, Double) => Unit): Unit = {
//    tnode.interactive = true
//    tnode.on("mouseenter", handlerWrapper(fn) _)
//  }
//  def onMouseExit(fn: (Double, Double) => Unit): Unit = {
//    tnode.interactive = true
//    tnode.on("mouseleave", handlerWrapper(fn) _)
//  }
}
