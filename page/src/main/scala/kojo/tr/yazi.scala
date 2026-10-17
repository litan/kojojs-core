package kojo.tr

/**
 * Turkish names for String.
 *
 * Note: `dizime` / `eşleme` turn into the Dizim/Eşlem wrappers on the desktop; here too they are the
 * wrappers of dizim.scala and eslem.scala.
 */
trait YazıYöntemleri extends TemelTürler with BelkiYöntemleri with HarfYöntemleri with DizimYöntemleri with EşlemYöntemleri {
  type EsnekYazı = collection.mutable.StringBuilder

  object Yazı {
    val tırnak = """ " """.trim
    type Harf = Char
    def olarak(n: Nesne) = String.valueOf(n)
    def olarak(n: Kesir) = String.valueOf(n)
    def olarak(n: Sayı) = String.valueOf(n)
    def olarak(n: İkil) = if (n) "doğru" else "yanlış"
    def olarak(n: Harf) = String.valueOf(n)
    def olarak(n: UfakKesir) = String.valueOf(n)
    def olarak(n: Uzun) = String.valueOf(n)
    def olarak(n: Array[Char]) = String.valueOf(n)
    def olarak(n: Array[Char], nereden: Sayı, kaçTane: Sayı) = String.valueOf(n, nereden, kaçTane)
  }

