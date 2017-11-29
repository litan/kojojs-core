package kojo

import org.scalajs.dom.{document, html, window}
import pixiscalajs.PIXI
import pixiscalajs.PIXI.{CanvasRenderer, Pixi, RendererOptions}

import scala.collection.mutable
import scala.scalajs.js

object Kojo {

  object RepeatCommands extends RepeatCommands

  trait RepeatCommands {
    def repeat(n: Int)(fn: => Unit) {
      var i = 0
      while (i < n) {
        fn
        i += 1
      }
    }

    def repeati(n: Int)(fn: Int => Unit) {
      var i = 0
      while (i < n) {
        fn(i + 1)
        i += 1
      }
    }

    def repeatWhile(cond: => Boolean)(fn: => Unit) {
      while (cond) {
        fn
      }
    }

    def repeatUntil(cond: => Boolean)(fn: => Unit) {
      while (!cond) {
        fn
      }
    }

    def repeatFor[T](seq: Iterable[T])(fn: T => Unit) {
      val iter = seq.iterator
      while (iter.hasNext) {
        fn(iter.next)
      }
    }
  }

  object Utils {
    def doublesEqual(d1: Double, d2: Double, tol: Double): Boolean = {
      if (d1 == d2) return true
      else if (math.abs(d1 - d2) < tol) return true
      else return false
    }

    def deg2radians(angle: Double) = angle * math.Pi / 180

    def rad2degrees(angle: Double) = angle * 180 / math.Pi
  }

  object TurtleHelper {

    def posAfterForward(x: Double, y: Double, theta: Double, n: Double): (Double, Double) = {
      val delX = math.cos(theta) * n
      val delY = math.sin(theta) * n
      (x + delX, y + delY)
    }

    def thetaTowards(px: Double, py: Double, x: Double, y: Double, oldTheta: Double): Double = {
      val (x0, y0) = (px, py)
      val delX     = x - x0
      val delY     = y - y0
      if (Utils.doublesEqual(delX, 0, 0.001)) {
        if (Utils.doublesEqual(delY, 0, 0.001)) oldTheta
        else if (delY > 0) math.Pi / 2
        else 3 * math.Pi / 2
      } else if (Utils.doublesEqual(delY, 0, 0.001)) {
        if (delX > 0) 0
        else math.Pi
      } else {
        var nt2 = math.atan(delY / delX)
        if (delX < 0 && delY > 0) nt2 += math.Pi
        else if (delX < 0 && delY < 0) nt2 += math.Pi
        else if (delX > 0 && delY < 0) nt2 += 2 * math.Pi
        nt2
      }
    }

    def thetaAfterTurn(angle: Double, oldTheta: Double) = {
      var newTheta = oldTheta + Utils.deg2radians(angle)
      if (newTheta < 0) newTheta = newTheta % (2 * math.Pi) + 2 * math.Pi
      else if (newTheta > 2 * math.Pi) newTheta = newTheta % (2 * math.Pi)
      newTheta
    }

    def distance(x0: Double, y0: Double, x: Double, y: Double): Double = {
      val delX = x - x0
      val delY = y - y0
      math.sqrt(delX * delX + delY * delY)
    }

    def delayFor(dist: Double, animationDelay: Long): Long = {
      if (animationDelay < 1) {
        return animationDelay
      }

      // _animationDelay is delay for 100 steps;
      // Here we calculate delay for specified distance
      val speed = 100f / animationDelay
      val delay = Math.abs(dist) / speed
      delay.round
    }
  }

  class TurtleWorld {
    val fiddleContainer = document.getElementById("fiddle-container").asInstanceOf[html.Div]
    val canvas_holder   = document.getElementById("canvas-holder").asInstanceOf[html.Div]
    val (width, height) = (fiddleContainer.clientWidth, fiddleContainer.clientHeight)
    //    val renderer        = Pixi.autoDetectRenderer(width, height, rendererOptions())
    val renderer = new CanvasRenderer(width, height, rendererOptions())
    renderer.backgroundColor = 0xFFFFFF
    canvas_holder.appendChild(renderer.view)
    val stage = new PIXI.Container()
    stage.name = "Stage"
    init()

