package kojo.syntax

import kojo._
import org.scalajs.dom.window
import pixiscalajs.PIXI.{Graphics, Rectangle}

import scala.scalajs.js.Date

class Builtins(implicit kojoWorld: KojoWorld) {
  var turtle0 = new Turtle(0, 0)
  val turtle = new GlobalTurtleForPicture
  turtle.globalTurtle = turtle0
  val svTurtle = new SwedishTurtle(turtle0)

  val Color = kojo.doodle.Color
  type Color = kojo.doodle.Color

  val ColorMaker = kojo.doodle.Color
  val cm = kojo.doodle.Color
  val noColor = Color(0, 0, 0, 0)
  type Point = pixiscalajs.PIXI.Point
  val Point = pixiscalajs.PIXI.Point

  val Random = new java.util.Random

  def setRandomSeed(seed: Long): Unit = { Random.setSeed(seed) }
  def random(upperBound: Int) = Random.nextInt(upperBound)
  def random(lowerBound: Int, upperBound: Int): Int = {
    if (lowerBound >= upperBound) lowerBound else
      lowerBound + random(upperBound - lowerBound)
  }
  def randomDouble(upperBound: Double): Double = {
    if ((upperBound == 0) || (upperBound != upperBound)) 0 else
      Random.nextDouble * upperBound
  }
  def randomDouble(lowerBound: Double, upperBound: Double): Double = {
    if (lowerBound >= upperBound) lowerBound else
      lowerBound + randomDouble(upperBound - lowerBound)
  }
  def randomNormalDouble = Random.nextGaussian()
  def randomBoolean = Random.nextBoolean
  def randomInt = Random.nextInt
  def randomLong = Random.nextLong
  def randomFrom[T](seq: collection.Seq[T]) = seq(random(seq.length))
  def randomFrom[T](seq: collection.Seq[T], weights: collection.mutable.Seq[Int]): T = randomFrom(seq, weights map (_.toDouble))
  def randomFrom[T](seq: collection.Seq[T], weights: collection.immutable.Seq[Int]): T = randomFrom(seq, weights map (_.toDouble))
  def randomFrom[T](seq: collection.Seq[T], weights: Seq[Double]): T = {
    val sum = weights.sum
    val probabilities = if (Utils.doublesEqual(sum, 1.0, 1e-3)) {
      weights
    }
    else {
      weights.map { w => w / sum }
    }

    // sourced from:
    // https://stackoverflow.com/questions/24869304/scala-how-can-i-generate-numbers-according-to-an-expected-distribution
    val p = Random.nextDouble
    val it = seq.zip(probabilities).iterator
    var accum = 0.0
    while (it.hasNext) {
      val (item, itemProb) = it.next
      accum += itemProb
      if (accum >= p)
        return item // return so that we don't have to search through the whole distribution
    }
    sys.error("this should never happen") // needed so it will compile
  }

  def randomColor = Color(random(256), random(256), random(256))
  def randomTransparentColor = Color(random(256), random(256), random(256), 100 + random(156))
  def initRandomGenerator(): Unit = {
    initRandomGenerator(randomLong)
  }

  def initRandomGenerator(seed: Long): Unit = {
    println(s"Random seed set to: ${seed}L")
    setRandomSeed(seed)
  }

  def rangeTo(start: Int, end: Int, step: Int = 1) = start to end by step
  def rangeTill(start: Int, end: Int, step: Int = 1) = start until end by step

  def rangeTo(start: Double, end: Double, step: Double) = Range.BigDecimal.inclusive(start, end, step)
  def rangeTill(start: Double, end: Double, step: Double) = Range.BigDecimal(start, end, step)

  import scala.language.implicitConversions
  implicit def bd2double(bd: BigDecimal) = bd.doubleValue

  def readln(prompt: String): String = {
    val ret = window.prompt(prompt, "Type here")
    if (ret == null)
      throw new RuntimeException("Read failed.")
    else
      ret
  }

  def readInt(prompt: String): Int = readln(prompt).toInt

  def readDouble(prompt: String): Double = readln(prompt).toDouble

  def setBackground(color: Color): Unit = {
    kojoWorld.setBackground(color)
  }
  // background gradients currently not supported. Maybe with pixi v5
  def setBackgroundH(c1: Color, c2: Color) = setBackground(c1)
  def setBackgroundV(c1: Color, c2: Color) = setBackground(c1)

  def disablePanAndZoom(): Unit = {}

  def animate(fn: => Unit): Unit = {
    kojoWorld.animate(fn)
  }

  def setup(fn: => Unit) = {
    kojoWorld.setup(fn)
  }

  def drawLoop(fn: => Unit) = animate(fn)

  def timer(ms: Long)(fn: => Unit): Unit = {
    kojoWorld.timer(ms)(fn)
  }

  def drawStage(fillc: Color)(implicit kojoWorld: KojoWorld): Unit = {
    kojoWorld.drawStage(fillc)
  }