  implicit class YazıMetotları(y: Yazı) {
    type Belki[B] = Option[B]
    type Harf = Char

    def başı: Harf = y.head
    def kuyruğu: Yazı = y.tail
    def önü: Yazı = y.init
    def sonu: Harf = y.last
    def boyu: Sayı = y.length
    def boşMu: İkil = y.isEmpty
    def doluMu: İkil = y.nonEmpty

    def ele(deneme: Harf => İkil): Yazı = y.filter(deneme)
    def eleDeğilse(deneme: Harf => İkil): Yazı = y.filterNot(deneme)
    def işle(işlev: Harf => Harf): Yazı = y.map(işlev)
    def düzİşle(işlev: Harf => Yazı): Yazı = y.flatMap(işlev)
    def sıralı(implicit ord: Ordering[Harf]): Yazı = y.sorted(ord)
    def sırala[A](i: Harf => A)(implicit ord: Ordering[A]): Yazı = y.sortBy(i)
    def sırayaSok(önce: (Harf, Harf) => İkil): Yazı = y.sortWith(önce)
    def indirge[B >: Harf](işlem: (B, B) => B): B = y.reduce(işlem)
    def soldanKatla[T2](z: T2)(işlev: (T2, Harf) => T2): T2 = y.foldLeft(z)(işlev)
    def sağdanKatla[T2](z: T2)(işlev: (Harf, T2) => T2): T2 = y.foldRight(z)(işlev)
    def yinelemesiz: Yazı = y.distinct
    def yinelemesizİşlevle[T2](işlev: Harf => T2): Yazı = y.distinctBy(işlev)
    def yazıYap: Yazı = y.mkString
    def yazıYap(ara: Yazı): Yazı = y.mkString(ara)
    def yazıYap(başı: Yazı, ara: Yazı, sonu: Yazı): Yazı = y.mkString(başı, ara, sonu)
    def tersi: Yazı = y.reverse
    def değiştir(yeri: Sayı, değeri: Harf): Yazı = y.updated(yeri, değeri)
    def herbiriİçin[S](işlev: Harf => S): Birim = y.foreach(işlev)
    def varMı(deneme: Harf => İkil): İkil = y.exists(deneme)
    def hepsiDoğruMu(deneme: Harf => İkil): İkil = y.forall(deneme)
    def hepsiİçinDoğruMu(deneme: Harf => İkil): İkil = y.forall(deneme)
    def içeriyorMu(dilim: Yazı): İkil = y.contains(dilim)
    def al(n: Sayı): Yazı = y.take(n)
    def alDoğruKaldıkça(deneme: Harf => İkil): Yazı = y.takeWhile(deneme)
    def alSağdan(n: Sayı): Yazı = y.takeRight(n)
    def düşür(n: Sayı): Yazı = y.drop(n)
    def düşürDoğruKaldıkça(deneme: Harf => İkil): Yazı = y.dropWhile(deneme)
    def düşürSağdan(n: Sayı): Yazı = y.dropRight(n)
    def sırası(dilim: Yazı): Sayı = y.indexOf(dilim)
    def sırası(dilim: Yazı, başlamaNoktası: Sayı): Sayı = y.indexOf(dilim, başlamaNoktası)
    def sırasıSondan(dilim: Yazı): Sayı = y.lastIndexOf(dilim)
    def say(işlev: Harf => İkil): Sayı = y.count(işlev)

    // specific to yazı
    def kısalt: Yazı = y.trim
    def değiştir(a: Harf, b: Harf): Yazı = y.replace(a, b)
    def değiştir(xler: Yazı, yler: Yazı): Yazı = y.replace(xler, yler)
    def değiştirHepsini(xler: Yazı, yler: Yazı): Yazı = y.replaceAll(xler, yler)
    def değiştirİlkini(xler: Yazı, yler: Yazı): Yazı = y.replaceFirst(xler, yler)
    def böl(delim: Harf): Dizin[Yazı] = y.split(delim).toList
    def böl(delim: Yazı, enÇokParça: Sayı = 0): Dizin[Yazı] = y.split(delim, enÇokParça).toList
    // the Turkish i/İ rule -- see harf.scala
    def büyükHarfe: Yazı = y.map(trBüyüt)
    def küçükHarfe: Yazı = y.map(trKüçült)
    def ilkHarfiBüyült: Yazı = if (y.isEmpty) y else trBüyüt(y.head) + y.tail
    def kıyasla(öbürü: Yazı): Sayı = y.compareTo(öbürü)
    def kıyaslaKüçükHarfBüyükHarfAyrımıYapmadan(öbürü: Yazı): Sayı = y.compareToIgnoreCase(öbürü)
    def eşitMiKüçükHarfBüyükHarfAyrımıYapmadan(öbürü: Yazı): İkil = y.equalsIgnoreCase(öbürü)
    def harf(sıra: Sayı): Harf = y.charAt(sıra)
    def parçası(nereden: Sayı): Yazı = y.substring(nereden)
    def parçası(nereden: Sayı, nereye: Sayı): Yazı = y.substring(nereden, nereye)
    def başındaMı(öbürü: Yazı): İkil = y.startsWith(öbürü)
    def sonundaMı(öbürü: Yazı): İkil = y.endsWith(öbürü)
    def kenarPayınıÇıkar: Yazı = y.stripMargin
    def eşlenirMi(düzenliDeyiş: Yazı): İkil = y.matches(düzenliDeyiş)

    def ikile = y.toBoolean
    def lokmaya = y.toByte
    def kesire = y.toDouble
    def ufakKesire = y.toFloat
    def sayıya = y.toInt
    def kısaya = y.toShort
    def dizine = y.toList
    def diziye = y.toSeq
    def kümeye = y.toSet
    def yöneye = y.toVector
    def dizime[S >: Harf](implicit delil: scala.reflect.ClassTag[S]): Dizim[S] = new Dizim(y.toArray(delil))
    def eşleme[A, D](implicit delil: Harf <:< (A, D)): Eşlem[A, D] = Eşlem.değişmezden(y.toMap)

    def sayıyaBelki: Belki[Sayı] = y.toIntOption
    def kesireBelki: Belki[Kesir] = y.toDoubleOption
    def ufakKesireBelki: Belki[UfakKesir] = y.toFloatOption
    def lokmayaBelki: Belki[Lokma] = y.toByteOption
    def kısayaBelki: Belki[Kısa] = y.toShortOption
    def ikileBelki: Belki[İkil] = y.toBooleanOption

    def öbekle(iş: Harf => Harf): collection.immutable.Map[Harf, Yazı] = y.groupBy(iş)
  
    // --- the core shared with the other collections ---------------
    // NOTE: on Yazı `böl` = split (cutting by a separator). So no name is given to partition here; the
    // counterparts of span/splitAt are below.
    def başıBelki: Belki[Harf] = y.headOption
    def sonuBelki: Belki[Harf] = y.lastOption
    def bul(deneme: Harf => İkil): Belki[Harf] = y.find(deneme)
    def nerede(deneme: Harf => İkil): Sayı = y.indexWhere(deneme)
    def neredeSondan(deneme: Harf => İkil): Sayı = y.lastIndexWhere(deneme)
    def sıralar: Range = y.indices
    def bölDoğruKaldıkça(deneme: Harf => İkil): (Yazı, Yazı) = y.span(deneme)
    def bölYerinden(yeri: Sayı): (Yazı, Yazı) = y.splitAt(yeri)
    def öbekli(boy: Sayı): Yineleyici[Yazı] = y.grouped(boy)
    def kayarÖbekli(boy: Sayı): Yineleyici[Yazı] = y.sliding(boy)
    def kayarÖbekli(boy: Sayı, adım: Sayı): Yineleyici[Yazı] = y.sliding(boy, adım)
    def kuyruklar: Yineleyici[Yazı] = y.tails
    def önler: Yineleyici[Yazı] = y.inits
    def kombinasyonlar(harfSayısı: Sayı): Yineleyici[Yazı] = y.combinations(harfSayısı)
    def permütasyonlar: Yineleyici[Yazı] = y.permutations
    def katla(z: Harf)(işlev: (Harf, Harf) => Harf): Harf = y.fold(z)(işlev)
    def dilim(nereden: Sayı, nereye: Sayı): Yazı = y.slice(nereden, nereye)
    def uzat(boy: Sayı, harf: Harf): Yazı = y.padTo(boy, harf)
    def yama(nereden: Sayı, yenisi: YinelenebilirBirKere[Harf], kaçTane: Sayı): Yazı =
      y.patch(nereden, yenisi, kaçTane)
    def fark(öbürü: Dizi[Harf]): Yazı = y.diff(öbürü)
    def kesişim(öbürü: Dizi[Harf]): Yazı = y.intersect(öbürü)
    def sonunaEkle(harf: Harf): Yazı = y.appended(harf)
    def önüneEkle(harf: Harf): Yazı = y.prepended(harf)
    def sonunaEkleHepsini(öbürü: Yazı): Yazı = y.appendedAll(öbürü)
    def önüneEkleHepsini(öbürü: Yazı): Yazı = y.prependedAll(öbürü)

    // --- only for Yazı ----------------------------------------------
    // NOTE: because `böl` = split, the name of partition is ikiyeAyır.
    def ikiyeAyır(deneme: Harf => İkil): (Yazı, Yazı) = y.partition(deneme)
    def ikiyeAyırİşle[A1, A2](işlev: Harf => Either[A1, A2]): (Dizi[A1], Dizi[A2]) =
      // The overload that is specific to Yazı shadows the general one: over the sequence of letters
      (y: Dizi[Harf]).partitionMap(işlev)
    def seçİşle[B](işlev: PartialFunction[Harf, B]): Dizi[B] = y.collect(işlev)
    def satırlar: Yineleyici[Yazı] = y.linesIterator
    def başındanAt(önek: Yazı): Yazı = y.stripPrefix(önek)
    def sonundanAt(sonek: Yazı): Yazı = y.stripSuffix(sonek)
    def satırSonunuAt: Yazı = y.stripLineEnd
    def uzuna: Uzun = y.toLong
    def uzunaBelki: Belki[Uzun] = y.toLongOption
}

