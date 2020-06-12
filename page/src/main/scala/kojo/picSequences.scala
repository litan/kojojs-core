package kojo

import kojo.doodle.Color
import pixiscalajs.PIXI

import scala.concurrent.Future

abstract class BasePicSequece(pics: Seq[Picture]) extends Picture {
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
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

  lazy val ready: Future[Unit] = {
    val futures = pics map (_.ready)
    futures.reduce { (f1, f2) => for (_ <- f1; _ <- f2) yield () }
  }

  def realDraw(): Unit = {
    kojoWorld.addLayer(tnode)
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
  }
}

object GPics {
  def apply(pics: collection.immutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new GPics(pics)
  def apply(pics: collection.mutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new GPics(pics)
  def apply(pics: Picture*)(implicit kojoWorld: KojoWorld) = new GPics(pics)
}

class GPics(pics: Seq[Picture])(implicit val kojoWorld: KojoWorld) extends BasePicSequece(pics) {
}

object GPicsCentered {
  def apply(pics: collection.immutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new GPicsCentered(pics)
  def apply(pics: collection.mutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new GPicsCentered(pics)
  def apply(pics: Picture*)(implicit kojoWorld: KojoWorld) = new GPicsCentered(pics)
}

class GPicsCentered(pics: Seq[Picture])(implicit val kojoWorld: KojoWorld) extends BasePicSequece(pics) {
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  ready.foreach { _ =>
    var prevPic: Option[Picture] = None
    pics.foreach { pic =>
      prevPic match {
        case Some(ppic) =>
          val pbounds = ppic.bounds
          val bounds = pic.bounds
          val tx = pbounds.x - bounds.x + (pbounds.width - bounds.width) / 2
          val ty = pbounds.y - bounds.y + (pbounds.height - bounds.height) / 2
          pic.offset(tx, ty)
        case None =>
      }
      prevPic = Some(pic)
    }
  }
}

object HPics {
  def apply(pics: collection.immutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new HPics(pics)
  def apply(pics: collection.mutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new HPics(pics)
  def apply(pics: Picture*)(implicit kojoWorld: KojoWorld) = new HPics(pics)
}

class HPics(pics: Seq[Picture])(implicit val kojoWorld: KojoWorld) extends BasePicSequece(pics) {
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  ready.foreach { _ =>
    var ox = 0.0
    pics.foreach { pic =>
      pic.offset(ox, 0)
      val nbounds = pic.bounds
      ox = nbounds.x + nbounds.width
    }
  }
}

object HPicsCentered {
  def apply(pics: collection.immutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new HPicsCentered(pics)
  def apply(pics: collection.mutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new HPicsCentered(pics)
  def apply(pics: Picture*)(implicit kojoWorld: KojoWorld) = new HPicsCentered(pics)
}

class HPicsCentered(pics: Seq[Picture])(implicit val kojoWorld: KojoWorld) extends BasePicSequece(pics) {
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
}

object VPics {
  def apply(pics: collection.immutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new VPics(pics)
  def apply(pics: collection.mutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new VPics(pics)
  def apply(pics: Picture*)(implicit kojoWorld: KojoWorld) = new VPics(pics)
}

class VPics(pics: Seq[Picture])(implicit val kojoWorld: KojoWorld) extends BasePicSequece(pics) {
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  ready.foreach { _ =>
    var oy = 0.0
    pics.foreach { pic =>
      pic.offset(0, oy)
      val nbounds = pic.bounds
      oy = nbounds.y + nbounds.height
    }
  }
}

object VPicsCentered {
  def apply(pics: collection.immutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new VPicsCentered(pics)
  def apply(pics: collection.mutable.Seq[Picture])(implicit kojoWorld: KojoWorld) = new VPicsCentered(pics)
  def apply(pics: Picture*)(implicit kojoWorld: KojoWorld) = new VPicsCentered(pics)
}

class VPicsCentered(pics: Seq[Picture])(implicit val kojoWorld: KojoWorld) extends BasePicSequece(pics) {
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  ready.foreach { _ =>
    var prevPic: Option[Picture] = None
    pics.foreach { pic =>
      prevPic match {
        case Some(ppic) =>
          val pbounds = ppic.bounds
          val ty = pbounds.y + pbounds.height
          pic.offset(0, ty)
          val bounds = pic.bounds
          val tx = pbounds.x - bounds.x + (pbounds.width - bounds.width) / 2
          val ty2 = pbounds.y + pbounds.height - bounds.y
          pic.offset(tx, ty2)
        case None =>
      }
      prevPic = Some(pic)
    }
  }
}