  def size(width: Int, height: Int): Unit = {
    kojoWorld.size(width, height)
  }

  def zoomXY(xfactor: Double, yfactor: Double, cx: Double, cy: Double): Unit = {
    kojoWorld.zoomXY(xfactor, yfactor, cx, cy)
  }

  def zoom(factor: Double, cx: Double, cy: Double): Unit = {
    zoomXY(factor, factor, cx, cy)
  }

  def cwidth = kojoWorld.width
  def cheight = kojoWorld.height

  def mouseX = kojoWorld.mouseXY.x
  def mouseY = kojoWorld.mouseXY.y

  def originTopLeft(): Unit = {
    zoomXY(1, -1, cwidth / 2, cheight / 2)
  }

  def originBottomLeft(): Unit = {
    zoomXY(1, 1, cwidth / 2, cheight / 2)
  }

  def erasePictures() = kojoWorld.erasePictures()

  def bounceVecOffStage(v: Vector2D, p: Picture): Vector2D =
    kojoWorld.bounceVecOffStage(v, p)
  def bouncePicVectorOffPic(pic: Picture, v: Vector2D, obstacle: Picture): Vector2D =
    kojoWorld.bouncePicVectorOffPic(pic, v, obstacle, Random)
  def bouncePicOffPic(pic: Picture, v: Vector2D, obstacle: Picture): Vector2D = bouncePicVectorOffPic(pic, v, obstacle)
  def bouncePicVectorOffStage(p: Picture, v: Vector2D): Vector2D = bouncePicVectorOffPic(p, v, kojoWorld.stageBorder)
  def bouncePicOffStage(p: Picture, v: Vector2D): Vector2D = bouncePicVectorOffPic(p, v, kojoWorld.stageBorder)

  def isKeyPressed(keyCode: Int): Boolean = kojoWorld.isKeyPressed(keyCode)

  lazy val stageBorder = kojoWorld.stageBorder
  lazy val stageTop = kojoWorld.stageTop
  lazy val stageBot = kojoWorld.stageBot
  lazy val stageLeft = kojoWorld.stageLeft
  lazy val stageRight = kojoWorld.stageRight
  lazy val stageArea = kojoWorld.stageArea
  val Kc = new KeyCodes

  val kmath = Kmath
  val mathx = kmath

  def canvasBounds = {
    val pos = kojoWorld.stagePosition
    new Rectangle(-pos.x, -pos.y, kojoWorld.width, kojoWorld.height)
  }
  def PictureT(fn: Turtle => Unit)(implicit kojoWorld: KojoWorld): TurtlePicture = {
    TurtlePicture(fn)
  }
  def Picture(fn: => Unit)(implicit kojoWorld: KojoWorld): TurtlePicture = {
    val tp = new TurtlePicture
    turtle.globalTurtle = tp.turtle
    tp.make { t =>
      fn
    }
    turtle.globalTurtle = turtle0
    tp
  }

  def textExtent(str: String, fontSize: Int) = {
    val pic = new TextPic(str, fontSize, Color.black)
    pic.tnode.getBounds()
  }

  def drawCenteredMessage(message: String, color: Color = Color.black, fontSize: Int = 15) {
    val cb = canvasBounds
    val te = textExtent(message, fontSize)
    val pic = Picture.textu(message, fontSize, color)
    pic.translate(cb.x + (cb.width - te.width) / 2, 0)
    draw(pic)
  }
  def showGameTime(limitSecs: Int, endMsg: String, color: Color = Color.black, fontSize: Int = 15): Unit = {
    val cb = canvasBounds
    @volatile var gameTime = 0
    val timeLabel = Picture.textu(gameTime, fontSize, color)
    draw(timeLabel)
    timeLabel.setPosition(cb.x + 10, cb.y + 50)

    timer(1000) {
      gameTime += 1
      timeLabel.update(gameTime)

      if (gameTime == limitSecs) {
        drawCenteredMessage(endMsg, color, fontSize * 2)
        stopAnimation()
      }
    }
  }
  def activateCanvas(): Unit = {
    kojoWorld.runLater(0) {
      window.focus()
    }
  }
  def switchToDefault2Perspective() {}
  def toggleFullScreenCanvas() {}

  val stopAnimation = kojoWorld.stopAnimation _
  def draw(pictures: Picture*) = pictures.foreach { _ draw () }
  def draw(pictures: IndexedSeq[Picture]) = pictures.foreach { _ draw () }
  def draw(pictures: List[Picture]) = pictures.foreach { _ draw () }
  def drawAndHide(pictures: Picture*) = pictures.foreach { p =>
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
    p.draw()
    p.ready.foreach { _ =>
      p.invisible()
    }
  }

