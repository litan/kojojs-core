package kojo

import kojo.doodle.Color
import pixiscalajs.PIXI

object GPics {
  def apply(pics: List[Picture])(implicit turtleWorld: TurtleWorld) = new GPics(pics)
  def apply(pics: Picture*)(implicit turtleWorld: TurtleWorld) = new GPics(pics)
}

class GPics(pics: Seq[Picture])(implicit val turtleWorld: TurtleWorld) extends Picture {
  val tnode = new PIXI.Container()

  pics.foreach { p =>
    tnode.addChild(p.tnode)
  }

  def made: Boolean = {
    val notMade = pics.find { p =>
      !p.made
    }
    notMade match {
      case Some(_) => false
      case None    => true
    }
  }

  def realDraw(): Unit = {
    turtleWorld.addTurtleLayer(tnode)
    turtleWorld.render()
    pics.foreach { p =>
      p.updateGeomTransform()
    }
  }

  def initGeom() = {
    var pg = pics(0).picGeom
    pics.tail.foreach { pic =>
      pg = pg union pic.picGeom
    }
    pg
  }

  def setFillColor(c: Color): Unit = {
    pics.foreach { p =>
      p.setFillColor(c)
    }
  }

  def setPenColor(c: Color): Unit = {
    pics.foreach { p =>
      p.setPenColor(c)
    }
  }

  def setPenThickness(t: Double): Unit = {
    pics.foreach { p =>
      p.setPenThickness(t)
    }
  }

  def erase(): Unit = {
    pics.foreach { p =>
      p.erase()
    }
  }
}
