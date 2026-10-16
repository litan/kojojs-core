package kojo

import kojo.doodle.Color

// Turkish names for the turtle commands, the same idea as SwedishTurtle. The names follow desktop Kojo's
// Turkish layer (net.kogics.kojo.lite.i18n.trInit), so a script reads the same in both.
//
// englishTurtle is the TurtleAPI that Builtins passes in: its `turtle` (GlobalTurtleForPicture), not
// `turtle0`. Picture { ... } swaps the global turtle for the picture's own turtle while its body runs, and
// a wrapper bound to turtle0 would draw such a picture's lines on the canvas and leave the picture empty.
//
// Also the speeds and the colors, with the names of desktop Kojo's Turkish layer (the fork's gradients
// and the color constructors are not here).
//
// Only commands that TurtleAPI has are here. Others (home, towards, costumes, saved styles...) follow when
// the English API has them.
class TurkishTurtle(val englishTurtle: TurtleAPI)
    extends kojo.tr.SayıYöntemleri
    with kojo.tr.MatematikYöntemleri
    with kojo.tr.BelkiYöntemleri
    with kojo.tr.NoktaYöntemleri
    with kojo.tr.HarfYöntemleri
    with kojo.tr.İkisindenBiriYöntemleri
    with kojo.tr.BölümselİşlevYöntemleri
    with kojo.tr.KökTürYöntemleri
    with kojo.tr.BuAnYöntemleri
    with kojo.tr.AralıkYöntemleri
    with kojo.tr.DizimYöntemleri
    with kojo.tr.DizinYöntemleri
    with kojo.tr.MiskinDizinYöntemleri
    with kojo.tr.YöneyYöntemleri
    with kojo.tr.Yöney2BYöntemleri {
  // clearing
  def sil(): Unit      = englishTurtle.clear()
  def temizle(): Unit  = englishTurtle.cleari()
  def görünür(): Unit  = englishTurtle.visible()
  def görünmez(): Unit = englishTurtle.invisible()

  // moving and turning
  def ileri(adım: Double): Unit                = englishTurtle.forward(adım)
  def ileri(): Unit                            = englishTurtle.forward()
  def geri(adım: Double): Unit                 = englishTurtle.back(adım)
  def geri(): Unit                             = englishTurtle.back()
  def sağ(açı: Double, yarıçap: Double): Unit  = englishTurtle.right(açı, yarıçap)
  def sağ(açı: Double): Unit                   = englishTurtle.right(açı)
  def sağ(): Unit                              = englishTurtle.right()
  def sol(açı: Double, yarıçap: Double): Unit  = englishTurtle.left(açı, yarıçap)
  def sol(açı: Double): Unit                   = englishTurtle.left(açı)
  def sol(): Unit                              = englishTurtle.left()
  def dön(açı: Double, yarıçap: Double): Unit  = englishTurtle.turn(açı, yarıçap)
  def dön(açı: Double): Unit                   = englishTurtle.turn(açı)
  def zıpla(adım: Double): Unit                = englishTurtle.hop(adım)
  def zıpla(): Unit                            = englishTurtle.hop()
  def atla(x: Double, y: Double): Unit         = englishTurtle.setPosition(x, y)
  def ilerle(x: Double, y: Double): Unit       = englishTurtle.moveTo(x, y)
  def noktayaGit(x: Double, y: Double): Unit   = englishTurtle.lineTo(x, y)
  def açıyaDön(açı: Double): Unit              = englishTurtle.setHeading(açı)
  def doğu(): Unit                             = englishTurtle.setHeading(0)
  def batı(): Unit                             = englishTurtle.setHeading(180)
  def kuzey(): Unit                            = englishTurtle.setHeading(90)
  def güney(): Unit                            = englishTurtle.setHeading(-90)
  def konumVeYönüBelleğeYaz(): Unit            = englishTurtle.savePosHe()
  def konumVeYönüGeriYükle(): Unit             = englishTurtle.restorePosHe()

  // pen
  def kalemiİndir(): Unit                      = englishTurtle.penDown()
  def kalemiKaldır(): Unit                     = englishTurtle.penUp()
  def kalemRenginiKur(renk: Color): Unit       = englishTurtle.setPenColor(renk)
  def boyamaRenginiKur(renk: Color): Unit      = englishTurtle.setFillColor(renk)
  def kalemKalınlığınıKur(kalınlık: Double): Unit = englishTurtle.setPenThickness(kalınlık)

  // shapes and text
  def yay(yarıçap: Double, açı: Double): Unit  = englishTurtle.arc(yarıçap, açı)
  def daire(yarıçap: Double): Unit             = englishTurtle.circle(yarıçap)
  def yazı(metin: String): Unit                = englishTurtle.write(metin)
  def yazıBoyunuKur(boy: Int): Unit            = englishTurtle.setPenFontSize(boy)

  // speed and waiting
  def hızıKur(hız: Speed.Speed): Unit          = englishTurtle.setSpeed(hız)
  def canlandırmaHızınıKur(n: Long): Unit      = englishTurtle.setAnimationDelay(n)
  def bekle(saniye: Double): Unit              = englishTurtle.pause(saniye)

  // repeating
  def yinele(n: Int)(komutlar: => Unit): Unit = RepeatCommands.repeat(n)(komutlar)

  // speeds
  lazy val yavaş    = Speed.slow
  lazy val orta     = Speed.medium
  lazy val hızlı    = Speed.fast
  lazy val çokHızlı = Speed.superFast

  // colors
  type Renk = Color

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