  def drawCentered(pic: Picture): Unit = {
    def center(pic: Picture): Unit = {
      val cb = canvasBounds
      val pb = pic.bounds
      val xDelta = cb.x - pb.x + (cb.width - pb.width) / 2
      val yDelta = cb.y - pb.y + (cb.height - pb.height) / 2
      pic.offset(xDelta, yDelta)
    }
    pic.draw()
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
    pic.ready.foreach { _ =>
      center(pic)
    }
  }

  val GPics = kojo.GPics
  val HPics = kojo.HPics
  val VPics = kojo.VPics
  val GPics2 = kojo.GPicsCentered
  val HPics2 = kojo.HPicsCentered
  val VPics2 = kojo.VPicsCentered
  val picRow = HPics
  val picCol = VPics
  val picStack = GPics
  val picBatch = BatchPics
  val picRowCentered = HPics2
  val picColCentered = VPics2
  val picStackCentered = GPics2

  def rot(angle: Double) = Rotate(angle)
  def trans(x: Double, y: Double) = Translate(x, y)
  def offset(x: Double, y: Double) = Offset(x, y)
  def scale(f: Double) = Scale(f)
  def scale(fx: Double, fy: Double) = ScaleXY(fx, fy)
  def penColor(c: Color) = PenColor(c)
  def penWidth(t: Double) = PenThickness(t)
  def penThickness(t: Double) = PenThickness(t)
  def fillColor(c: Color) = FillColor(c)

  object Picture {
    def rect(h: Double, w: Double) = Picture.fromPath { path =>
      path.moveTo(0, 0)
      path.lineTo(0, h)
      path.lineTo(w, h)
      path.lineTo(w, 0)
      path.lineTo(0, 0)
    }

    def rectangle(w: Double, h: Double) = new RectanglePic(w, h)
    // def rectangle(w: Double, h: Double) = rect(h, w)

    def circle(r: Double) = new CirclePic(r)

    def line(width: Double, height: Double) = Picture.fromPath { path =>
      path.moveTo(0, 0)
      path.lineTo(width, height)
    }

    def hline(n: Double) = Picture {
      import turtle._
      right(90)
      forward(n)
    }

    def vline(n: Double) = Picture {
      import turtle._
      forward(n)
    }

    def textu(text: Any, fontSize: Int, color: Color = Color.black)(implicit kojoWorld: KojoWorld): TextPic = {
      new TextPic(text, fontSize, color)
    }
    def text(s0: Any, fontSize: Int = 15) = textu(s0, fontSize)

    def image(url: String)(implicit kojoWorld: KojoWorld): ImagePic = {
      new ImagePic(url, None)
    }

    def image(url: String, envelope: Picture)(implicit kojoWorld: KojoWorld): ImagePic = {
      new ImagePic(url, Some(envelope))
    }

    def fromPath(fn: Graphics => Unit) = { new PathPic(fn) }

    def fromVertexShape(fn: VertexShape => Unit) = fromPath { g =>
      fn(new VertexShape(g))
    }

    def hgap(gap: Double) = penColor(noColor) * penThickness(0.001) -> Picture.rectangle(gap, 0.001)
    def vgap(gap: Double) = penColor(noColor) * penThickness(0.001) -> Picture.rectangle(0.001, gap)
  }
  val PicShape = Picture

  def url(s: String) = s
  type MMap[K, V] = collection.mutable.Map[K, V]
  type MSet[V] = collection.mutable.Set[V]
  type MSeq[V] = collection.mutable.Seq[V]

  val HashMap = collection.mutable.HashMap
  val HashSet = collection.mutable.HashSet
  val ArrayBuffer = collection.mutable.ArrayBuffer

  def showFps(color: Color = Color.black, fontSize: Int = 15)(implicit kojoWorld: KojoWorld) {
    val cb = canvasBounds
    var frameCnt = 0
    val fpsLabel = Picture.textu("Fps: ", fontSize, color)
    fpsLabel.setPosition(cb.x + 10, cb.y + cb.height - 10)
    draw(fpsLabel)
    //    fpsLabel.forwardInputTo(TSCanvas.stageArea)

    timer(1000) {
      fpsLabel.update(s"Fps: $frameCnt")
      frameCnt = 0
    }
    animate {
      frameCnt += 1
    }
  }

  def newMp3Player = new Mp3Player
  val mp3player = newMp3Player
  def playMp3(mp3File: String) {
    mp3player.playMp3(mp3File)
  }

  def playMp3Sound(mp3File: String) {
    mp3player.playMp3Sound(mp3File)
  }

  def playMp3Loop(mp3File: String) {
    mp3player.playMp3Loop(mp3File)
  }
  def isMp3Playing = mp3player.isMp3Playing
  def stopMp3() = mp3player.stopMp3()
  def stopMp3Loop() = mp3player.stopMp3Loop()

  def epochTimeMillis = System.currentTimeMillis
  def epochTime = epochTimeMillis / 1000.0

  def schedule(seconds: Double)(code: => Unit) = kojoWorld.runLater(seconds * 1000) {
    code
  }

  def clearOutput(): Unit = {}
}
