package kojo

import kojo.doodle.Color

abstract class ComposableTransformer extends Function1[Picture, Picture] { outer =>
  def apply(p: Picture): Picture
  def -> (p: Picture) = apply(p)
  def *(other: ComposableTransformer) = new ComposableTransformer {
    def apply(p: Picture): Picture = {
      other.apply(outer.apply(p))
    }
  }
}

case class Rotate(angle: Double) extends ComposableTransformer {
  def apply(p: Picture): Picture = {
    p.rotate(angle)
    p
  }
}

case class Translate(x: Double, y: Double) extends ComposableTransformer {
  def apply(p: Picture): Picture = {
    p.translate(x, y)
    p
  }
}

case class Scale(f: Double) extends ComposableTransformer {
  def apply(p: Picture): Picture = {
    p.scale(f)
    p
  }
}

case class PenColor(c: Color) extends ComposableTransformer {
  def apply(p: Picture): Picture = {
    p.setPenColor(c)
    p
  }
}

case class PenThickness(t: Double) extends ComposableTransformer {
  def apply(p: Picture): Picture = {
    p.setPenThickness(t)
    p
  }
}

case class FillColor(c: Color) extends ComposableTransformer {
  def apply(p: Picture): Picture = {
    p.setFillColor(c)
    p
  }
}
