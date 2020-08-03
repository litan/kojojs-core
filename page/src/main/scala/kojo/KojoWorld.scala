package kojo

import java.util.Random

import com.vividsolutions.jts.geom.Coordinate
import kojo.doodle.Color
import org.scalajs.dom.raw.{KeyboardEvent, UIEvent}
import org.scalajs.dom.{document, html, window}
import pixiscalajs.PIXI
import pixiscalajs.PIXI.{Point, Rectangle, RendererOptions}
import pixiscalajs.PIXI.interaction.InteractionData

import scala.scalajs.js

trait KojoWorld {
  def canvasWidth: Double
  def canvasHeight: Double
  def addLayer(layer: PIXI.Container): Unit
  def removeLayer(layer: PIXI.Container): Unit
  def scheduleLater(fn: => Unit): Unit
  def runLater(ms: Double)(fn: => Unit): Unit
  def render(): Unit
  def moveToFront(obj: PIXI.DisplayObject): Unit
  def moveToBack(obj: PIXI.DisplayObject): Unit

  def setBackground(color: Color): Unit
  def animate(fn: => Unit): Unit
  def timer(ms: Long)(fn: => Unit): Unit
  def stopAnimation(): Unit
  def setup(fn: => Unit): Unit

  def drawStage(fillc: Color)(implicit kojoWorld: KojoWorld)
  def bounceVecOffStage(v: Vector2D, p: Picture): Vector2D
  def bouncePicVectorOffPic(pic: Picture, vel: Vector2D, obstacle: Picture, rg: Random): Vector2D
  def stageBorder: Picture
  def stageLeft: Picture
  def stageTop: Picture
  def stageRight: Picture
  def stageBot: Picture
  def stageArea: Picture

  def isKeyPressed(keyCode: Int): Boolean
  def stagePosition: Point
  def positionOnStage(data: InteractionData): Point
  def isAMouseButtonPressed: Boolean
  def mouseMoveOnlyWhenInside(on: Boolean): Unit
  def size(width: Double, height: Double): Unit
  def zoomXY(xfactor: Double, yfactor: Double, cx: Double, cy: Double): Unit
  def mouseXY: Point
  def erasePictures(): Unit
  def toggleFullScreenCanvas(): Unit
  def canvasBounds: Rectangle
}

class KojoWorldImpl extends KojoWorld {
  PIXI.Pixi
  private val fiddleContainer =
    document.getElementById("fiddle-container").asInstanceOf[html.Div]
  private val canvas_holder =
    document.getElementById("canvas-holder").asInstanceOf[html.Div]
  val margin = 4.0
  var canvasWidth = fiddleContainer.clientWidth - margin
  var canvasHeight = fiddleContainer.clientHeight - margin
  var canvasOriginX = -canvasWidth / 2
  var canvasOriginY = -canvasHeight / 2
  val screenWidth = canvasWidth
  val screenHeight = canvasHeight
  private val renderer = PIXI.Pixi.autoDetectRenderer(canvasWidth, canvasHeight, rendererOptions(), noWebGL = false)
  private val interaction = renderer.plugins.interaction
  private val stage = new PIXI.Container()
  window.addEventListener("resize", resize)
  init()

  def init() {
    canvas_holder.appendChild(renderer.view)
    render()
    stage.name = "Stage"
    //    stage.width = canvasWidth
    //    stage.height = canvasHeight
    stage.interactive = true
    stage.setTransform(canvasWidth / 2, canvasHeight / 2, 1, -1, 0, 0, 0, 0, 0)
    mouseMoveOnlyWhenInside(true)
    initEvents()
  }

  def toggleFullScreenCanvas(): Unit = {
    try {
      import org.scalajs.dom.experimental.Fullscreen._
      if (window.document.fullscreenElement == null) {
        fiddleContainer.requestFullscreen()
      }
      else {
        window.document.exitFullscreen()
      }
    }
    catch {
      case _: Throwable =>
    }
  }

  def size(w: Double, h: Double): Unit = {
    canvasWidth = w
    canvasHeight = h
    canvasOriginX = -canvasWidth / 2
    canvasOriginY = -canvasHeight / 2
    //    stage.width = w
    //    stage.height = h
    renderer.resize(w, h)
    stage.setTransform(canvasWidth / 2, canvasHeight / 2, 1, -1, 0, 0, 0, 0, 0)
    render()
  }

  def resize(event: UIEvent): Unit = {
    size(fiddleContainer.clientWidth - margin, fiddleContainer.clientHeight - margin)
  }

  //  def originAt(x: Double, y: Double): Unit = {
  //    stage.setTransform(x, y, 1, -1, 0, 0, 0, 0, 0)
  //    render()
  //  }
  //

