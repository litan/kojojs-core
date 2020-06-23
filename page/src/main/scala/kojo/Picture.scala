package kojo

import com.vividsolutions.jts.geom.{AffineTransformation, Geometry}
import kojo.doodle.Color
import pixiscalajs.PIXI
import pixiscalajs.PIXI.{Matrix, Point}
import pixiscalajs.PIXI.interaction.InteractionEvent

import scala.concurrent.{Future, Promise}

trait Picture {
  def made: Boolean
  def ready: Future[Unit]

  def tnode: PIXI.DisplayObject
  def bounds = Utils.transformRectangle(tnode.getLocalBounds(), tnode.localTransform)

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
  def position: Point = tnode.position
  def heading = tnode.rotation.toDegrees
  def setOpacity(opac: Double) {
    tnode.alpha = opac
    kojoWorld.render()
  }

  def forwardInputTo(other: Picture): Unit = {

  }

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

  def rotateAboutPoint(angle: Double, x: Double, y: Double): Unit = {
    translate(x, y)
    rotate(angle)
    translate(-x, -y)
  }

  def setHeading(angle: Double): Unit = {
    val angleRads = Utils.deg2radians(angle)
    tnode.rotation = angleRads
    tnode.rotation = tnode.rotation % (math.Pi * 2)
    transformDone()
  }

  def setRotation(angle: Double) = setHeading(angle)

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

  def setScale(f: Double): Unit = {
    tnode.scale.set(f, f)
    transformDone()
  }

  def setPosition(x: Double, y: Double): Unit = {
    tnode.position.set(x, y)
    transformDone()
  }

  def setPosition(p: Point): Unit = {
    setPosition(p.x, p.y)
  }

  def setFillColor(c: Color): Unit
  def setPenColor(c: Color): Unit
  def setPenThickness(t: Double): Unit

  private var _picGeom: Geometry = _
  protected var _pgTransform: AffineTransformation = _

  def pgTransform = {
    if (_pgTransform == null) {
      _pgTransform = t2t(tnode.localTransform)
    }
    _pgTransform
  }

  def updateGeomTransform(): Unit = {
    _pgTransform = null // t2t(tnode.localTransform)
  }

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
    tnode.on("pointerdown", handler)
  }

  def onMouseRelease(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    val handler = handlerWrapper(fn)(_)
    tnode.on("pointerup", handler)
    tnode.on("pointerupoutside", handler)
  }

  def onMouseClick(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    val handler = handlerWrapper(fn)(_)
    tnode.on("pointertap", handler)
  }

  def onMouseMove(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    val moveWrapper: (Double, Double) => Unit = { (x, y) =>
      if (!kojoWorld.isAMouseButtonPressed) {
        fn(x, y)
      }
    }
    val handler = handlerWrapper(moveWrapper)(_)
    tnode.on("pointermove", handler)
  }

  private var mousePressed = false
  def onMouseDrag(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true

    onMousePress { (_, _) =>
      mousePressed = true
      kojoWorld.mouseMoveOnlyWhenInside(false)
    }

    onMouseRelease { (_, _) =>
      mousePressed = false
      kojoWorld.mouseMoveOnlyWhenInside(true)
    }

    val moveWrapper: (Double, Double) => Unit = { (x, y) =>
      if (mousePressed) {
        fn(x, y)
      }
    }

    val handler = handlerWrapper(moveWrapper, false)(_)
    tnode.on("pointermove", handler)
  }

  def onMouseEnter(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    val handler = handlerWrapper(fn)(_)
    tnode.on("pointerover", handler)
  }

  def onMouseExit(fn: (Double, Double) => Unit): Unit = {
    tnode.interactive = true
    val handler = handlerWrapper(fn)(_)
    tnode.on("pointerout", handler)
  }
}

trait ReadyPromise { self: Picture =>
  private var _made = false
  private val readyPromise = Promise[Unit]()

  def made: Boolean = _made

  def ready: Future[Unit] = {
    readyPromise.future
  }

  protected def makeDone(): Unit = {
    _made = true
    readyPromise.success(())
  }
}