    def init() {
      stage.width = width
      stage.height = height
      stage.interactive = true
      stage.setTransform(width / 2, height / 2, 1, -1, 0, 0, 0, 0, 0)
    }

    def addTurtleLayer(layer: PIXI.Container): Unit = {
      stage.addChild(layer)
    }

    def scheduleLater(fn: () => Unit): Unit = {
      window.setTimeout(fn, 0)
    }

    def addSprite(sprite: PIXI.Sprite): Unit = {
      def endFrame(): Unit = {
        stage.addChild(sprite)
        render()
      }

      def frame(): Unit = {
        scheduleLater(endFrame)
        render()
      }

      scheduleLater(frame)
    }

    def render(): Unit = {
      renderer.render(stage)
    }

    def rendererOptions(antialias: Boolean = true, resolution: Double = 1): RendererOptions = {
      js.Dynamic.literal(antialias = antialias, resolution = resolution).asInstanceOf[RendererOptions]
    }
  }

  sealed trait Command

  case class Forward(n: Double) extends Command

  case class Turn(angle: Double) extends Command

  class Turtle(x: Double, y: Double)(implicit turtleWorld: TurtleWorld) {
    val commandQ = mutable.Queue.empty[Command]

    val turtleLayer                 = new PIXI.Container()
    var turtleImage: PIXI.Container = _
    val turtlePath                  = new PIXI.Graphics()

    Pixi.loader.add("turtle32", "assets/images/turtle32.png").load(init)

    def init(loader: PIXI.loaders.Loader, any: Any) {
      turtleLayer.name = "Turtle Layer"
      turtleWorld.addTurtleLayer(turtleLayer)
      turtleImage = loadTurtle(x, y, loader)
      turtleImage.name = "Turtle Icon"

      var penWidth       = 2
      var penColor       = 0x0000FF
      var animationDelay = 1000l

      turtlePath.name = "Turtle Path"
      turtlePath.lineStyle(penWidth, penColor, 1)
      turtlePath.moveTo(x, y)
      turtleLayer.addChild(turtlePath)
      turtleLayer.addChild(turtleImage)
      turtleWorld.scheduleLater(queueHandler)
    }

    def position = turtleImage.position

    def headingRadians = turtleImage.rotation

    def heading = Utils.rad2degrees(headingRadians)

    def loadTurtle(x: Double, y: Double, loader: PIXI.loaders.Loader): PIXI.Container = {
      val turtle = {
        val rasterTurtle = new PIXI.Sprite(loader.resources("turtle32").texture)
        rasterTurtle.position.set(-16, -16)
        rasterTurtle.alpha = 0.7
        rasterTurtle
      }
      val turtleHolder = new PIXI.Container()
      turtleHolder.addChild(turtle)
      turtleHolder.position.set(x, y)
      turtleHolder.rotation = Utils.deg2radians(90)
      turtleHolder
    }

    def forward(n: Double): Unit = {
      commandQ.enqueue(Forward(n))
    }

    def left(angle: Double): Unit = {
      commandQ.enqueue(Turn(angle))
    }

    def right(angle: Double) = left(-angle)

    def queueHandler(): Unit = {
      if (commandQ.size > 0) {
        commandQ.dequeue() match {
          case Forward(n)  => forwardImpl(n)
          case Turn(angle) => leftImpl(angle)
        }
      }
    }

    def forwardImpl(n: Double) {
      def forwardFrame(): Unit = {
        val p0x        = position.x
        val p0y        = position.y
        val (pfx, pfy) = TurtleHelper.posAfterForward(p0x, p0y, headingRadians, n)
        turtlePath.lineTo(pfx, pfy)
        //        turtlePath.dirty += 1
        turtlePath.clearDirty += 1
        turtleImage.position.x = pfx
        turtleImage.position.y = pfy
        turtleWorld.render()
        turtleWorld.scheduleLater(queueHandler)
      }

      forwardFrame()
    }

    def leftImpl(angle: Double): Unit = {

      def leftFrame(): Unit = {
        val angleRads = Utils.deg2radians(angle)
        turtleImage.rotation += angleRads
        turtleWorld.render()
        turtleWorld.scheduleLater(queueHandler)
      }

      leftFrame()
    }

  }

}
