package kojo.tr

/**
 * Turkish names for List.
 *
 * The `ParalelDiziYöntemleri` (and `Dizin.paralel`) of desktop Kojo are NOT here: `scala.collection.parallel`
 * does not exist in Scala.js at all, because the browser has one thread. This is a permanent loss of
 * the port: with no counterpart in the browser even a shim cannot be written.
 */
trait DizinYöntemleri extends TemelTürler {
  val Boş = collection.immutable.Nil

  object Dizin {
    def apply[A](ögeler: A*): Dizin[A] = ögeler.toList
    def unapplySeq[A](list: Dizin[A]) = List.unapplySeq(list)
    def boş[A]: Dizin[A] = Nil
    def doldur[A](n: Sayı)(f: Sayı => A): Dizin[A] = List.tabulate(n)(f)
  }

  implicit class DizinMetotları[T](d: Dizin[T]) {
    type Belki[B] = Option[B]
    type Col = Dizin[T]
    type Eşlek[A, D] = collection.immutable.Map[A, D]

    def başı: T = d.head
    def kuyruğu: Col = d.tail
    def önü: Col = d.init
    def sonu: T = d.last
    def boyu: Sayı = d.length
    def boşMu: İkil = d.isEmpty
    def doluMu: İkil = d.nonEmpty
    def ele(deneme: T => İkil): Col = d.filter(deneme)
    def eleDeğilse(deneme: T => İkil): Col = d.filterNot(deneme)
    def işle[A](işlev: T => A): Dizin[A] = d.map(işlev)
    def düzİşle[A](işlev: T => Dizin[A]): Dizin[A] = d.flatMap(işlev)
    def sıralı(implicit ord: Ordering[T]): Col = d.sorted(ord)
    def sırala[A](i: T => A)(implicit ord: Ordering[A]): Col = d.sortBy(i)
    def sırayaSok(önce: (T, T) => İkil): Col = d.sortWith(önce)
    def indirge[B >: T](işlem: (B, B) => B): B = d.reduce(işlem)
    def soldanKatla[T2](z: T2)(işlev: (T2, T) => T2): T2 = d.foldLeft(z)(işlev)
    def sağdanKatla[T2](z: T2)(işlev: (T, T2) => T2): T2 = d.foldRight(z)(işlev)
    def topla[T2 >: T](implicit num: Numeric[T2]) = d.sum(num)
    def çarp[T2 >: T](implicit num: Numeric[T2]) = d.product(num)
    def yinelemesiz: Col = d.distinct
    def yinelemesizİşlevle[T2](işlev: T => T2): Col = d.distinctBy(işlev)
    def yazıYap: Yazı = d.mkString
    def yazıYap(ara: Yazı): Yazı = d.mkString(ara)
    def yazıYap(başı: Yazı, ara: Yazı, sonu: Yazı): Yazı = d.mkString(başı, ara, sonu)
    def tersi: Col = d.reverse
    def değiştir[S >: T](yeri: Sayı, değeri: S): Dizin[S] = d.updated(yeri, değeri)
    def herbiriİçin[S](işlev: T => S): Birim = d.foreach(işlev)
    def varMı(deneme: T => İkil): İkil = d.exists(deneme)
    def hepsiDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def hepsiİçinDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def içeriyorMu[S >: T](öge: S): İkil = d.contains(öge)
    def al(n: Sayı): Col = d.take(n)
    def alDoğruKaldıkça(deneme: T => İkil): Col = d.takeWhile(deneme)
    def alSağdan(n: Sayı): Col = d.takeRight(n)
    def düşür(n: Sayı): Col = d.drop(n)
    def düşürDoğruKaldıkça(deneme: T => İkil): Col = d.dropWhile(deneme)
    def düşürSağdan(n: Sayı): Col = d.dropRight(n)
    def sırası[S >: T](öge: S): Sayı = d.indexOf(öge)
    def sırasıSondan[S >: T](öge: S): Sayı = d.lastIndexOf(öge)
    def dizine: Dizin[T] = d
    def diziye: Dizi[T] = d.toSeq
    def kümeye: Set[T] = d.toSet
    def yöneye: Vector[T] = d.toVector
    def say(işlev: T => İkil): Sayı = d.count(işlev)
    def dilim(nereden: Sayı, nereye: Sayı): Col = d.slice(nereden, nereye)
    def ikile[S](öbürü: Yinelenebilir[S]) = d.zip(öbürü)
    def ikileSırayla = d.zipWithIndex
    def öbekle[A](iş: T => A): Eşlek[A, Col] = d.groupBy(iş)
    def böl(deneme: T => İkil): (Col, Col) = d.partition(deneme)
    def enUfağı[B >: T](implicit sıralama: Ordering[B]): T = d.min(sıralama)
    def enİrisi[B >: T](implicit sıralama: Ordering[B]): T = d.max(sıralama)
    def enUfağıİşlevle[B](iş: T => B)(implicit k: Ordering[B]): T = d.minBy(iş)(k)
    def enİrisiİşlevle[B](iş: T => B)(implicit k: Ordering[B]): T = d.maxBy(iş)(k)
  
    // --- ends, searching -----------------------------------------------
    def başıBelki: Belki[T] = d.headOption
    def sonuBelki: Belki[T] = d.lastOption
    def bul(deneme: T => İkil): Belki[T] = d.find(deneme)
    def bulSondan(deneme: T => İkil): Belki[T] = d.findLast(deneme)
    def nerede(deneme: T => İkil): Sayı = d.indexWhere(deneme)
    def nerede(deneme: T => İkil, başlamaNoktası: Sayı): Sayı = d.indexWhere(deneme, başlamaNoktası)
    def neredeSondan(deneme: T => İkil): Sayı = d.lastIndexWhere(deneme)
    def dilimSırası[S >: T](dilim: Diz[S]): Sayı = d.indexOfSlice(dilim)
    def dilimSırasıSondan[S >: T](dilim: Diz[S]): Sayı = d.lastIndexOfSlice(dilim)
    def sıralar: Range = d.indices
    def başındaMı[S >: T](dizi: Yinelenebilir[S]): İkil = d.startsWith(dizi)
    def sonundaMı[S >: T](dizi: Yinelenebilir[S]): İkil = d.endsWith(dizi)
    def karşılıklıMı[S](öbürü: Diz[S])(deneme: (T, S) => İkil): İkil = d.corresponds(öbürü)(deneme)

    // --- splitting, grouping -------------------------------------------
    def bölİşle[A1, A2](işlev: T => Either[A1, A2]): (Dizin[A1], Dizin[A2]) = d.partitionMap(işlev)
    def bölDoğruKaldıkça(deneme: T => İkil): (Col, Col) = d.span(deneme)
    def bölYerinden(yeri: Sayı): (Col, Col) = d.splitAt(yeri)
    def öbekli(boy: Sayı): Yineleyici[Col] = d.grouped(boy)
    def kayarÖbekli(boy: Sayı): Yineleyici[Col] = d.sliding(boy)
    def kayarÖbekli(boy: Sayı, adım: Sayı): Yineleyici[Col] = d.sliding(boy, adım)
    def öbekleİşle[K, B](anahtar: T => K)(değer: T => B): Eşlek[K, Dizin[B]] = d.groupMap(anahtar)(değer)
    def öbekleİşleİndirge[K, B](anahtar: T => K)(değer: T => B)(indirge: (B, B) => B): Eşlek[K, B] =
      d.groupMapReduce(anahtar)(değer)(indirge)
    def kombinasyonlar(ögeSayısı: Sayı): Yineleyici[Col] = d.combinations(ögeSayısı)
    def permütasyonlar: Yineleyici[Col] = d.permutations
    def kuyruklar: Yineleyici[Col] = d.tails
    def önler: Yineleyici[Col] = d.inits

    // --- folding, reducing, scanning -----------------------------------
    def katla[S >: T](z: S)(işlev: (S, S) => S): S = d.fold(z)(işlev)
    def indirgeSoldan[S >: T](işlem: (S, T) => S): S = d.reduceLeft(işlem)
    def indirgeSağdan[S >: T](işlem: (T, S) => S): S = d.reduceRight(işlem)
    def indirgeBelki[S >: T](işlem: (S, S) => S): Belki[S] = d.reduceOption(işlem)
    def indirgeSoldanBelki[S >: T](işlem: (S, T) => S): Belki[S] = d.reduceLeftOption(işlem)
    def indirgeSağdanBelki[S >: T](işlem: (T, S) => S): Belki[S] = d.reduceRightOption(işlem)
    // tara: like katla, but gives ALL the intermediate results
    def tara[S >: T](z: S)(işlev: (S, S) => S): Dizin[S] = d.scan(z)(işlev)
    def taraSoldan[B](z: B)(işlev: (B, T) => B): Dizin[B] = d.scanLeft(z)(işlev)
    def taraSağdan[B](z: B)(işlev: (T, B) => B): Dizin[B] = d.scanRight(z)(işlev)
    def enUfağıBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.minOption(sıralama)
    def enUfağıBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.minByOption(iş)(karşılaştırma)
    def enİrisiBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.maxOption(sıralama)
    def enİrisiBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.maxByOption(iş)(karşılaştırma)

    // --- adding, removing ----------------------------------------------
    def sonunaEkle[S >: T](öge: S): Dizin[S] = d.appended(öge)
    def önüneEkle[S >: T](öge: S): Dizin[S] = d.prepended(öge)
    def sonunaEkleHepsini[S >: T](öbürü: YinelenebilirBirKere[S]): Dizin[S] = d.appendedAll(öbürü)
    def önüneEkleHepsini[S >: T](öbürü: YinelenebilirBirKere[S]): Dizin[S] = d.prependedAll(öbürü)
    def uzat[S >: T](boy: Sayı, öge: S): Dizin[S] = d.padTo(boy, öge)
    def yama[S >: T](nereden: Sayı, yenisi: YinelenebilirBirKere[S], kaçTane: Sayı): Dizin[S] =
      d.patch(nereden, yenisi, kaçTane)
    def fark[S >: T](öbürü: Diz[S]): Col = d.diff(öbürü)
    def kesişim[S >: T](öbürü: Diz[S]): Col = d.intersect(öbürü)
    def bileşim[S >: T](öbürü: Diz[S]): Dizin[S] = d.concat(öbürü)

    // --- selecting, flattening, pairwise operations --------------------
    def seçİşle[B](işlev: PartialFunction[T, B]): Dizin[B] = d.collect(işlev)
    def seçİşleİlk[B](işlev: PartialFunction[T, B]): Belki[B] = d.collectFirst(işlev)
    def düzleştir[B](implicit delil: T => YinelenebilirBirKere[B]): Dizin[B] = d.flatten(delil)
    def devrik[B](implicit delil: T => Yinelenebilir[B]): Dizin[Dizin[B]] = d.transpose(delil)
    def ikiliyiAç[A1, A2](implicit delil: T => (A1, A2)): (Dizin[A1], Dizin[A2]) = d.unzip(delil)
    def ikileHepsini[B, S >: T](öbürü: Yinelenebilir[B], buDolgu: S, oDolgu: B): Dizin[(S, B)] =
      d.zipAll(öbürü, buDolgu, oDolgu)
    def tersİşle[B](işlev: T => B): Dizin[B] = d.reverse.map(işlev)
}
}
