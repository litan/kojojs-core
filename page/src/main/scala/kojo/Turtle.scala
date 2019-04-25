package kojo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import org.scalajs.dom.window

import kojo.doodle.Color
import pixiscalajs.PIXI
import pixiscalajs.PIXI
import pixiscalajs.PIXI.Pixi
import pixiscalajs.PIXI.Point

object TurtleImageHelper {
  private val loader = Pixi.loader

  case class QEntry(name: String, url: String, doneFn: (PIXI.loaders.Loader, Any) => Unit)

  val queue = mutable.Queue.empty[QEntry]

  def addAndLoad(name: String, url: String, doneFn: (PIXI.loaders.Loader, Any) => Unit): Unit = {

    def checkQ(): Unit = {
      if (queue.nonEmpty) {
        val qe = queue.dequeue()
        addAndLoad(qe.name, qe.url, qe.doneFn)
      }
    }

    if (!loader.loading) {
      val resVal = loader.resources(name)
      if (resVal == ()) {
        loader.add(name, url)
        loader.load { (loader, any) =>
          doneFn(loader, any)
          checkQ()
        }
      }
      else {
        doneFn(loader, resVal)
        checkQ()
      }
    }
    else {
      queue.enqueue(QEntry(name, url, doneFn))
    }
  }
}