  def zoomXY(xfactor: Double, yfactor: Double, cx: Double, cy: Double): Unit = {
    //    stage.setTransform(width / 2 - cx, height / 2 + cy, xfactor, -yfactor, 0, 0, 0, 0, 0)
    stage.scale.set(xfactor, -yfactor)
    stage.position.set(screenWidth / 2 - cx * xfactor, screenHeight / 2 + cy * yfactor)
    canvasWidth = screenWidth / xfactor
    canvasHeight = screenHeight / yfactor.abs
    canvasOriginX = cx - canvasWidth / 2
    canvasOriginY = cy - canvasHeight / 2
    render()
  }

  def canvasBounds: Rectangle = {
    new Rectangle(canvasOriginX, canvasOriginY, canvasWidth, canvasHeight)
  }

  def addLayer(layer: PIXI.Container): Unit = {
    stage.addChild(layer)
    render()
  }

  def removeLayer(layer: PIXI.Container): Unit = {
    stage.removeChild(layer)
    render()
  }

  def erasePictures(): Unit = {
    val children = stage.children.toBuffer
    children.foreach { c =>
      if (c.name != "Turtle Layer") {
        stage.removeChild(c)
      }
    }
    render()
  }

  val MaxBurst = 100
  var burstCount = 0
  def scheduleLater(fn: => Unit): Unit = {
    burstCount += 1
    if (burstCount < MaxBurst) {
      fn
    }
    else {
      window.setTimeout(() => fn, 0)
      burstCount = 0
    }
  }

  def runLater(ms: Double)(fn: => Unit): Unit = {
    window.setTimeout(() => fn, ms)
  }

  def render(): Unit = {
    renderer.render(stage)
  }

  def moveToFront(obj: PIXI.DisplayObject): Unit = {
    val c = stage.removeChild(obj)
    stage.addChild(c)
    render()
  }

  def moveToBack(obj: PIXI.DisplayObject): Unit = {
    val c = stage.removeChild(obj)
    stage.addChildAt(c, 0)
    render()
  }

  def rendererOptions(
    antialias:         Boolean = true,
    resolution:        Double  = 1,
    backgroundColor:   Int     = 0xFFFFFF,
    clearBeforeRender: Boolean = true
  ): RendererOptions = {
    js.Dynamic
      .literal(
        antialias = antialias,
        resolution = resolution,
        backgroundColor = backgroundColor,
        clearBeforeRender = clearBeforeRender
      )
      .asInstanceOf[RendererOptions]
  }

  def setBackground(color: Color): Unit = {
    renderer.backgroundColor = color.toRGBDouble
  }

  var animating = false
  def notAssetLoading = !AssetLoader.loading
  var timers = Vector.empty[Int]

  def animate(fn: => Unit): Unit = {
    animating = true
    animateHelper(fn)
  }

  def animateHelper(fn: => Unit): Unit = {
    window.requestAnimationFrame { t =>
      if (notAssetLoading) {
        fn
      }
      if (animating) {
        animateHelper(fn)
      }
    }
  }

  def setup(fn: => Unit): Unit = {
    window.requestAnimationFrame { _ =>
      if (notAssetLoading) {
        fn
      }
      else {
        setup(fn)
      }
    }
  }

  def stopAnimation(): Unit = {
    animating = false
    timers foreach { t =>
      window.clearInterval(t)
    }
    timers = Vector.empty[Int]
  }

  def timer(ms: Long)(fn: => Unit): Unit = {
    val handle = window.setInterval({ () =>
      if (notAssetLoading) {
        fn
      }
    }, ms)
    timers = timers :+ handle
  }

  lazy val noPic = TurtlePicture { t =>
  }(this)
  @volatile var stageBorder: Picture = _
  @volatile var stageLeft: Picture = _
  @volatile var stageTop: Picture = _
  @volatile var stageRight: Picture = _
  @volatile var stageBot: Picture = _
  @volatile var stageArea: Picture = _

  def clearStage() {
    stageBorder = noPic
    stageLeft = noPic
    stageTop = noPic
    stageRight = noPic
    stageBot = noPic
  }

  def drawStage(fillc: Color)(implicit kojoWorld: KojoWorld) {
    def left(size: Double) = TurtlePicture { t =>
      t.setPenThickness(0)
      t.forward(size)
    }
    def top(size: Double) = TurtlePicture { t =>
      t.setPenThickness(0)
      t.right()
      t.forward(size)
    }
    def right(size: Double) = TurtlePicture { t =>
      t.setPenThickness(0)
      t.right(180)
      t.forward(size)
    }
    def bottom(size: Double) = TurtlePicture { t =>
      t.setPenThickness(0)
      t.left()
      t.forward(size)
    }

    val cb = canvasBounds

    stageLeft = left(canvasHeight)
    stageLeft.translate(cb.x, cb.y)

    stageTop = top(canvasWidth)
    stageTop.translate(cb.x, cb.y + cb.height)

    stageRight = right(canvasHeight)
    stageRight.translate(cb.x + cb.width, cb.y + cb.height)

    stageBot = bottom(canvasWidth)
    stageBot.translate(cb.x + cb.width, cb.y)

    stageArea = TurtlePicture { t =>
      t.setFillColor(fillc)
      t.setPenColor(Color.darkGray)
      for (_ <- 1 to 2) {
        t.forward(canvasHeight)
        t.right()
        t.forward(canvasWidth)
        t.right()
      }
    }
    stageArea.translate(cb.x, cb.y)

    stageBorder = GPics(
      stageLeft,
      stageTop,
      stageRight,
      stageBot
    )

    stageArea.draw()
    stageBorder.draw()
  }

