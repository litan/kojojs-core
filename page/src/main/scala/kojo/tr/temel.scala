package kojo.tr

/**
 * Turkish type aliases and core values.
 *
 * Why a trait and not a package object: a script does `import trTurtle._`, so everything it should
 * see has to be a member of TurkishTurtle. Aliases in a package object do not come with that import.
 *
 * The other kojo.tr traits extend this one, so that they can use names like Sayı and İkil.
 *
 * `doğru` and `yanlış` are keywords of the patched Scala compiler of desktop Kojo, not library values.
 * Here the compiler is the standard one, so they are provided as `val`s.
 */
trait TemelTürler {
  type Nesne = Object
  type Birim = Unit
  type Her = Any
  type HerDeğer = AnyVal
  type HerGönder = AnyRef
  type Yok = Null
  type Hiç = Nothing

  type İkil = Boolean
  type Seçim = Boolean
  // These two lines read differently in the two compilers:
  //  - standard compiler: two ordinary member definitions;
  //  - patched (desktop) compiler: doğru/yanlış are keywords there, so the lines parse as literal
  //    patterns like `val true = true`, which always match and bind no member. That is fine, because
  //    the keywords already resolve in user code on that compiler.
  // So `doğru` works in both. Do not delete the lines (the standard compiler needs them), and do not
  // call them as members from elsewhere (they do not exist on the patched compiler).
  val doğru = true
  val yanlış = false

  type Lokma = Byte
  type Kısa = Short
  type Sayı = Int
  type Uzun = Long
  type İriSayı = BigInt
  type UfakKesir = Float
  type Kesir = Double
  type İriKesir = BigDecimal

  type Yazı = String

  type Diz[B] = collection.Seq[B]
  type Dizi[B] = Seq[B]
  type Dizin[A] = List[A]
  type İşlev1[D, R] = Function1[D, R]
  type İşlev2[D1, D2, R] = Function2[D1, D2, R]
  type İşlev3[D1, D2, D3, R] = Function3[D1, D2, D3, R]
  // A picture transformation that can be combined (translate, rotate, color...): chained with `*`,
  // applied to a picture with `->`.
  type Dönüştürücü = kojo.ComposableTransformer
  type SıralıDizi[A] = IndexedSeq[A]
  type Yineleyici[C] = Iterator[C]
  type Yinelenebilir[C] = Iterable[C]
  type YinelenebilirBirKere[C] = IterableOnce[C]

  type KuralDışı = Exception
  type ÇalışmaSırasıKuralDışı = RuntimeException
  // Turkish names of the error types of desktop Kojo (lite/i18n/tr). All of them exist in Scala.js's
  // java library, except İşParçacığıÖlümü (ThreadDeath): there are no threads in the browser.
  type MatematikselHata = java.lang.ArithmeticException
  type BelirtimHatası = java.lang.AssertionError
  type KuraldışıGirdiHatası = java.lang.IllegalArgumentException
  type EksikTanımHatası = scala.NotImplementedError
  type SınırDışınaTaşmaHatası = java.lang.IndexOutOfBoundsException
  type BoşGöstergeHatası = java.lang.NullPointerException
}
