package kojo

import scala.concurrent.Future
import scala.concurrent.Promise

import com.vividsolutions.jts.geom.AffineTransformation
import com.vividsolutions.jts.geom.Geometry

import kojo.doodle.Color
import pixiscalajs.PIXI
import pixiscalajs.PIXI.Matrix
import pixiscalajs.PIXI.Point
import pixiscalajs.PIXI.interaction.InteractionEvent

trait Picture {
  def made: Boolean
  def ready: Future[Unit]

  def tnode: PIXI.DisplayObject
  def bounds = Utils.transformRectangle(tnode.getLocalBounds(), tnode.localTransform)

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
    kojoWorld.render()
  }
  def visible(): Unit = {
    tnode.visible = true
    kojoWorld.render()
  }
  def isVisible = tnode.visible

  def erase(): Unit
  def moveToFront() = kojoWorld.moveToFront(tnode)
  def moveToBack() = kojoWorld.moveToBack(tnode)
  def position = tnode.position
  def heading = tnode.rotation.toDegrees
  def setOpacity(opac: Double) {
    tnode.alpha = opac
    kojoWorld.render()
  }

  def forwardInputTo(other: Picture): Unit = {

  }

  var pgTransform = new AffineTransformation
  def kojoWorld: KojoWorld

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
    kojoWorld.render()
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
    tnode.rotation = tnode.rotation % (math.Pi * 2)
    transformDone()
  }

  def setHeading(angle: Double): Unit = {
    val angleRads = Utils.deg2radians(angle)
    tnode.rotation = angleRads
    tnode.rotation = tnode.rotation % (math.Pi * 2)
    transformDone()
  }

  def translate(dx: Double, dy: Double): Unit = {
    val transform = tnode.localTransform
    val localPos = Point(dx, dy)
    val parentPos = transform.apply(localPos)
    tnode.position.set(parentPos.x, parentPos.y)
    transformDone()
  }

  def offset(dx: Double, dy: Double): Unit = {
    val pos = tnode.position
    pos.set(pos.x + dx, pos.y + dy)
    transformDone()
  }

  def scale(fx: Double, fy: Double): Unit = {
    val scale = tnode.scale
    scale.set(scale.x * fx, scale.y * fy)
    transformDone()
  }

  def setPosition(x: Double, y: Double): Unit = {
    tnode.position.set(x, y)
    transformDone()
  }

  def setFillColor(c: Color): Unit
  def setPenColor(c: Color): Unit
  def setPenThickness(t: Double): Unit

  private var _picGeom: Geometry = _
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

  def collisions(others: Set[Picture]): Set[Picture] = {
    others.filter { this collidesWith _ }
  }

  def intersection(other: Picture): Geometry = {
    picGeom.intersection(other.picGeom)
  }

  def handlerWrapper(fn: (Double, Double) => Unit, stop: Boolean = true)(event: InteractionEvent): Unit = {
    val pos = kojoWorld.positionOnStage(event.data)
    if (stop) {
      event.stopPropagation()
    }
    fn(pos.x, pos.y)
  }

  def onMousePress(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    val handler = handlerWrapper(fn)(_)
    tnode.on("mousedown", handler)
    tnode.on("touchstart", handler)
  }

  def onMouseRelease(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    val handler = handlerWrapper(fn)(_)
    tnode.on("mouseup", handler)
    tnode.on("touchend", handler)
  }

  def onMouseClick(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    val handler = handlerWrapper(fn)(_)
    tnode.on("click", handler)
  }

  def onMouseMove(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    val moveWrapper: (Double, Double) => Unit = { (x, y) =>
      if (!kojoWorld.isAMouseButtonPressed) {
        fn(x, y)
      }
    }
    val handler = handlerWrapper(moveWrapper)(_)
    tnode.on("mousemove", handler)
  }

  private var dragMouseMonitored = false
  private var mousePressed = false
  private var dragEnd: Option[(Double, Double) => Unit] = None
  def onMouseDrag(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true

    def mouseReleased(x: Double, y: Double): Unit = {
      mousePressed = false
      dragEnd.foreach {
        _(x, y)
      }
      kojoWorld.runLater(0) {
        kojoWorld.mouseMoveOnlyWhenInside(true)
      }
    }

    if (!dragMouseMonitored) {
      dragMouseMonitored = true

      onMousePress { (x, y) =>
        mousePressed = true
      }

      onMouseRelease { (x, y) =>
        // catch mouse release inside component
        mouseReleased(x, y)
      }
    }

    val moveWrapper: (Double, Double) => Unit = { (x, y) =>
      if (kojoWorld.isAMouseButtonPressed) {
        if (mousePressed) {
          kojoWorld.mouseMoveOnlyWhenInside(false)
          fn(x, y)
        }
      }
      else {
        if (mousePressed) {
          // catch mouse release outside component
          mouseReleased(x, y)
        }
      }
    }
    val handler = handlerWrapper(moveWrapper, false)(_)
    tnode.on("mousemove", handler)
  }

  def onMouseDragEnd(fn: (Double, Double) => Unit): Unit = {
    dragEnd = Some(fn)
  }

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

trait ReadyPromise { self: Picture =>
  def made: Boolean = _made

  private var _made = false
  private val readyPromise = Promise[Unit]()
  def ready: Future[Unit] = {
    readyPromise.future
  }
  protected def makeDone(): Unit = {
    _made = true
    readyPromise.success(())
  }
}
