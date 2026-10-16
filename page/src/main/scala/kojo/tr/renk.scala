package kojo.tr

import kojo.doodle.Color

/**
 * Turkish names for the colors, with the names of desktop Kojo's Turkish layer (trInit.scala).
 *
 * The desktop has TWO color types, `java.awt.Color` (the basic colors) and `kojo.doodle.Color` (the rich
 * list of names). There is no AWT here, only one type, so the port is SIMPLER: `Renk` = `kojo.doodle.Color`.
 *
 * `renksiz` is KColor.noColor on the desktop; here it is transparent black.
 *
 * Not here: the gradients (`doğrusalDeğişim` and the like) and `DokumaBoya`, which need the paint type
 * `Boya` that this repo does not have.
 */
trait RenkYöntemleri extends TemelTürler {
  type Renk = Color

  /** The color constructors of the desktop: `Renk(kırmızı, yeşil, mavi)`, `Renk(0xrrggbb)` and so on. */
  object Renk {
    def apply(kırmızı: Sayı, yeşil: Sayı, mavi: Sayı, saydamlık: Sayı = 255): Renk =
      Color.rgba(kırmızı, yeşil, mavi, saydamlık)
    def apply(onaltılık: Uzun): Renk = Color(onaltılık)
    def apply(onaltılık: Uzun, saydamlıkVar: İkil): Renk = Color(onaltılık, saydamlıkVar)

    def kym(kırmızı: Sayı, yeşil: Sayı, mavi: Sayı): Renk = Color.rgb(kırmızı, yeşil, mavi)
    def kyms(kırmızı: Sayı, yeşil: Sayı, mavi: Sayı, saydamlık: Sayı): Renk =
      Color.rgba(kırmızı, yeşil, mavi, saydamlık)
    def ada(arıRenk: Kesir, doygunluk: Kesir, açıklık: Kesir): Renk = Color.hsl(arıRenk, doygunluk, açıklık)
    def adas(arıRenk: Kesir, doygunluk: Kesir, açıklık: Kesir, saydamlık: Kesir): Renk =
      Color.hsla(arıRenk, doygunluk, açıklık, saydamlık)
    // the English constructors
    def rgb(r: Sayı, g: Sayı, b: Sayı): Renk = Color.rgb(r, g, b)
    def rgba(r: Sayı, g: Sayı, b: Sayı, a: Sayı): Renk = Color.rgba(r, g, b, a)
    def hsl(h: Kesir, s: Kesir, l: Kesir): Renk = Color.hsl(h, s, l)
    def hsla(h: Kesir, s: Kesir, l: Kesir, a: Kesir): Renk = Color.hsla(h, s, l, a)
  }

  /** The name list of the English side: `renkler.red` and so on. */
  val renkler = Color

  /** Make a color from red, green, blue (0-255) and, optionally, the opacity. */
  def renkKur(kırmızı: Sayı, yeşil: Sayı, mavi: Sayı): Renk = Color.rgb(kırmızı, yeşil, mavi)
  def renkKur(kırmızı: Sayı, yeşil: Sayı, mavi: Sayı, saydamlık: Sayı): Renk =
    Color.rgba(kırmızı, yeşil, mavi, saydamlık)

