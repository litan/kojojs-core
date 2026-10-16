package kojo.tr

/**
 * Core object methods.
 *
 * NOTE: the original file in desktop Kojo uses TURKISH KEYWORDS (`özellik` for trait, `tanım` for def,
 * `baskın` for override), which exist only in the patched scala-tr compiler. The web Kojo compiles with
 * standard Scala, so the keywords are English here; the MEMBER NAMES stay Turkish, so the code a user
 * writes looks the same:
 *
 *     class Nokta extends BaskınYazıyaYöntemiyle { def yazıya = "..." }
 */
trait KökTürYöntemleri extends TemelTürler {

  /** A trait that gives toString through `yazıya`. */
  trait BaskınYazıyaYöntemiyle {
    def yazıya: Yazı
    override def toString = yazıya
  }

  /** A trait that gives hashCode and equals through `kıymaKodu`. */
  trait Eşsizlik {
    def kıymaKodu: Sayı
    override def hashCode = kıymaKodu
    // Looking only at the hash would be wrong: it is not symmetric (nokta == "yazı" could be true
    // while the reverse is not) and a hash collision makes unrelated objects equal, so Küme/Eşlek
    // lookups could return the wrong element. The type check is needed.
    override def equals(h2: Any) = h2 match {
      case e: Eşsizlik => e.kıymaKodu == kıymaKodu
      case _           => false
    }
  }

  implicit class NesneMetotları(h: Nesne) {
    def kıymaKodu = h.hashCode
    def eşitMi(h2: Her) = h.equals(h2)
    def nesnesiOlarak[T] = h.asInstanceOf[T]
    def yazıya = h.toString
  }

  implicit class HerGönderMetotları(h: HerGönder) {
    def aynıMı(h2: HerGönder): İkil = h eq h2
  }
}
