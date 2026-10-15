package kojo.tr

import scala.scalajs.js

/**
 * The `BuAn()` (the current moment) of desktop Kojo (lite/i18n/tr/buan.scala).
 *
 * The desktop implementation is built on `java.util.Calendar` (`Takvim` in Turkish). That class does not
 * exist in the browser, so this one is written with JavaScript's `Date`. The field names and their
 * meanings are the same as on the desktop.
 *
 * The types `Takvim`, `Tarih` and `SaatDilimi` themselves do not exist yet; they are Java library
 * types and a separate job.
 */
trait BuAnYöntemleri extends TemelTürler {
  case class BuAn() {
    private val d = new js.Date()
    val saniye: Sayı = d.getSeconds().toInt
    val dakika: Sayı = d.getMinutes().toInt
    val saat: Sayı = d.getHours().toInt
    val gün: Sayı = d.getDate().toInt
    /** The month, 1..12 (JavaScript counts from 0; as on the desktop we start at 1). */
    val ay: Sayı = d.getMonth().toInt + 1
    val yıl: Sayı = d.getFullYear().toInt
    /** The readable form, by the browser's locale. */
    def İngilizce: Yazı = d.toString()
    def hepsi: Yazı = f"$yıl-$ay%02d-$gün%02d $saat%02d:$dakika%02d:$saniye%02d"
    override def toString: Yazı = hepsi
  }
}
