package kojo.tr

/**
 * The `Yazıyüzü` (font) family of desktop Kojo's Turkish layer (trInit.scala: `type Yazıyüzü = java.awt.Font`,
 * `yazıyüzü(ad, boy[, biçem])`, `yazıyüzleri`). There is no AWT in the browser; it is `kojo.Font`
 * (the font family and the size). On the desktop `biçem` is the bit mask Font.BOLD/ITALIC; here it is the
 * same numbers (1 bold, 2 italic, 3 both).
 */
trait YazıyüzüYöntemleri extends TemelTürler {
  // The SAME type as the English Font (kojo.Font), so that the two sides accept each other's fonts. The
  // fields (ad, boy, biçem) are given by the implicit class below.
  type Yazıyüzü = kojo.Font
  object Yazıyüzü {
    val DÜZ = 0
    val KALIN = 1
    val EĞİK = 2
    def apply(ad: Yazı, boy: Sayı, biçem: Sayı = 0): Yazıyüzü = kojo.Font(ad, boy, biçem)
    def unapply(yy: kojo.Font): Option[(Yazı, Sayı, Sayı)] = Some((yy.name, yy.size, yy.style))
  }
  implicit class YazıyüzüMetotları(yy: kojo.Font) {
    def ad: Yazı = yy.name
    def boy: Sayı = yy.size
    def biçem: Sayı = yy.style
    def kalınMı: İkil = (yy.style & Yazıyüzü.KALIN) != 0
    def eğikMi: İkil = (yy.style & Yazıyüzü.EĞİK) != 0
  }
  // `Font(ad, boy)` is not offered from here: a script imports `builtins._` and `trTurtle._` at the same
  // level, and two `Font`s would be "reference to Font is ambiguous". The one `Font` is on the English side
  // (and makes the same type, kojo.Font). On the desktop `Font(ad, BİÇEM, boy)` of AWT has the arguments in
  // another order; it is not offered here so that it is not silently misread: for three arguments use
  // yazıyüzü(...).
  def yazıyüzü(adı: Yazı, boyu: Sayı): Yazıyüzü = Yazıyüzü(adı, boyu)
  def yazıyüzü(adı: Yazı, boyu: Sayı, biçem: Sayı): Yazıyüzü = Yazıyüzü(adı, boyu, biçem)
  /** The generic families that every browser has; the desktop lists the system fonts. */
  def yazıyüzleri: Dizin[Yazı] = List("sans-serif", "serif", "monospace", "cursive", "fantasy",
    "Arial", "Helvetica", "Verdana", "Georgia", "Times New Roman", "Courier New")
}
