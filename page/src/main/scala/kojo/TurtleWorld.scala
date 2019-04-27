package kojo

import java.util.Random

import scala.scalajs.js

import org.scalajs.dom.document
import org.scalajs.dom.html
import org.scalajs.dom.raw.KeyboardEvent
import org.scalajs.dom.window

import com.vividsolutions.jts.geom.Coordinate

import kojo.doodle.Color
import pixiscalajs.PIXI
import pixiscalajs.PIXI.RendererOptions

class TurtleWorld {
  PIXI.Pixi
  val fiddleContainer =
    document.getElementById("fiddle-container").asInstanceOf[html.Div]
  val canvas_holder =
    document.getElementById("canvas-holder").asInstanceOf[html.Div]
  val (width, height) =
    (fiddleContainer.clientWidth, fiddleContainer.clientHeight)
  val renderer = PIXI.Pixi.autoDetectRenderer(width, height, rendererOptions(), noWebGL = true)
  val stage = new PIXI.Container()
  init()

  def init() {
    render()
    canvas_holder.appendChild(renderer.view)
    stage.name = "Stage"
    stage.width = width
    stage.height = height
    stage.interactive = true
    stage.setTransform(width / 2, height / 2, 1, -1, 0, 0, 0, 0, 0)
    initEvents()
  }

  def addTurtleLayer(layer: PIXI.Container): Unit = {
    stage.addChild(layer)
    render()
  }

  def removeTurtleLayer(layer: PIXI.Container): Unit = {
    stage.removeChild(layer)
    render()
  }

  val MaxBurst = 100
  var burstCount = 0
  def scheduleLater(fn: () => Unit): Unit = {
    burstCount += 1
    if (burstCount < MaxBurst) {
      fn()
    }
    else {
      window.setTimeout(fn, 0)
      burstCount = 0
    }
  }

  def runLater(ms: Double)(fn: () => Unit): Unit = {
    window.setTimeout(fn, ms)
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
  var loaded = false
  var timers = Vector.empty[Int]

  def animate(fn: => Unit): Unit = {
    animating = true
    animateHelper(fn)
  }

  def animateHelper(fn: => Unit): Unit = {
    if (!loaded && TurtleImageHelper.queue.isEmpty) {
      loaded = true
    }

    window.requestAnimationFrame { t =>
      if (loaded) {
        fn
      }
      if (animating) {
        animateHelper(fn)
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
      if (loaded) {
        fn
      }
    }, ms)
    timers = timers :+ handle
  }

  val noPic = TurtlePicture { t =>
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

  def drawStage(fillc: Color)(implicit turtleWorld: TurtleWorld) {
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

    val xmax = stage.position.x.abs
    val ymax = stage.position.y.abs

    stageLeft = left(height)
    stageLeft.translate(-xmax, -ymax)

    stageTop = top(width)
    stageTop.translate(-xmax, ymax)

    stageRight = right(height)
    stageRight.translate(xmax, ymax)

    stageBot = bottom(width)
    stageBot.translate(xmax, -ymax)

    stageArea = TurtlePicture { t =>
      t.setFillColor(fillc)
      t.setPenColor(Color.darkGray)
      for (_ <- 1 to 2) {
        t.forward(height)
        t.right()
        t.forward(width)
        t.right()
      }
    }
    stageArea.translate(-xmax, -ymax)

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
    val pt = pic.intersection(obstacle)
    val iCoords = pt.getCoordinates

    // returns points on the obstacle that contain the given collision coordinate
    def obstacleCollPoints(c: Coordinate): Option[js.Array[Coordinate]] = {
      obstacle.picGeom.getCoordinates.sliding(2).find { cs =>
        val xcheck =
          if (cs(0).x > cs(1).x)
            cs(0).x >= c.x && c.x >= cs(1).x
          else
            cs(0).x <= c.x && c.x <= cs(1).x

        val ycheck =
          if (cs(0).y > cs(1).y)
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
      //      pic.offset(velNorm * 2)
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
    window.addEventListener("keydown", keyDown, false)
    window.addEventListener("keyup", keyUp, false)
  }

  def isKeyPressed(keyCode: Int) = pressedKeys.contains(keyCode)
}