  def bounceVecOffStage(v: Vector2D, p: Picture): Vector2D = {
    val topCollides = p.collidesWith(stageTop)
    val leftCollides = p.collidesWith(stageLeft)
    val botCollides = p.collidesWith(stageBot)
    val rightCollides = p.collidesWith(stageRight)

    val c = v.magnitude / math.sqrt(2)
    if (topCollides && leftCollides)
      Vector2D(c, -c)
    else if (topCollides && rightCollides)
      Vector2D(-c, -c)
    else if (botCollides && leftCollides)
      Vector2D(c, c)
    else if (botCollides && rightCollides)
      Vector2D(-c, c)
    else if (topCollides)
      Vector2D(v.x, -v.y)
    else if (botCollides)
      Vector2D(v.x, -v.y)
    else if (leftCollides)
      Vector2D(-v.x, v.y)
    else if (rightCollides)
      Vector2D(-v.x, v.y)
    else
      v
  }

  def collidesWithStage(p: Picture): Boolean = {
    val stageparts = List(stageTop, stageBot, stageLeft, stageRight)
    p.collision(stageparts).isDefined
  }

  def bouncePicVectorOffPic(pic: Picture, vel: Vector2D, obstacle: Picture, rg: Random): Vector2D = {
    // returns points on the obstacle that contain the given collision coordinate
    def obstacleCollPoints(c: Coordinate): Option[js.Array[Coordinate]] = {
      obstacle.picGeom.getCoordinates.sliding(2).find { cs =>
        val xcheck = if (cs(0).x > cs(1).x)
          cs(0).x >= c.x && c.x >= cs(1).x
        else
          cs(0).x <= c.x && c.x <= cs(1).x

        val ycheck = if (cs(0).y > cs(1).y)
          cs(0).y >= c.y && c.y >= cs(1).y
        else
          cs(0).y <= c.y && c.y <= cs(1).y
        xcheck && ycheck
      }
    }
    // returns vector for obstacle boundary segment that contains the collision point
    def obstacleCollVector(c: Coordinate) = makeVectorFromCollPoints(obstacleCollPoints(c))

    // creates a vector out of two (collision) points
    def makeVectorFromCollPoints(cps: Option[js.Array[Coordinate]]) = cps match {
      case Some(cs) =>
        Vector2D(cs(0).x - cs(1).x, cs(0).y - cs(1).y)
      case None =>
        println("Warning: unable to determine collision vector; generating random vector")
        Vector2D(rg.nextDouble, rg.nextDouble)
    }

    def collisionVector = {
      val pt = obstacle.intersection(pic)
      val iCoords = pt.getCoordinates

      if (iCoords.length == 0) {
        Vector2D(rg.nextDouble, rg.nextDouble).normalize
      }
      else {
        if (iCoords.length == 1) {
          val cv1 = obstacleCollVector(iCoords(0))
          cv1.normalize
        }
        else {
          val c1 = iCoords(0)
          val c2 = iCoords(iCoords.length - 1)
          makeVectorFromCollPoints(Some(js.Array(c1, c2))).normalize
        }
      }
    }
    def pullbackCollision() = {
      val velNorm = vel.normalize
      val v2 = velNorm.rotate(180)
      val velMag = vel.magnitude
      var pulled = 0
      while (pic.collidesWith(obstacle) && pulled < velMag) {
        pic.offset(v2)
        pulled += 1
      }
      pic.offset(velNorm)
    }

    pullbackCollision()
    val cv = collisionVector
    vel.bounceOff(cv)
  }

  val pressedKeys = new collection.mutable.HashSet[Int]

  def initEvents(): Unit = {
    def keyDown(e: KeyboardEvent): Unit = {
      pressedKeys.add(e.keyCode)
    }
    def keyUp(e: KeyboardEvent): Unit = {
      pressedKeys.remove(e.keyCode)
    }
    window.addEventListener("keydown", keyDown(_), false)
    window.addEventListener("keyup", keyUp(_), false)
  }

  def isKeyPressed(keyCode: Int) = pressedKeys.contains(keyCode)
  def stagePosition = stage.position
  def positionOnStage(data: InteractionData) = data.getLocalPosition(stage)
  def isAMouseButtonPressed = interaction.mouse.buttons > 0
  def mouseMoveOnlyWhenInside(on: Boolean): Unit = {
    interaction.moveWhenInside = on
  }
  def mouseXY = interaction.mouse.getLocalPosition(stage)
}
