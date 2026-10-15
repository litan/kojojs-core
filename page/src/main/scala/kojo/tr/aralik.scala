package kojo.tr

/**
 * Turkish names for Range.
 *
 * On the desktop `Aralık` is a top-level case class. Here it is INSIDE the trait, because there is no
 * package object and aliases like `Sayı` and `Dizin` come from TemelTürler. A script still reaches it
 * with `import trTurtle._` under the same name.
 */
trait AralıkYöntemleri extends TemelTürler {

  // Aralık IS A TYPE ALIAS (the same pattern as Küme = Set and Dizin = List).
  // It used to be a case class wrapping Range; then a learner who wrote `Aralık(1, 10)` saw about 20
  // methods, and one who wrote `1 |-| 10` saw the roughly 110 methods of the SıralıDizi/Diz wrapper,
  // because that is a Range. The two faces were merged.
  // The special display (`Aralık(1, 4, 7)`) is below, as yazı()/yazıya.
  type Aralık = Range


  object Aralık {
    def apply(ilki: Sayı, sonuncu: Sayı, adım: Sayı = 1): Range = Range(ilki, sonuncu, adım)
    def kapalı(ilki: Sayı, sonuncu: Sayı, adım: Sayı = 1): Range = Range.inclusive(ilki, sonuncu, adım)
    def kesirden(ilki: Kesir, sonuncu: Kesir, adım: Kesir) = Range.BigDecimal(ilki, sonuncu, adım)
    // the same as kesirden; an alias whose name says it is an "open range"
    def kesirdenAçık(ilki: Kesir, sonuncu: Kesir, adım: Kesir) = Range.BigDecimal(ilki, sonuncu, adım)
    def kesirdenKapalı(ilki: Kesir, sonuncu: Kesir, adım: Kesir) =
      Range.BigDecimal.inclusive(ilki, sonuncu, adım)

    /**
     * A display that is friendly to learners: `Aralık(1, 4, 7)`, shortened for long ranges.
     *
     * `Aralık` is a type alias, so `toString` cannot be overridden. The display is meant to be used in
     * TWO places, both looking here: the `yazıya`/`yazı()` method (below) and a host's output panel, if
     * it wants this form. One body, one format, the same as `Aralık.gösterim` of desktop Kojo.
     */
    def gösterim(r: Range): Yazı = {
      val gövde =
        if (r.size <= 10) r.mkString("(", ", ", ")")
        else {
          val (b, s2) = (r.take(5), r.drop(r.size - 5))
          b.mkString("(", ", ", " ...") + s2.mkString(" ", ", ", ")")
        }
      s"Aralık$gövde"
    }
  }

  implicit class RangeMetotları(r: Range) {
    // Moved from the Aralık case class (the old names are kept)
    def ilki: Sayı = r.start
    def sonuncu: Sayı = r.end
    def adımı: Sayı = r.step
    def adım: Sayı = r.step
    def uzunluğu: Sayı = r.size
    def başı: Sayı = r.head
    def sonu: Sayı = r.last
    def herÖgeİçin(komutlar: Sayı => Birim): Birim = r.foreach(komutlar)
    // The display that is friendly to learners: toString cannot be overridden (Aralık is now a type
    // alias), but this method gives the old output.
    def yazı(): Yazı = yazıya
    def yazıya: Yazı = Aralık.gösterim(r)
    def adım(c: Sayı): Range = r by c
    def diziye = r.toSeq
    def dizine = r.toList
    def boyu = r.length
    def içindeMi(s: Sayı) = r.contains(s)

    def işle[B](f: Sayı => B) = r.map(f)
    def elekle(deneme: Sayı => İkil) = r.withFilter(deneme)
    def düzİşle[B](f: Sayı => YinelenebilirBirKere[B]) = r.flatMap(f)
    def herbiriİçin(f: Sayı => Unit) = r.foreach(f)
    def indirge(iş: (Sayı, Sayı) => Sayı): Sayı = r.toSeq.reduce(iş)
    def soldanKatla[B](z: B)(iş: (B, Sayı) => B): B = r.toSeq.foldLeft(z)(iş)
    def sağdanKatla[B](z: B)(iş: (Sayı, B) => B): B = r.toSeq.foldRight(z)(iş)
  }
}
