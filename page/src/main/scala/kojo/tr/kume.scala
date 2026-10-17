package kojo.tr

/**
 * Turkish names for Set.
 *
 * `dizime` and `eşleme` turn into the Dizim/Eşlem wrappers on the desktop; here too they are the wrappers
 * of dizim.scala and eslem.scala.
 */
trait KümeYöntemleri extends TemelTürler with DizimYöntemleri with EşlemYöntemleri {
  type Küme[T] = Set[T]

  object Küme {
    def apply[T](elemanlar: T*): Küme[T] = elemanlar.toSet
    def boş[T] = Set.empty[T]
  }

  implicit class KümeMetotları[T](d: Küme[T]) {
    type Belki[B] = Option[B]
    type Col = Küme[T]
    type Eşlek[A, D] = collection.immutable.Map[A, D]

    def başı: T = d.head
    def kuyruğu: Col = d.tail
    def boyu: Sayı = d.size
    def boşMu: İkil = d.isEmpty
    def doluMu: İkil = d.nonEmpty
    def ele(deneme: T => İkil): Col = d.filter(deneme)
    def eleDeğilse(deneme: T => İkil): Col = d.filterNot(deneme)
    def işle[A](işlev: T => A): Küme[A] = d.map(işlev)
    def düzİşle[A](işlev: T => Küme[A]): Küme[A] = d.flatMap(işlev)
    def indirge[B >: T](işlem: (B, B) => B): B = d.reduce(işlem)
    def soldanKatla[T2](z: T2)(işlev: (T2, T) => T2): T2 = d.foldLeft(z)(işlev)
    def sağdanKatla[T2](z: T2)(işlev: (T, T2) => T2): T2 = d.foldRight(z)(işlev)
    def topla[T2 >: T](implicit num: Numeric[T2]) = d.sum(num)
    def çarp[T2 >: T](implicit num: Numeric[T2]) = d.product(num)
    def yazıYap: Yazı = d.mkString
    def yazıYap(ara: Yazı): Yazı = d.mkString(ara)
    def yazıYap(başı: Yazı, ara: Yazı, sonu: Yazı): Yazı = d.mkString(başı, ara, sonu)
    def herbiriİçin[S](işlev: T => S): Birim = d.foreach(işlev)
    def varMı(deneme: T => İkil): İkil = d.exists(deneme)
    def hepsiDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def hepsiİçinDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def içeriyorMu(öge: T): İkil = d.contains(öge)
    def al(n: Sayı): Col = d.take(n)
    def alDoğruKaldıkça(deneme: T => İkil): Col = d.takeWhile(deneme)
    def alSağdan(n: Sayı): Col = d.takeRight(n)
    def düşür(n: Sayı): Col = d.drop(n)
    def düşürDoğruKaldıkça(deneme: T => İkil): Col = d.dropWhile(deneme)
    def düşürSağdan(n: Sayı): Col = d.dropRight(n)

    def yineleyici: Yineleyici[T] = d.iterator
    def dizine = d.toList
    def diziye = d.toSeq
    def kümeye = d.toSet
    def yöneye = d.toVector
    def dizime[S >: T](implicit delil: scala.reflect.ClassTag[S]): Dizim[S] = new Dizim(d.toArray(delil))
    def eşleme[A, D](implicit delil: T <:< (A, D)): Eşlem[A, D] = Eşlem.değişmezden(d.toMap)
    def eşleğe[A, D](implicit delil: T <:< (A, D)): Eşlek[A, D] = d.toMap
    def say(işlev: T => İkil): Sayı = d.count(işlev)

    def dilim(nereden: Sayı, nereye: Sayı) = d.slice(nereden, nereye)
    def ikile[S](öbürü: Yinelenebilir[S]) = d.zip(öbürü)
    def ikileSırayla = d.zipWithIndex
    def ikileKonumla = d.zipWithIndex
    def öbekle[A](iş: T => A): Eşlek[A, Col] = d.groupBy(iş)

    def enUfağı[B >: T](implicit sıralama: Ordering[B]): T = d.min(sıralama)
    def enUfağıİşlevle[B](iş: T => B)(implicit k: Ordering[B]): T = d.minBy(iş)(k)
    def enİrisi[B >: T](implicit sıralama: Ordering[B]): T = d.max(sıralama)
    def enİrisiİşlevle[B](iş: T => B)(implicit k: Ordering[B]): T = d.maxBy(iş)(k)

    def altKümeleri(ögeSayısı: Sayı): Yineleyici[Col] = d.subsets(ögeSayısı)
    def boş: Col = d.empty
    def böl(deneme: T => İkil): (Col, Col) = d.partition(deneme)
    def kesişim(öbürü: Küme[T]): Küme[T] = d.intersect(öbürü)
    def bileşim(öbürü: Küme[T]): Küme[T] = d.union(öbürü)
    def fark(öbürü: Küme[T]): Küme[T] = d.diff(öbürü)
    def öbekli(boy: Sayı): Yineleyici[Col] = d.grouped(boy)
  
    // --- ends, searching -----------------------------------------------
    def önü: Col = d.init
    def sonu: T = d.last
    def başıBelki: Belki[T] = d.headOption
    def sonuBelki: Belki[T] = d.lastOption
    def bul(deneme: T => İkil): Belki[T] = d.find(deneme)

    // --- splitting, grouping -------------------------------------------
    def bölİşle[A1, A2](işlev: T => Either[A1, A2]): (Küme[A1], Küme[A2]) = d.partitionMap(işlev)
    def bölDoğruKaldıkça(deneme: T => İkil): (Col, Col) = d.span(deneme)
    def bölYerinden(yeri: Sayı): (Col, Col) = d.splitAt(yeri)
    def kayarÖbekli(boy: Sayı): Yineleyici[Col] = d.sliding(boy)
    def kayarÖbekli(boy: Sayı, adım: Sayı): Yineleyici[Col] = d.sliding(boy, adım)
    def öbekleİşle[K, B](anahtar: T => K)(değer: T => B): Eşlek[K, Küme[B]] = d.groupMap(anahtar)(değer)
    def öbekleİşleİndirge[K, B](anahtar: T => K)(değer: T => B)(indirge: (B, B) => B): Eşlek[K, B] =
      d.groupMapReduce(anahtar)(değer)(indirge)
    def kuyruklar: Yineleyici[Col] = d.tails
    def önler: Yineleyici[Col] = d.inits

    // --- folding, reducing, scanning -----------------------------------
    def katla[S >: T](z: S)(işlev: (S, S) => S): S = d.fold(z)(işlev)
    def indirgeSoldan[S >: T](işlem: (S, T) => S): S = d.reduceLeft(işlem)
    def indirgeSağdan[S >: T](işlem: (T, S) => S): S = d.reduceRight(işlem)
    def indirgeBelki[S >: T](işlem: (S, S) => S): Belki[S] = d.reduceOption(işlem)
    def indirgeSoldanBelki[S >: T](işlem: (S, T) => S): Belki[S] = d.reduceLeftOption(işlem)
    def indirgeSağdanBelki[S >: T](işlem: (T, S) => S): Belki[S] = d.reduceRightOption(işlem)
    def tara[S >: T](z: S)(işlev: (S, S) => S): Küme[S] = d.scan(z)(işlev)
    def taraSoldan[B](z: B)(işlev: (B, T) => B): Küme[B] = d.scanLeft(z)(işlev)
    def taraSağdan[B](z: B)(işlev: (T, B) => B): Küme[B] = d.scanRight(z)(işlev)
    def enUfağıBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.minOption(sıralama)
    def enUfağıBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.minByOption(iş)(karşılaştırma)
    def enİrisiBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.maxOption(sıralama)
    def enİrisiBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.maxByOption(iş)(karşılaştırma)

    // --- selecting, flattening, pairwise operations --------------------
    def seçİşle[B](işlev: PartialFunction[T, B]): Küme[B] = d.collect(işlev)
    def seçİşleİlk[B](işlev: PartialFunction[T, B]): Belki[B] = d.collectFirst(işlev)
    def düzleştir[B](implicit delil: T => YinelenebilirBirKere[B]): Küme[B] = d.flatten(delil)
    def devrik[B](implicit delil: T => Yinelenebilir[B]): Küme[Küme[B]] = d.transpose(delil)
    def ikiliyiAç[A1, A2](implicit delil: T => (A1, A2)): (Küme[A1], Küme[A2]) = d.unzip(delil)
    def ikileHepsini[B, S >: T](öbürü: Yinelenebilir[B], buDolgu: S, oDolgu: B): Küme[(S, B)] =
      d.zipAll(öbürü, buDolgu, oDolgu)

    // --- only for Küme ---------------------------------------------------
    // Küme is IMMUTABLE: these give a new set and do not change the one that exists.
    def ekli(öge: T): Col = d.incl(öge)
    def çıkarılmış(öge: T): Col = d.excl(öge)
    def hepsiÇıkarılmış(ögeler: YinelenebilirBirKere[T]): Col = d.removedAll(ögeler)
    def altKümesiMi(öbürü: Küme[T]): İkil = d.subsetOf(öbürü)
}
}
