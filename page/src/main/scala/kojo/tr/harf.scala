package kojo.tr

/**
 * Turkish names for Char.
 *
 * The desktop aliases `ArraySeq.ofChar`, `ArrayCharSequence` and `SeqCharSequence` are not here: the
 * first is specific to Scala 2.13, the other two are internal types with no practical value for children.
 */
trait HarfYöntemleri extends TemelTürler {
  type Harf = Char

  object Harf {
    def sayıMı(h: Harf): İkil = h.isDigit
    def harfMi(h: Harf): İkil = h.isLetter
    def kutuyaKoy(h: Harf) = Char.box(h)
    def kutudanÇıkar(h: HerGönder) = Char.unbox(h)
    def sayıya(h: Harf) = Char.char2int(h)
    def uzuna(h: Harf) = Char.char2long(h)
    def kesire(h: Harf) = Char.char2double(h)
    def ufakKesire(h: Harf) = Char.char2float(h)
    // desktop Kojo has these the wrong way round (enUfağı = MaxValue); fixed here
    val enUfağı = Char.MinValue
    val enİrisi = Char.MaxValue
  }

  /**
   * The Turkish rule for the dotted and dotless i. Java's and JS's default toUpper/toLower are English:
   * 'i'.toUpper = 'I' (in Turkish it must be 'İ') and 'I'.toLower = 'i' (in Turkish it must be 'ı').
   * In a Turkish product "istanbul".büyükHarfe must not give "ISTANBUL".
   */
  private[tr] def trBüyüt(h: Char): Char = h match {
    case 'i' => 'İ'
    case 'ı' => 'I'
    case _   => h.toUpper
  }
  private[tr] def trKüçült(h: Char): Char = h match {
    case 'I' => 'ı'
    case 'İ' => 'i'
    case _   => h.toLower
  }

  implicit class HarfMetotları(h: Harf) {
    def yazıya: Yazı = h.toString
    def büyükHarfe: Harf = trBüyüt(h)
    def küçükHarfe: Harf = trKüçült(h)
    def sayıya: Sayı = h.toInt
    def kesire: Kesir = h.toDouble
    def sayıMı: İkil = h.isDigit
    def harfMi: İkil = h.isLetter
    def boşlukMu: İkil = h.isWhitespace
    def küçükHarfMi: İkil = h.isLower
    def büyükHarfMi: İkil = h.isUpper
  }
}
