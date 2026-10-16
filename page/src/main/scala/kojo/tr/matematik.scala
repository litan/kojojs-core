package kojo.tr

/**
 * Turkish names for the math library.
 *
 * Unlike desktop Kojo, Apache Commons Math (`ArithmeticUtils`, `StatUtils`) is not available in
 * Scala.js, so gcd, lcm, mean and variance are written by hand here.
 *
 * The `uzaklık` and `açı` versions that take points come through NoktaYöntemleri (Nokta = PIXI.Point).
 */
trait MatematikYöntemleri extends TemelTürler with NoktaYöntemleri {
  def piSayısı: Kesir = math.Pi
  def eSayısı: Kesir = math.E

  def yuvarla(sayı: Kesir, basamaklar: Sayı = 0): Kesir = {
    val faktor = math.pow(10, basamaklar)
    math.round(sayı * faktor).toLong / faktor
  }
  def karesi(x: Kesir): Kesir = math.pow(x, 2)
  def karekökü(x: Kesir): Kesir = math.sqrt(x)
  def kuvveti(x: Kesir, k: Kesir): Kesir = math.pow(x, k)
  val gücü = kuvveti _
  def eüssü(x: Kesir): Kesir = math.exp(x)
  def onlukTabandaLogu(x: Kesir): Kesir = math.log10(x)
  def doğalLogu(x: Kesir): Kesir = math.log(x)
  def logaritması(x: Kesir): Kesir = math.log(x)
  def logTabanlı(x: Kesir, t: Kesir): Kesir = math.log(x) / math.log(t)
  private lazy val ln2 = math.log(2)
  def log2tabanlı(x: Kesir): Kesir = math.log(x) / ln2

  def radyana(açı: Kesir): Kesir = math.toRadians(açı)
  def dereceye(açı: Kesir): Kesir = math.toDegrees(açı)
  def sinüs(açı: Kesir): Kesir = math.sin(açı)
  def kosinüs(açı: Kesir): Kesir = math.cos(açı)
  def tanjant(açı: Kesir): Kesir = math.tan(açı)
  def sinüsünAçısı(x: Kesir): Kesir = math.asin(x)
  def kosinüsünAçısı(x: Kesir): Kesir = math.acos(x)
  def tanjantınAçısı(x: Kesir): Kesir = math.atan(x)
  def taban(x: Kesir): Kesir = math.floor(x)
  def tavan(x: Kesir): Kesir = math.ceil(x)
  def yakını(x: Kesir): Kesir = math.rint(x)
  def işareti(x: Kesir): Sayı = x.sign.toInt
  def sayıya(x: Kesir): Sayı = x.toInt
  def rasgele: Kesir = math.random()

  def mutlakDeğer(x: Sayı): Sayı = math.abs(x)
  def mutlakDeğer(x: Uzun): Uzun = math.abs(x)
  def mutlakDeğer(x: Kesir): Kesir = math.abs(x)
  def mutlakDeğer(x: UfakKesir): UfakKesir = math.abs(x)
  def yakın(x: Kesir): Uzun = math.round(x)
  def yakın(x: UfakKesir): Sayı = math.round(x)

  def enİrisi(x: Sayı, y: Sayı): Sayı = math.max(x, y)
  def enUfağı(x: Sayı, y: Sayı): Sayı = math.min(x, y)
  def enİrisi(x: Uzun, y: Uzun): Uzun = math.max(x, y)
  def enUfağı(x: Uzun, y: Uzun): Uzun = math.min(x, y)
  def enİrisi(x: Kesir, y: Kesir): Kesir = math.max(x, y)
  def enUfağı(x: Kesir, y: Kesir): Kesir = math.min(x, y)
  def enİrisi(x: UfakKesir, y: UfakKesir): UfakKesir = math.max(x, y)
  def enUfağı(x: UfakKesir, y: UfakKesir): UfakKesir = math.min(x, y)

  // Helpers for the booklets:
  def bölünüyorMu(sayı: Sayı, bölen: Sayı): İkil = sayı % bölen == 0
  def bölünüyorMu(sayı: Uzun, bölen: Uzun): İkil = sayı % bölen == 0
  def buSaniye: Kesir = System.currentTimeMillis() / 1000.0 // the seconds of the current time

  // Euclid's algorithm instead of Commons Math
  def enİriOrtakPayda(s1: Sayı, s2: Sayı): Sayı = {
    var (a, b) = (math.abs(s1), math.abs(s2))
    while (b != 0) { val t = b; b = a % b; a = t }
    a
  }
  def enİriOrtakPayda(s1: Uzun, s2: Uzun): Uzun = {
    var (a, b) = (math.abs(s1), math.abs(s2))
    while (b != 0) { val t = b; b = a % b; a = t }
    a
  }
  def enUfakOrtakKat(s1: Sayı, s2: Sayı): Sayı =
    if (s1 == 0 || s2 == 0) 0 else math.abs(s1 / enİriOrtakPayda(s1, s2) * s2)
  def enUfakOrtakKat(s1: Uzun, s2: Uzun): Uzun =
    if (s1 == 0 || s2 == 0) 0L else math.abs(s1 / enİriOrtakPayda(s1, s2) * s2)

  def uzaklık(x1: Kesir, y1: Kesir, x2: Kesir, y2: Kesir): Kesir =
    math.sqrt(math.pow(y2 - y1, 2) + math.pow(x2 - x1, 2))
  def açı(x1: Kesir, y1: Kesir, x2: Kesir, y2: Kesir): Kesir =
    math.toDegrees(math.atan2(y2 - y1, x2 - x1))
  def uzaklık(n1: Nokta, n2: Nokta): Kesir = uzaklık(n1.x, n1.y, n2.x, n2.y)
  def açı(n1: Nokta, n2: Nokta): Kesir = açı(n1.x, n1.y, n2.x, n2.y)

  // instead of StatUtils
  def ortalama(sayılar: Array[Kesir]): Kesir =
    if (sayılar.isEmpty) Double.NaN else sayılar.sum / sayılar.length
  def değişim(sayılar: Array[Kesir]): Kesir = değişim(sayılar, ortalama(sayılar))
  def değişim(sayılar: Array[Kesir], ortalama: Kesir): Kesir =
    if (sayılar.length < 2) Double.NaN
    else sayılar.map(x => (x - ortalama) * (x - ortalama)).sum / (sayılar.length - 1)
}