  object Renkler {
    val renksiz: Renk = Color(0, 0, 0, 0)
    val saydam: Renk  = renksiz
    val mavi: Renk = Color.blue
    val kırmızı: Renk = Color.red
    val sarı: Renk = Color.yellow
    val yeşil: Renk = Color.green
    val mor: Renk = Color.purple
    val pembe: Renk = Color.pink
    val kahverengi: Renk = Color.brown
    val siyah: Renk = Color.black
    val beyaz: Renk = Color.white
    val gri: Renk = Color.gray
    val koyuGri: Renk = Color.darkGray
    val açıkGri: Renk = Color.lightGray
    val turuncu: Renk = Color.orange
    val morumsu: Renk = Color.magenta
    val camgöbeği: Renk = Color.cyan
    val altınbaşak: Renk = Color.goldenrod
    val altın: Renk = Color.gold
    val yeşilimsiSarı: Renk = Color.greenYellow
    val zeytin: Renk = Color.olive
    val orkidePembesi: Renk = Color.orchid
    val somon: Renk = Color.salmon
    val denizYeşili: Renk = Color.seaGreen
    val kurşunMavisi: Renk = Color.slateBlue
    val kurşunGrisi: Renk = Color.slateGray
    val turkuaz: Renk = Color.turquoise
    val menekşe: Renk = Color.violet
    val haki: Renk = Color.khaki
    val mercan: Renk = Color.coral
    val gökMavisi: Renk = Color.skyBlue
    val çelikMavisi: Renk = Color.steelBlue
    val beyazlatılmışBadem: Renk = Color.blanchedAlmond
    val koyuMavi: Renk = Color.darkBlue
    val koyuCamgöbeği: Renk = Color.darkCyan
    val koyuAltınbaşak: Renk = Color.darkGoldenrod
    val koyuKlasikGri: Renk = Color.darkGrayClassic
    val koyuYeşil: Renk = Color.darkGreen
    val koyuHaki: Renk = Color.darkKhaki
    val koyuMorumsu: Renk = Color.darkMagenta
    val koyuZeytinYeşili: Renk = Color.darkOliveGreen
    val koyuTuruncu: Renk = Color.darkOrange
    val koyuOrkidePembesi: Renk = Color.darkOrchid
    val koyuKırmızı: Renk = Color.darkRed
    val koyuSomon: Renk = Color.darkSalmon
    val koyuDenizYeşili: Renk = Color.darkSeaGreen
    val koyuKurşunMavisi: Renk = Color.darkSlateBlue
    val koyuKurşunGrisi: Renk = Color.darkSlateGray
    val koyuTurkuaz: Renk = Color.darkTurquoise
    val koyuMenekşe: Renk = Color.darkViolet
    val açıkMavi: Renk = Color.lightBlue
    val açıkMercan: Renk = Color.lightCoral
    val açıkCamgöbeği: Renk = Color.lightCyan
    val açıkAltınbaşakSarısı: Renk = Color.lightGoldenrodYellow
    val açıkYeşil: Renk = Color.lightGreen
    val açıkPembe: Renk = Color.lightPink
    val açıkSomon: Renk = Color.lightSalmon
    val açıkDenizYeşili: Renk = Color.lightSeaGreen
    val açıkGökMavisi: Renk = Color.lightSkyBlue
    val açıkKurşunGrisi: Renk = Color.lightSlateGray
    val açıkÇelikMavisi: Renk = Color.lightSteelBlue
    val açıkSarı: Renk = Color.lightYellow
  }

  // the common ones without the prefix
  val renksiz = Renkler.renksiz
  val saydam = Renkler.saydam
  val siyah = Renkler.siyah
  val beyaz = Renkler.beyaz
  val açıkGri = Renkler.açıkGri
  val camgöbeği = Renkler.camgöbeği
  val gri = Renkler.gri
  val kahverengi = Renkler.kahverengi
  val koyuGri = Renkler.koyuGri
  val koyuMavi = Renkler.koyuMavi
  val koyuYeşil = Renkler.koyuYeşil
  val koyuKırmızı = Renkler.koyuKırmızı
  val koyuTuruncu = Renkler.koyuTuruncu
  val koyuCamgöbeği = Renkler.koyuCamgöbeği
  val koyuMorumsu = Renkler.koyuMorumsu
  val kırmızı = Renkler.kırmızı
  val mavi = Renkler.mavi
  val mor = Renkler.mor
  val morumsu = Renkler.morumsu
  val pembe = Renkler.pembe
  val sarı = Renkler.sarı
  val turuncu = Renkler.turuncu
  val yeşil = Renkler.yeşil

  implicit class RenkMetotları(r: Renk) {
    def soluk(oran: Double): Renk       = r.fadeOut(oran)
    def belirgin(oran: Double): Renk    = r.fadeIn(oran)
    def çevir(açı: Double): Renk        = r.spin(açı)
    def dahaAçıkYap(açıklık: Double): Renk = r.lighten(açıklık)
    def dahaKoyuYap(koyuluk: Double): Renk = r.darken(koyuluk)
  }
}
