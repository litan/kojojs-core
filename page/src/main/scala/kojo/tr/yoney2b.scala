package kojo.tr

/**
 * A 2-dimensional vector, for the physics of games: speed, bounce, direction.
 *
 * DESIGN: on the desktop `Yöney2B` is a case class WRAPPING Vector2D. Here it is bound as a TYPE ALIAS
 * (`type Yöney2B = kojo.Vector2D`), because the bounce functions of the web Kojo (`bouncePicOffStage` and
 * the like) want a real `Vector2D`: with a wrapper every call would have to unwrap and wrap, and Turkish
 * code would stop fitting the English API. The same reason holds for Nokta.
 */
trait Yöney2BYöntemleri extends TemelTürler with BelkiYöntemleri {
  type Yöney2B = kojo.Vector2D

  object Yöney2B {
    def apply(x: Kesir, y: Kesir): Yöney2B = kojo.Vector2D(x, y)
    def unapply(y: Yöney2B): Belki[(Kesir, Kesir)] =
      if (y == null) None else Some((y.x, y.y))
    val sıfır: Yöney2B = kojo.Vector2D(0, 0)
    /** The unit vector of an angle (in degrees). */
    def açıdan(açı: Kesir): Yöney2B =
      kojo.Vector2D(math.cos(math.toRadians(açı)), math.sin(math.toRadians(açı)))
  }

  implicit class Yöney2BMetotları(y: Yöney2B) {
    def döndür(açı: Kesir): Yöney2B = y.rotate(açı)
    def büyüt(oran: Kesir): Yöney2B = y.scale(oran)
    def boyunuBirYap: Yöney2B = y.normalize
    def boyu: Kesir = y.magnitude
    def boyunKaresi: Kesir = y.magSquared
    def sınırla(sınır: Kesir): Yöney2B = y.limit(sınır)
    def içÇarpım(y2: Yöney2B): Kesir = y.dot(y2)
    def izdüşümü(y2: Yöney2B): Yöney2B = y.project(y2)
    def ağırlıklıToplam(y2: Yöney2B, oran: Kesir): Yöney2B = y.lerp(y2, oran)
    def uzaklığı(y2: Yöney2B): Kesir = y.distance(y2)
    def doğrultu: Kesir = y.heading
    def açısı(y2: Yöney2B): Kesir = y.angle(y2)
    def açısı2(y2: Yöney2B): Kesir = y.angleTo(y2)
    def yansıt(y2: Yöney2B): Yöney2B = y.bounceOff(y2)
    def noktaya: Nokta2B = (y.x, y.y)
  }

  /** A helper to read a Yöney2B as an (x, y) pair. */
  type Nokta2B = (Kesir, Kesir)
}
