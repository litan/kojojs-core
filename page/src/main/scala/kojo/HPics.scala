package kojo

import kojo.doodle.Color
import pixiscalajs.PIXI

import scala.concurrent.Future

object HPics {
  def apply(pics: List[Picture])(implicit kojoWorld: KojoWorld) = new HPics(pics)
  def apply(pics: Picture*)(implicit kojoWorld: KojoWorld) = new HPics(pics)
}

class HPics(pics: Seq[Picture])(implicit val kojoWorld: KojoWorld) extends Picture {
  val tnode = new PIXI.Container()

  pics.foreach { p =>
    tnode.addChild(p.tnode)
  }

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  ready.foreach { _ =>
    var ox = 0.0
    pics.foreach { pic =>
      pic.offset(ox, 0)
      val nbounds = pic.bounds
      ox = nbounds.x + nbounds.width
    }
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

  def ready: Future[Unit] = {
    val futures = pics map (_.ready)
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
    futures.reduce { (f1, f2) => for (_ <- f1; _ <- f2) yield () }
  }

  def realDraw(): Unit = {
    kojoWorld.addLayer(tnode)
    //    pics.foreach { p =>
    //      p.updateGeomTransform()
    //    }
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
    kojoWorld.removeLayer(tnode)
    //    pics.foreach { p =>
    //      p.erase()
    //    }
  }
}
