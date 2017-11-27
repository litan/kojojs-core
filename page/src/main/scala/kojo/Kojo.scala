package kojo

import org.scalajs.dom.{document, html, window}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import pixiscalajs.PIXI
import pixiscalajs.PIXI.{Pixi, RendererOptions, CanvasRenderer}

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
    //    val renderer = new CanvasRenderer(800, 600, rendererOptions(canvas))
    val (width, height) = (fiddleContainer.clientWidth, fiddleContainer.clientHeight)
    val renderer        = Pixi.autoDetectRenderer(width, height, rendererOptions())
    canvas_holder.appendChild(renderer.view)
    val stage = new PIXI.Container()
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

    def render(): Unit = {
      renderer.render(stage)
    }

    def rendererOptions(antialias: Boolean = true, resolution: Double = 1): RendererOptions = {
      js.Dynamic.literal(antialias = antialias, resolution = resolution).asInstanceOf[RendererOptions]
    }
  }

  class Turtle(x: Double, y: Double)(implicit turtleWorld: TurtleWorld) {
    val turtleLayer = new PIXI.Container()
    turtleWorld.addTurtleLayer(turtleLayer)

    val turtleImage = loadTurtleImage(x, y)
    turtleLayer.addChild(turtleImage)

    var penWidth       = 2
    var penColor       = 0x0000FF
    var animationDelay = 1000l

    val turtlePath = new PIXI.Graphics()
    turtlePath.lineStyle(penWidth, penColor, 1)
    turtlePath.moveTo(x, y)
    turtleLayer.addChild(turtlePath)

    def position = turtleImage.position

    def heading = turtleImage.rotation

    def loadTurtleImage(x: Double, y: Double): PIXI.Graphics = {
      val turtleImage = new PIXI.Graphics()
      turtleImage.lineStyle(2, 0xFF0000, 1).beginFill(0xFF700B, 1)
      turtleImage.drawRect(-10, -10, 20, 20)
      turtleImage.endFill()
      turtleImage.position.set(x, y)
      turtleImage.rotation = Utils.deg2radians(90)
      turtleImage
    }

    def forward(n: Double) {
      def forwardEndFrame(frameTime: Double): Unit = {
        turtleLayer.addChild(turtlePath)
        turtleWorld.render()
      }

      def forwardFrame(frameTime: Double): Unit = {
        val p0x        = position.x
        val p0y        = position.y
        val (pfx, pfy) = TurtleHelper.posAfterForward(p0x, p0y, heading, n)
        turtlePath.lineTo(pfx, pfy)
        turtleLayer.removeChild(turtlePath)
        turtleImage.position.x = pfx
        turtleImage.position.y = pfy
        window.requestAnimationFrame(forwardEndFrame)
        turtleWorld.render()
      }

      window.requestAnimationFrame(forwardFrame)
    }

    def right(angle: Double): Unit = {
      def rightEndFrame(frameTime: Double): Unit = {
        turtleLayer.addChild(turtlePath)
        turtleWorld.render()
      }

      def rightFrame(frameTime: Double): Unit = {
        val angleRads = Utils.deg2radians(angle)
        turtleLayer.removeChild(turtlePath)
        turtleImage.rotation -= angleRads
        window.requestAnimationFrame(rightEndFrame)
        turtleWorld.render()
      }

      window.requestAnimationFrame(rightFrame)

    }

  }

  object TurlePlay {
    def main(args: Array[String]): Unit = {
      implicit val turtleWorld = new TurtleWorld()
      val turtle               = new Turtle(0, 0)
      import turtle._
      import RepeatCommands._
      repeat(6000) {
        repeat(4) {
          forward(100)
          right(90)
        }
        right(8)
      }
    }
  }

}