  implicit class EsnekYazıMetotları(ey: EsnekYazı) {
    type Harf = Char
    def boşMu = ey.size == 0
    def doluMu = ey.size != 0
    def boyu = ey.size
    def sil() = ey.clear()
    def ekle[T](x: T) = ey.append(x)
    def yazıya = ey.toString
    def sayıya = ey.toString.toInt
  
    // --- the buffer side (the ones that change in place) ----------
    // EsnekYazı is both a SEQUENCE of letters (the Diz wrapper applies here too: bul, böl, öbekli,
    // enİrisi... all work) and a string BUFFER. The ones below are the buffer side; they change the
    // buffer itself.
    def ekleHepsini(harfler: YinelenebilirBirKere[Harf]): EsnekYazı = ey.appendAll(harfler)
    def ekleAraya(yeri: Sayı, x: Yazı): EsnekYazı = ey.insert(yeri, x)
    def ekleArayaHepsini(yeri: Sayı, harfler: YinelenebilirBirKere[Harf]): EsnekYazı = ey.insertAll(yeri, harfler)
    @deprecated("eylemle başlayan ada geçildi: ekleAraya kullanın", "Eylül 2026")
    def araEkle(yeri: Sayı, x: Yazı): EsnekYazı = ekleAraya(yeri, x)
    @deprecated("eylemle başlayan ada geçildi: ekleArayaHepsini kullanın", "Eylül 2026")
    def araEkleHepsini(yeri: Sayı, harfler: YinelenebilirBirKere[Harf]): EsnekYazı = ekleArayaHepsini(yeri, harfler)
    def aralığıSil(nereden: Sayı, nereye: Sayı): EsnekYazı = ey.delete(nereden, nereye)
    def harfiSil(yeri: Sayı): EsnekYazı = ey.deleteCharAt(yeri)
    def değiştirAralığını(nereden: Sayı, nereye: Sayı, yenisi: Yazı): EsnekYazı =
      ey.replace(nereden, nereye, yenisi)
    def harfiKur(yeri: Sayı, harf: Harf): Birim = ey.setCharAt(yeri, harf)
    def boyuKur(boy: Sayı): Birim = ey.setLength(boy)
    def tersiYerinde: EsnekYazı = ey.reverseInPlace()
    def yerAyır(boy: Sayı): Birim = ey.ensureCapacity(boy)
    def kapasitesi: Sayı = ey.capacity
    def harf(yeri: Sayı): Harf = ey.charAt(yeri)
    def parçası(nereden: Sayı): Yazı = ey.substring(nereden)
    def parçası(nereden: Sayı, nereye: Sayı): Yazı = ey.substring(nereden, nereye)
    def kesire: Kesir = ey.toString.toDouble
    def uzuna: Uzun = ey.toString.toLong
}
}
