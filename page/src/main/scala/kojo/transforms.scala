package kojo

import com.vividsolutions.jts.geom.Geometry
import kojo.PicCache.freshPic
import kojo.doodle.Color

trait PicTransformer extends Picture {
  val tpic: Picture

  def made = tpic.made

  def ready = tpic.ready

  def tnode = tpic.tnode

  def realDraw() = tpic.realDraw()

  def erase() = tpic.erase()

  def kojoWorld = tpic.kojoWorld

  def setFillColor(c: Color) = tpic.setFillColor(c)

  def setPenColor(c: Color) = tpic.setPenColor(c)

  def setPenThickness(t: Double) = tpic.setPenThickness(t)

  def initGeom(): Geometry = tpic.initGeom()
}

abstract class Transform(pic: Picture) extends PicTransformer {
  val tpic = freshPic(pic)
}

case class PreDrawTransform(fn: Picture => Unit)(pic: Picture) extends Transform(pic) {
  override def draw(): Unit = {
    fn(tpic)
    tpic.draw()
  }
  override def copy = PreDrawTransform(fn)(pic.copy)
  override def toString() = s"PreDrawTransform($fn) (Id: ${System.identityHashCode(this)}) -> ${tpic.toString}"
}

case class PostDrawTransform(fn: Picture => Unit)(pic: Picture) extends Transform(pic) {
  override def draw(): Unit = {
    tpic.draw()
    fn(tpic)
  }
  override def copy = PostDrawTransform(fn)(pic.copy)
  override def toString() = s"PostDrawTransform($fn) (Id: ${System.identityHashCode(this)}) -> ${tpic.toString}"
}

abstract class ComposableTransformer extends Function1[Picture, Picture] { outer =>
  def apply(p: Picture): Picture
  def -> (p: Picture) = apply(p)
  def *(other: ComposableTransformer) = new ComposableTransformer {
    def apply(p: Picture): Picture = {
      outer.apply(other.apply(p))
    }
  }
}

case class PreDrawTransformc(fn: Picture => Unit) extends ComposableTransformer {
  def apply(p: Picture) = PreDrawTransform(fn)(p)
}

case class PostDrawTransformc(fn: Picture => Unit) extends ComposableTransformer {
  def apply(p: Picture) = PostDrawTransform(fn)(p)
}

