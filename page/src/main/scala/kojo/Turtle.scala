package kojo

import org.scalajs.dom.window
import pixiscalajs.PIXI
import pixiscalajs.PIXI.Pixi

import scala.collection.mutable

class Turtle(x: Double, y: Double)(implicit turtleWorld: TurtleWorld) {
  val commandQ = mutable.Queue.empty[Command]

  val turtleLayer                 = new PIXI.Container()
  var turtleImage: PIXI.Container = _
  val turtlePath                  = new PIXI.Graphics()
  val tempGraphics                = new PIXI.Graphics()

  var penWidth         = 2d
  var penColor         = Color.blue
  var fillColor: Color = _
  var animationDelay   = 1000l

  Pixi.loader.add("turtle32", "assets/images/turtle32.png").load(init)

  def init(loader: PIXI.loaders.Loader, any: Any) {
    turtleLayer.name = "Turtle Layer"
    turtleWorld.addTurtleLayer(turtleLayer)
    turtleImage = loadTurtle(x, y, loader)
    turtleImage.name = "Turtle Icon"

    turtlePath.name = "Turtle Path"
    turtlePath.lineStyle(penWidth, penColor.toRGBDouble, penColor.alpha.get)
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

  def clear(): Unit = {
    // no-op for now
  }

  def forward(n: Double): Unit = {
    commandQ.enqueue(Forward(n))
  }

  def hop(n: Double): Unit = {
    commandQ.enqueue(Hop(n))
  }

  def left(angle: Double): Unit = {
    commandQ.enqueue(Turn(angle))
  }

  def right(angle: Double) = left(-angle)

  def setAnimationDelay(delay: Long): Unit = {
    commandQ.enqueue(SetAnimationDelay(delay))
  }

  def setPenThickness(t: Double): Unit = {
    commandQ.enqueue(SetPenThickness(t))
  }

  def setPenColor(color: Color): Unit = {
    commandQ.enqueue(SetPenColor(color))
  }

  def setFillColor(color: Color): Unit = {
    commandQ.enqueue(SetFillColor(color))
  }

  def queueHandler(): Unit = {
    if (commandQ.size > 0) {
      commandQ.dequeue() match {
        case Forward(n)  => realForward(n)
        case Hop(n)      => realHop(n)
        case Turn(angle) => realLeft(angle)
        case SetAnimationDelay(delay) =>
          animationDelay = delay; turtleWorld.scheduleLater(queueHandler)
        case SetPenThickness(t) => realSetPenThickness(t)
        case SetPenColor(c)     => realSetPenColor(c)
        case SetFillColor(c)    => realSetFillColor(c)
      }
    }
  }

  def realSetPenThickness(t: Double): Unit = {
    penWidth = t
    turtlePath.lineStyle(penWidth, penColor.toRGBDouble, penColor.alpha.get)
    turtleWorld.scheduleLater(queueHandler)
  }

  def realSetPenColor(color: Color): Unit = {
    penColor = color
    turtlePath.lineStyle(penWidth, penColor.toRGBDouble, penColor.alpha.get)
    turtleWorld.scheduleLater(queueHandler)
  }

  def realSetFillColor(color: Color): Unit = {
    // start new path
    turtlePath.lineStyle(penWidth, penColor.toRGBDouble, penColor.alpha.get)
    // set new fill
    fillColor = color
    turtlePath.beginFill(fillColor.toRGBDouble, fillColor.alpha.get)
    turtleWorld.scheduleLater(queueHandler)
  }

  def realHop(n: Double): Unit = {
    turtleLayer.addChild(tempGraphics)
    var len        = 0
    val p0x        = position.x
    val p0y        = position.y
    val (pfx, pfy) = TurtleHelper.posAfterForward(p0x, p0y, headingRadians, n)
    val aDelay     = TurtleHelper.delayFor(n, animationDelay)
    //      println(s"($p0x, $p0y) -> ($pfx, $pfy) [$aDelay]")
    val startTime = window.performance.now()

    def forwardEndFrame(frameTime: Double): Unit = {
      turtleWorld.render()
      turtleWorld.scheduleLater(queueHandler)
    }

    def forwardFrame(frameTime: Double): Unit = {
      val elapsedTime = frameTime - startTime
      val frac        = elapsedTime / aDelay
      //        println(s"Fraction: $frac")

      if (frac > 1) {
        tempGraphics.clear()
        turtleLayer.removeChild(tempGraphics)
        turtlePath.moveTo(pfx, pfy)
        turtlePath.clearDirty += 1
        turtleImage.position.x = pfx
        turtleImage.position.y = pfy
        window.requestAnimationFrame(forwardEndFrame)
      } else {
        val currX = p0x * (1 - frac) + pfx * frac
        val currY = p0y * (1 - frac) + pfy * frac

        tempGraphics.clear()
        tempGraphics.lineStyle(penWidth, Color.green.toRGBDouble, 1)
        tempGraphics.moveTo(p0x, p0y)
        tempGraphics.lineTo(currX, currY)
        //          tempGraphics.clearDirty += 1
        turtleImage.position.x = currX
        turtleImage.position.y = currY
        window.requestAnimationFrame(forwardFrame)
      }
      turtleWorld.render()
    }

    window.requestAnimationFrame(forwardFrame)
  }

  def realForward(n: Double) {

    turtleLayer.addChild(tempGraphics)
    var len        = 0
    val p0x        = position.x
    val p0y        = position.y
    val (pfx, pfy) = TurtleHelper.posAfterForward(p0x, p0y, headingRadians, n)
    val aDelay     = TurtleHelper.delayFor(n, animationDelay)
    //      println(s"($p0x, $p0y) -> ($pfx, $pfy) [$aDelay]")
    val startTime = window.performance.now()

    def forwardEndFrame(frameTime: Double): Unit = {
      turtleWorld.render()
      turtleWorld.scheduleLater(queueHandler)
    }

    def forwardFrame(frameTime: Double): Unit = {
      val elapsedTime = frameTime - startTime
      val frac        = elapsedTime / aDelay
      //        println(s"Fraction: $frac")

      if (frac > 1) {
        tempGraphics.clear()
        turtleLayer.removeChild(tempGraphics)
        turtlePath.lineTo(pfx, pfy)
        turtlePath.clearDirty += 1
        turtleImage.position.x = pfx
        turtleImage.position.y = pfy
        window.requestAnimationFrame(forwardEndFrame)
      } else {
        val currX = p0x * (1 - frac) + pfx * frac
        val currY = p0y * (1 - frac) + pfy * frac

        tempGraphics.clear()
        tempGraphics.lineStyle(penWidth, Color.green.toRGBDouble, 1)
        tempGraphics.moveTo(p0x, p0y)
        tempGraphics.lineTo(currX, currY)
        //          tempGraphics.clearDirty += 1
        turtleImage.position.x = currX
        turtleImage.position.y = currY
        window.requestAnimationFrame(forwardFrame)
      }
      turtleWorld.render()
    }

    window.requestAnimationFrame(forwardFrame)
  }

  def realLeft(angle: Double): Unit = {

    def leftFrame(): Unit = {
      val angleRads = Utils.deg2radians(angle)
      turtleImage.rotation += angleRads
      turtleWorld.render()
      turtleWorld.scheduleLater(queueHandler)
    }

    leftFrame()
  }

}
