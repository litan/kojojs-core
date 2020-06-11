package kojo

import kojo.doodle.Color
import pixiscalajs.PIXI

import scala.concurrent.Future

object HPicsHPicsCentered {
  def apply(pics: List[Picture])(implicit kojoWorld: KojoWorld) = new HPicsCentered(pics)
  def apply(pics: Picture*)(implicit kojoWorld: KojoWorld) = new HPicsCentered(pics)
}

class HPicsCentered(pics: Seq[Picture])(implicit val kojoWorld: KojoWorld) extends Picture {
  val tnode = new PIXI.Container()

  pics.foreach { p =>
    tnode.addChild(p.tnode)
  }

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  ready.foreach { _ =>
    var prevPic: Option[Picture] = None
    pics.foreach { pic =>
      prevPic match {
        case Some(ppic) =>
          val pbounds = ppic.bounds
          val tx = pbounds.x + pbounds.width
          pic.offset(tx, 0)
          val bounds = pic.bounds
          val ty = pbounds.y - bounds.y + (pbounds.height - bounds.height) / 2
          val tx2 = pbounds.x + pbounds.width - bounds.x
          pic.offset(tx2, ty)
        case None =>
      }
      prevPic = Some(pic)
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