class Turtle(x: Double, y: Double, forPic: Boolean = false)(implicit turtleWorld: TurtleWorld)
  extends TurtleAPI
  with RichTurtleCommands {
  private[kojo] val turtleLayer = new PIXI.Container()
  private var turtleImage: PIXI.Container = _
  private[kojo] val turtlePath = new PIXI.Graphics()
  private[kojo] val turtlePathPoints = ArrayBuffer[(Double, Double)]()
  private def turtlePathMoveTo(x: Double, y: Double): Unit = {
    turtlePath.moveTo(x, y)
    turtlePathPoints += ((x, y))
  }

  private def turtlePathLineTo(x: Double, y: Double): Unit = {
    turtlePath.lineTo(x, y)
    turtlePathPoints += ((x, y))
  }

  private val tempForwardPath = new PIXI.Graphics()

  private var penWidth = 2d
  private var penColor = Color.red
  private var fillColor: Color = _
  private var penFontSize = 15
  private var penIsUp = false
  private var animationDelay = 1000l
  private val savedPosHe = new mutable.Stack[(PIXI.Point, Double)]

  var commandQs = mutable.Queue.empty[Command] :: Nil

  TurtleImageHelper.addAndLoad("turtle32", "assets/images/turtle32.png", init)

  private def init(loader: PIXI.loaders.Loader, any: Any) {
    turtleLayer.name = "Turtle Layer"
    if (!forPic) {
      turtleWorld.addTurtleLayer(turtleLayer)
    }
    turtleImage = loadTurtle(x, y, loader)
    turtleImage.name = "Turtle Icon"

    turtlePath.name = "Turtle Path"
    turtleLayer.addChild(turtlePath)
    if (!forPic) {
      turtleLayer.addChild(turtleImage)
    }
    initTurtleLayer()
    turtleWorld.runLater(0)(queueHandler)
  }

  private def initTurtleLayer(): Unit = {
    turtlePath.lineStyle(penWidth, penColor.toRGBDouble, penColor.alpha.get)
    turtlePathMoveTo(x, y)
    turtleImage.position.set(x, y)
    turtleImage.rotation = Utils.deg2radians(90)
  }

  private def position = turtleImage.position

  private def headingRadians = turtleImage.rotation

  private def heading = Utils.rad2degrees(headingRadians)

  private def loadTurtle(x: Double, y: Double, loader: PIXI.loaders.Loader): PIXI.Container = {
    val turtle = {
      val rasterTurtle = new PIXI.Sprite(loader.resources("turtle32").texture)
      rasterTurtle.position.set(-16, -16)
      rasterTurtle.alpha = 0.7
      rasterTurtle
    }
    val turtleHolder = new PIXI.Container()
    turtleHolder.addChild(turtle)
    turtleHolder
  }

  def forward(n: Double): Unit = {
    commandQ.enqueue(Forward(n))
  }

  def hop(n: Double): Unit = {
    commandQ.enqueue(Hop(n))
  }

  def turn(angle: Double): Unit = {
    commandQ.enqueue(Turn(angle))
  }

  def setAnimationDelay(delay: Long): Unit = {
    commandQ.enqueue(SetAnimationDelay(delay))
  }

  def setPenThickness(t: Double): Unit = {
    commandQ.enqueue(SetPenThickness(t))
  }

  def setPenColor(color: Color): Unit = {
    commandQ.enqueue(SetPenColor(color))
  }

  def setPenFontSize(n: Int): Unit = {
    commandQ.enqueue(SetPenFontSize(n))
  }

  def setFillColor(color: Color): Unit = {
    commandQ.enqueue(SetFillColor(color))
  }

  def setPosition(x: Double, y: Double): Unit = {
    commandQ.enqueue(SetPosition(x, y))
  }

  def setHeading(theta: Double): Unit = {
    commandQ.enqueue(SetHeading(Utils.deg2radians(theta)))
  }

  def moveTo(x: Double, y: Double): Unit = {
    commandQ.enqueue(MoveTo(x, y))
  }

  def arc2(r: Double, a: Double): Unit = {
    commandQ.enqueue(Arc2(r, a))
  }

  def write(text: String): Unit = {
    commandQ.enqueue(Write(text))
  }

  def savePosHe(): Unit = {
    commandQ.enqueue(SavePosHe)
  }

  def restorePosHe(): Unit = {
    commandQ.enqueue(RestorePosHe)
  }

  def clear(): Unit = {
    commandQ.enqueue(Clear)
  }

  def pause(seconds: Double): Unit = {
    commandQ.enqueue(Pause(seconds))
  }

  def penUp(): Unit = {
    commandQ.enqueue(PenUp)
  }

  def penDown(): Unit = {
    commandQ.enqueue(PenDown)
  }

  def invisible(): Unit = {
    commandQ.enqueue(Invisible)
  }

  def visible(): Unit = {
    commandQ.enqueue(Visible)
  }

  private[kojo] def sync(fn: () => Unit): Unit = {
    commandQ.enqueue(Sync(fn))
  }

  private def queueHandler(): Unit = {
    if (commandQ.size > 0) {
      commandQ.dequeue() match {
        case Forward(n)  => realForward(n, penIsUp)
        case Hop(n)      => realForward(n, true)
        case Turn(angle) => realLeft(angle)
        case SetAnimationDelay(delay) =>
          animationDelay = delay; turtleWorld.scheduleLater(queueHandler)
        case SetPenThickness(t) => realSetPenThickness(t)
        case SetPenColor(c)     => realSetPenColor(c)
        case SetFillColor(c)    => realSetFillColor(c)
        case SetPosition(x, y)  => realSetPosition(x, y)
        case SetHeading(theta)  => realSetHeading(theta)
        case MoveTo(x, y)       => realMoveTo(x, y)
        case Arc2(r, a)         => realArc2(r, a)
        case PopQ               => realPopQ()
        case Write(text)        => realWriteText(text)
        case SetPenFontSize(n)  => realSetPenFontSize(n)
        case SavePosHe          => realSavePosHe()
        case RestorePosHe       => realRestorePosHe()
        case Clear              => realClear()
        case Pause(seconds)     => realPause(seconds)
        case PenUp              => realPenUpDown(true)
        case PenDown            => realPenUpDown(false)
        case Sync(fn)           => realSync(fn)
        case Invisible          => realInvisible()
        case Visible            => realVisible()
      }
    }
  }

  private def realSetPenThickness(t: Double): Unit = {
    penWidth = t
    turtlePath.lineStyle(penWidth, penColor.toRGBDouble, penColor.alpha.get)
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realSetPenColor(color: Color): Unit = {
    penColor = color
    turtlePath.lineStyle(penWidth, penColor.toRGBDouble, penColor.alpha.get)
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realSetPenFontSize(n: Int): Unit = {
    penFontSize = n
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realSetFillColor(color: Color): Unit = {
    // start new path
    turtlePath.lineStyle(penWidth, penColor.toRGBDouble, penColor.alpha.get)
    // set new fill
    fillColor = color
    turtlePath.beginFill(fillColor.toRGBDouble, fillColor.alpha.get)
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realSetPosition(x: Double, y: Double): Unit = {
    turtleImage.position.x = x
    turtleImage.position.y = y
    turtlePathMoveTo(x, y)
    turtleWorld.render()
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realSetHeading(theta: Double): Unit = {
    turtleImage.rotation = theta
    turtleWorld.render()
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realForwardNoAnim(n: Double, hop: Boolean): Unit = {
    val p0x = position.x
    val p0y = position.y
    val (pfx, pfy) = TurtleHelper.posAfterForward(p0x, p0y, headingRadians, n)
    if (hop) {
      turtlePathMoveTo(pfx, pfy)
    }
    else {
      turtlePathLineTo(pfx, pfy)
    }
    turtlePath.clearDirty += 1
    turtleImage.position.x = pfx
    turtleImage.position.y = pfy
    turtleWorld.render()
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realForward(n: Double, hop: Boolean): Unit = {
    if (animationDelay == 0) {
      realForwardNoAnim(n, hop)
      return
    }

    turtleLayer.addChild(tempForwardPath)
    var len = 0
    val p0x = position.x
    val p0y = position.y
    val (pfx, pfy) = TurtleHelper.posAfterForward(p0x, p0y, headingRadians, n)
    val aDelay = TurtleHelper.delayFor(n, animationDelay)
    //      println(s"($p0x, $p0y) -> ($pfx, $pfy) [$aDelay]")
    val startTime = window.performance.now()

    def forwardFrame(frameTime: Double): Unit = {
      val elapsedTime = frameTime - startTime
      val frac = elapsedTime / aDelay
      //        println(s"Fraction: $frac")

      if (frac > 1) {
        if (hop) {
          turtlePathMoveTo(pfx, pfy)
        }
        else {
          tempForwardPath.clear()
          turtleLayer.removeChild(tempForwardPath)
          turtlePathLineTo(pfx, pfy)
        }
        turtlePath.clearDirty += 1
        turtleImage.position.x = pfx
        turtleImage.position.y = pfy
        turtleWorld.render()
        turtleWorld.scheduleLater(queueHandler)
      }
      else {
        val currX = p0x * (1 - frac) + pfx * frac
        val currY = p0y * (1 - frac) + pfy * frac
        if (!hop) {
          tempForwardPath.clear()
          tempForwardPath.lineStyle(penWidth, Color.orange.toRGBDouble, 1)
          tempForwardPath.moveTo(p0x, p0y)
          tempForwardPath.lineTo(currX, currY)
          //          tempGraphics.clearDirty += 1
        }
        turtleImage.position.x = currX
        turtleImage.position.y = currY
        window.requestAnimationFrame(forwardFrame)
      }
      turtleWorld.render()
    }

    window.requestAnimationFrame(forwardFrame)
  }

  private def realLeft(angle: Double): Unit = {

    def leftFrame(): Unit = {
      val angleRads = Utils.deg2radians(angle)
      turtleImage.rotation += angleRads
      turtleWorld.render()
      turtleWorld.scheduleLater(queueHandler)
    }

    leftFrame()
  }

  private def realArc2(r: Double, a: Double) {
    pushQ()
    if (a == 0) {
      return
    }

    def x(t: Double) = r * math.cos(t.toRadians)

    def y(t: Double) = r * math.sin(t.toRadians)

    def makeArc() {
      val head = heading
      if (r != 0) {
        val pos = position
        var currAngle = 0.0
        val trans = new PIXI.Matrix
        trans.translate(-r, 0)
        trans.rotate((head - 90).toRadians)
        trans.translate(pos.x, pos.y)
        val step = if (a > 0) 3 else -3
        val pt = new Point(0, 0)
        val aabs = a.abs
        val aabsFloor = aabs.floor
        while (currAngle.abs < aabsFloor) {
          currAngle += step
          // account for step size > 1
          while (currAngle.abs > aabsFloor) currAngle -= step / step.abs
          pt.set(x(currAngle), y(currAngle))
          trans(pt, pt)
          moveTo(pt.x, pt.y)
        }
        if (a.floor != a) {
          currAngle += (aabs - aabs.floor) * step
          pt.set(x(currAngle), y(currAngle))
          trans(pt, pt)
          moveTo(pt.x, pt.y)
        }
      }
      if (a > 0) {
        setHeading(head + a)
      }
      else {
        setHeading(head + 180 + a)
      }
    }

    makeArc()
    popQ()
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realMoveTo(x: Double, y: Double) {
    pushQ()
    val newTheta = towardsHelper(x, y)
    setHeading(newTheta.toDegrees)
    val d = distanceTo(x, y)
    forward(d)
    popQ()
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realWriteText(text: String): Unit = {
    if (!penIsUp) {
      val pixiText = new PIXI.Text(text)
      pixiText.setTransform(0, 0, 1, -1, 0, 0, 0, 0, 0)
      pixiText.position = position
      pixiText.rotation = (heading - 90).toRadians
      pixiText.style.fontSize = penFontSize
      turtleLayer.addChild(pixiText)
      turtleWorld.render()
    }
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realSavePosHe(): Unit = {
    val pos = position
    savedPosHe.push((PIXI.Point(pos.x, pos.y), heading))
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realRestorePosHe(): Unit = {
    pushQ()
    val (newPosition, newHeading) = savedPosHe.pop()
    setPosition(newPosition.x, newPosition.y)
    setHeading(newHeading)
    popQ()
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realClear(): Unit = {
    turtlePath.clear()
    turtlePathPoints.clear()
    initTurtleLayer()
    turtleWorld.render()
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realPause(seconds: Double): Unit = {
    val t0 = window.performance.now()
    def pump(frameTime: Double): Unit = {
      if (frameTime - t0 > seconds * 1000) {
        turtleWorld.scheduleLater(queueHandler)
      }
      else {
        window.requestAnimationFrame(pump)
      }
    }
    pump(t0)
  }

  private def realPenUpDown(up: Boolean): Unit = {
    if (up) {
      penIsUp = true
    }
    else {
      penIsUp = false
    }
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realInvisible(): Unit = {
    turtleImage.visible = false
    turtleWorld.render()
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realVisible(): Unit = {
    turtleImage.visible = true
    turtleWorld.render()
    turtleWorld.scheduleLater(queueHandler)
  }

  private def distanceTo(x: Double, y: Double): Double = {
    TurtleHelper.distance(position.x, position.y, x, y)
  }

  private def towardsHelper(x: Double, y: Double): Double = {
    TurtleHelper.thetaTowards(position.x, position.y, x, y, headingRadians)
  }

  private def commandQ = commandQs.head

  private def pushQ(): Unit = {
    commandQs = mutable.Queue.empty[Command] :: commandQs
  }

  private def popQ(): Unit = {
    commandQ.enqueue(PopQ)
  }

  private def realPopQ(): Unit = {
    assert(commandQ.size == 0)
    commandQs = commandQs.tail
    turtleWorld.scheduleLater(queueHandler)
  }

  private def realSync(fn: () => Unit) = {
    fn()
    turtleWorld.scheduleLater(queueHandler)
  }
}
