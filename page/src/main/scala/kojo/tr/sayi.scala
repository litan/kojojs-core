package kojo.tr

/**
 * Turkish names for the number types: extension methods and companion objects. The type aliases are in
 * TemelTürler.
 */
trait SayıYöntemleri extends TemelTürler {
  val İriSayı = BigInt
  val İriKesir = BigDecimal

  type Sayılar = Vector[Sayı]
  object Sayılar {
    def apply(elemanlar: Sayı*): Sayılar = Vector.from(elemanlar)
    def unapplySeq(ss: Sayılar) = Vector.unapplySeq(ss)
  }

  implicit class LokmaMetotları(a: Lokma) {
    def yazıya = a.toString
    def kesire = a.toDouble
    def mutlakDeğer = math.abs(a.toInt).toByte
    def enİrisi(b: Lokma) = if (a >= b) a else b
    def enUfağı(b: Lokma) = if (a <= b) a else b
  }

  implicit class İriSayıMetotları(a: İriSayı) {
    def yazıya = a.toString
    def kesire = a.toDouble
    def mutlakDeğer = a.abs
    def enİrisi(b: İriSayı) = a.max(b)
    def enUfağı(b: İriSayı) = a.min(b)
    def lokmaya: Lokma = a.toByte
  }

  implicit class İriKesirMetotları(a: İriKesir) {
    def yazıya = a.toString
    def kesire = a.toDouble
    def mutlakDeğer = a.abs
    def enİrisi(b: İriKesir) = a.max(b)
    def enUfağı(b: İriKesir) = a.min(b)
    def iriSayıya = a.toBigInt
    def ölçek = a.scale
    def ölçeğiKur(ö: Sayı) = a.setScale(ö)
  }

  implicit class SayıMetotları(a: Sayı) {
    def |-(b: Sayı): Range = a until b
    def |-|(b: Sayı): Range = a to b
    def harfe = a.toChar
    def yazıya = a.toString
    def kesire = a.toDouble
    def mutlakDeğer = a.abs
    def enİrisi(b: Sayı) = a.max(b)
    def enUfağı(b: Sayı) = a.min(b)
    def lokmaya: Lokma = a.toByte
  }

  implicit class KısaMetotları(a: Kısa) {
    def |-(b: Sayı): Range = a until b
    def |-|(b: Sayı): Range = a to b
    def harfe = a.toChar
    def yazıya = a.toString
    def kesire = a.toDouble
    def mutlakDeğer = math.abs(a.toInt).toShort
    def enİrisi(b: Kısa) = if (a >= b) a else b
    def enUfağı(b: Kısa) = if (a <= b) a else b
    def lokmaya: Lokma = a.toByte
  }

  implicit class UzunMetotları(a: Uzun) {
    def harfe = a.toChar
    def yazıya = a.toString
    def kesire = a.toDouble
    def mutlakDeğer = a.abs
    def enİrisi(b: Uzun) = a.max(b)
    def enUfağı(b: Uzun) = a.min(b)
    def lokmaya: Lokma = a.toByte
  }

  implicit class KesirMetotları(a: Kesir) {
    def yazıya = a.toString
    def sayıya = a.toInt
    def dereceye = a.toDegrees
    def radyana = a.toRadians
    def mutlakDeğer = a.abs
    def enİrisi(b: Kesir) = a.max(b)
    def enUfağı(b: Kesir) = a.min(b)
    def taban = a.floor
    def tavan = a.ceil
    def yakın = a.round
  }

  object Lokma { def Enİrisi = Byte.MaxValue; def EnUfağı = Byte.MinValue }
  object Sayı { def Enİrisi = Int.MaxValue; def EnUfağı = Int.MinValue }
  object Kısa { def Enİrisi = Short.MaxValue; def EnUfağı = Short.MinValue }
  object Uzun { def Enİrisi = Long.MaxValue; def EnUfağı = Long.MinValue }
  object Kesir { def Enİrisi = Double.MaxValue; def EnUfağı = Double.MinValue }
  object UfakKesir { def Enİrisi = Float.MaxValue; def EnUfağı = Float.MinValue }
}
