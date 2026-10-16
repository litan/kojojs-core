package kojo.tr

import pixiscalajs.PIXI

/**
 * Nokta (point) and Dikdörtgen (rectangle), the basic geometric types.
 *
 * They are bound to PIXI's Point and Rectangle. PIXI.Point's x and y are `var`s, so a Nokta is mutable:
 * a point that a picture returns as its position is the picture's live position, not a copy. Use
 * `kopyası` to keep it.
 */
trait NoktaYöntemleri extends TemelTürler with BelkiYöntemleri {
  type Nokta = PIXI.Point
  type Dikdörtgen = PIXI.Rectangle

  object Nokta {
    def apply(x: Kesir, y: Kesir): Nokta = new PIXI.Point(x, y)
    def unapply(n: Nokta): Belki[(Kesir, Kesir)] = if (n == null) None else Some((n.x, n.y))
    val sıfır: Nokta = new PIXI.Point(0, 0)
  }

  object Dikdörtgen {
    def apply(x: Kesir, y: Kesir, en: Kesir, boy: Kesir): Dikdörtgen =
      new PIXI.Rectangle(x, y, en, boy)
  }

  implicit class NoktaMetotları(n: Nokta) {
    def kopyası: Nokta = new PIXI.Point(n.x, n.y)
    def uzaklığı(öbürü: Nokta): Kesir =
      math.sqrt(math.pow(öbürü.x - n.x, 2) + math.pow(öbürü.y - n.y, 2))
    def açısı(öbürü: Nokta): Kesir = math.toDegrees(math.atan2(öbürü.y - n.y, öbürü.x - n.x))
    def taşınmış(dx: Kesir, dy: Kesir): Nokta = new PIXI.Point(n.x + dx, n.y + dy)
    def yazıya: Yazı = s"Nokta(${n.x}, ${n.y})"
  }

  implicit class DikdörtgenMetotları(d: Dikdörtgen) {
    def eni: Kesir = d.width
    def boyu: Kesir = d.height
    def solu: Kesir = d.x
    def altı: Kesir = d.y
    def sağı: Kesir = d.x + d.width
    def üstü: Kesir = d.y + d.height
    def merkezi: Nokta = new PIXI.Point(d.x + d.width / 2, d.y + d.height / 2)
    def içeriyorMu(nokta: Nokta): İkil =
      nokta.x >= d.x && nokta.x <= d.x + d.width &&
        nokta.y >= d.y && nokta.y <= d.y + d.height
    def yazıya: Yazı = s"Dikdörtgen(${d.x}, ${d.y}, ${d.width}, ${d.height})"
  }
}
