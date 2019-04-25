package kojo.syntax

import scala.scalajs.js.Date

import org.scalajs.dom.window

import kojo.FillColor
import kojo.GlobalTurtleForPicture
import kojo.ImagePic
import kojo.KeyCodes
import kojo.Mp3Player
import kojo.Offset
import kojo.PenColor
import kojo.PenThickness
import kojo.Picture
import kojo.Rotate
import kojo.Scale
import kojo.ScaleXY
import kojo.SwedishTurtle
import kojo.TextPic
import kojo.Translate
import kojo.Turtle
import kojo.TurtlePicture
import kojo.TurtleWorld
import kojo.Vector2D
import kojo.doodle.Color
import pixiscalajs.PIXI.Rectangle

class Builtins(implicit turtleWorld: TurtleWorld) {
  var turtle0 = new Turtle(0, 0)
  val turtle = new GlobalTurtleForPicture
  turtle.globalTurtle = turtle0
  val svTurtle = new SwedishTurtle(turtle0)
  val Color = kojo.doodle.Color
  val cm = kojo.doodle.Color
  val noColor = Color(0, 0, 0, 0)

  val Random = new java.util.Random

  def random(upperBound: Int) = Random.nextInt(upperBound)

  def randomDouble(upperBound: Int) = Random.nextDouble * upperBound

  def randomNormalDouble = Random.nextGaussian

  def randomBoolean = Random.nextBoolean

  def randomFrom[T](seq: Seq[T]) = seq(random(seq.length))

  def randomColor = Color(random(256), random(256), random(256))

  def randomTransparentColor = Color(random(256), random(256), random(256), 100 + random(156))

  def readln(prompt: String): String = {
    val ret = window.prompt(prompt, "Type here")
    if (ret == null)
      throw new RuntimeException("Read failed.")
    else
      ret
  }

  def readInt(prompt: String): Int = readln(prompt).toInt

  def readDouble(prompt: String): Double = readln(prompt).toDouble

  val setBackground = turtleWorld.setBackground _
  val animate = turtleWorld.animate _
  val timer = turtleWorld.timer _
  val drawStage = turtleWorld.drawStage _

  val bounceVecOffStage = turtleWorld.bounceVecOffStage _
  def bouncePicVectorOffPic(pic: Picture, v: Vector2D, obstacle: Picture): Vector2D =
    turtleWorld.bouncePicVectorOffPic(pic, v, obstacle, Random)
  def bouncePicVectorOffStage(p: Picture, v: Vector2D): Vector2D = bouncePicVectorOffPic(p, v, turtleWorld.stageBorder)

  val isKeyPressed = turtleWorld.isKeyPressed _
  lazy val stageBorder = turtleWorld.stageBorder
  lazy val stageTop = turtleWorld.stageTop
  lazy val stageBot = turtleWorld.stageBot
  lazy val stageLeft = turtleWorld.stageLeft
  lazy val stageRight = turtleWorld.stageRight
  lazy val stageArea = turtleWorld.stageArea
  val Kc = new KeyCodes
  val canvasBounds = {
    val pos = turtleWorld.stage.position
    new Rectangle(-pos.x, -pos.y, turtleWorld.width, turtleWorld.height)
  }
  def PictureT(fn: Turtle => Unit)(implicit turtleWorld: TurtleWorld): TurtlePicture = {
    TurtlePicture(fn)
  }
  def Picture(fn: => Unit)(implicit turtleWorld: TurtleWorld): TurtlePicture = {
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
    // Does not work yet! Needs further research/experimentation
    turtleWorld.runLater(1000) { () =>
      turtleWorld.canvas_holder.click()
    }
  }
  def switchToDefault2Perspective() {}
  def toggleFullScreenCanvas() {}

  val stopAnimation = turtleWorld.stopAnimation _
  def draw(pictures: Picture*) = pictures.foreach { _ draw () }
  def draw(pictures: IndexedSeq[Picture]) = pictures.foreach { _ draw () }
  def draw(pictures: List[Picture]) = pictures.foreach { _ draw () }
  def drawAndHide(pictures: Picture*) = pictures.foreach { p => p.draw(); p.invisible() }

  val GPics = kojo.GPics
  def rot(angle: Double) = Rotate(angle)
  def trans(x: Double, y: Double) = Translate(x, y)
  def offset(x: Double, y: Double) = Offset(x, y)
  def scale(f: Double) = Scale(f)
  def scale(fx: Double, fy: Double) = ScaleXY(fx, fy)
  def penColor(c: Color) = PenColor(c)
  def penWidth(t: Double) = PenThickness(t)
  def fillColor(c: Color) = FillColor(c)

  object Picture {
    def rect(h: Double, w: Double) = Picture {
      import kojo.RepeatCommands._
      import turtle._
      repeat(2) {
        forward(h)
        right()
        forward(w)
        right()
      }
    }

    def rectangle(w: Double, h: Double) = rect(h, w)

    def circle(r: Double) = Picture {
      import turtle._
      right()
      hop(r)
      left()
      turtle.circle(r)
    }

    def vline(n: Double) = Picture {
      import turtle._
      forward(n)
    }

    def textu(text: Any, fontSize: Int, color: Color = Color.black)(implicit turtleWorld: TurtleWorld): TextPic = {
      new TextPic(text, fontSize, color)
    }
    def text(s0: Any, fontSize: Int = 15) = textu(s0, fontSize)

    def image(url: String)(implicit turtleWorld: TurtleWorld): ImagePic = {
      new ImagePic(url)
    }

    def image(url: String, envelope: Picture)(implicit turtleWorld: TurtleWorld): ImagePic = {
      new ImagePic(url)
    }
  }
  val PicShape = Picture

  def url(s: String) = s
  type MMap[K, V] = collection.mutable.Map[K, V]
  type MSet[V] = collection.mutable.Set[V]
  type MSeq[V] = collection.mutable.Seq[V]

  val HashMap = collection.mutable.HashMap
  val HashSet = collection.mutable.HashSet
  val ArrayBuffer = collection.mutable.ArrayBuffer

  def showFps(color: Color = Color.black, fontSize: Int = 15)(implicit turtleWorld: TurtleWorld) {
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

  def epochTimeMillis = new Date().getTime()

  def schedule(seconds: Double)(code: => Unit) = turtleWorld.runLater(seconds * 1000) { () =>
    code
  }
}
