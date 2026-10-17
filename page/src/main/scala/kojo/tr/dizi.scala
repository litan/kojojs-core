package kojo.tr

/**
 * Turkish names for Seq -- the collection that children use most.
 *
 * The same structure as on the desktop: SEPARATE implicit classes for `Diz` (collection.Seq) and `Dizi`
 * (immutable Seq) (the counterpart of the pair colSeqYöntemleri/SeqYöntemleri of the desktop). Both apply
 * to a List or a Vector; the compiler picks the more specific one (DiziMetotları).
 */
trait DiziYöntemleri extends TemelTürler with DizimYöntemleri with EşlemYöntemleri {

  object Dizi {
    // List.from; neither toSeq nor Seq.from. In Scala.js varargs come as a WrappedVarArgs, and its apply
    // does NOT check the bounds: Dizi(1)(5) gave undefined instead of throwing an error (on the desktop a
    // SınırDışınaTaşmaHatası). MEASURED in Node:
    //   toSeq/Seq.from -> WrappedVarArgs (does not copy!), printed as "WrappedVarArgs(1, 2, 3)",
    //                     an out-of-bounds access gives an UndefinedBehaviorError (a silent undefined in fullOpt)
    //   List.from      -> a List, printed as "List(1, 2, 3)", an out-of-bounds access gives an
    //                     IndexOutOfBoundsException
    // On the JVM the desktop (tr/dizi.scala) already makes a List with Seq.from; List.from gives both that
    // printing and the check. Do not "simplify" it to Seq.from.
    def apply[B](ögeler: B*): Dizi[B] = List.from(ögeler)
    def unapplySeq[B](dizi: Dizi[B]) = Seq.unapplySeq(dizi)
    def boş[B]: Dizi[B] = Seq.empty[B]
    def doldur[B](n1: Sayı)(f: Sayı => B) = Seq.tabulate(n1)(f)
    def doldur[B](n1: Sayı, n2: Sayı)(f: (Sayı, Sayı) => B) = Seq.tabulate(n1, n2)(f)
    def doldur[B](n1: Sayı, n2: Sayı, n3: Sayı)(f: (Sayı, Sayı, Sayı) => B) = Seq.tabulate(n1, n2, n3)(f)
  }

  object Diz {
    // The same reason and the same measurement as Dizi.apply: the bounds check plus the same printing as
    // the desktop. (A List is a collection.Seq.)
    def apply[B](ögeler: B*): Diz[B] = List.from(ögeler)
    def boş[B]: Diz[B] = collection.Seq.empty[B]
    def unapplySeq[B](dizi: Diz[B]) = collection.Seq.unapplySeq(dizi)
    def doldur[B](n1: Sayı)(f: Sayı => B) = Seq.tabulate(n1)(f)
  }

  // The counterpart of colSeqYöntemleri of the desktop: collection.Seq (Diz) -- the mutable Seqs too.
  // For immutable Seqs the compiler picks the more specific DiziMetotları below.
  implicit class DizMetotları[T](d: Diz[T]) {
    type Belki[B] = Option[B]
    type Col = Diz[T]
    type C2[B] = Diz[B]
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
    def işle[A](işlev: T => A): C2[A] = d.map(işlev)
    def düzİşle[A](işlev: T => C2[A]): C2[A] = d.flatMap(işlev)
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
    def yazıYap(baş: Yazı, ara: Yazı, sonu: Yazı): Yazı = d.mkString(baş, ara, sonu)
    def tersi: Col = d.reverse
    def değiştir[S >: T](yeri: Sayı, değeri: S): C2[S] = d.updated(yeri, değeri)
    def herbiriİçin[S](işlev: T => S): Birim = d.foreach(işlev)
    def varMı(deneme: T => İkil): İkil = d.exists(deneme)
    def hepsiDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def hepsiİçinDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def içeriyorMu[S >: T](öge: S): İkil = d.contains(öge)
    def içeriyorMuDilim(dilim: Diz[T]): İkil = d.containsSlice(dilim)
    def al(n: Sayı): Col = d.take(n)
    def alDoğruKaldıkça(deneme: T => İkil): Col = d.takeWhile(deneme)
    def alSağdan(n: Sayı): Col = d.takeRight(n)
    def düşür(n: Sayı): Col = d.drop(n)
    def düşürDoğruKaldıkça(deneme: T => İkil): Col = d.dropWhile(deneme)
    def düşürSağdan(n: Sayı): Col = d.dropRight(n)
    def sırası[S >: T](öge: S): Sayı = d.indexOf(öge)
    def sırasıSondan[S >: T](öge: S): Sayı = d.lastIndexOf(öge)

    // the way into Yineleyici (the glossary said iterator -> yineleyici, which was not clear)
    def yineleyici: Yineleyici[T] = d.iterator
    def dizine: Dizin[T] = d.toList
    def diziye: Dizi[T] = d.toSeq
    def kümeye: Set[T] = d.toSet
    def yöneye: Vector[T] = d.toVector
    // dizime/eşleme are written separately in every wrapper on the desktop; here they go on Diz/Dizi
    // and the sub types (Dizin, Yöney, Aralık, Yığın, Kuyruk, EsnekYazı, MiskinDizin) get them by
    // INHERITANCE -- Scala picks the most specific implicit class, so there is no ambiguity.
    def dizime[S >: T](implicit delil: scala.reflect.ClassTag[S]): Dizim[S] = new Dizim(d.toArray(delil))
    def eşleme[A, D](implicit delil: T <:< (A, D)): Eşlem[A, D] = Eşlem.değişmezden(d.toMap)
    def eşleğe[A, D](implicit delil: T <:< (A, D)): Eşlek[A, D] = d.toMap
    def say(işlev: T => İkil): Sayı = d.count(işlev)

    def dilim(nereden: Sayı, nereye: Sayı): Col = d.slice(nereden, nereye)
    def ikile[S](öbürü: Yinelenebilir[S]) = d.zip(öbürü)
    def ikileSırayla = d.zipWithIndex
    def ikileKonumla = d.zipWithIndex
    def öbekle[A](iş: T => A): Eşlek[A, Col] = d.groupBy(iş)
    def öbekli(boy: Sayı): Yineleyici[Col] = d.grouped(boy)
    def böl(deneme: T => İkil): (Col, Col) = d.partition(deneme)

    def enUfağı[B >: T](implicit sıralama: Ordering[B]): T = d.min(sıralama)
    def enUfağıİşlevle[B](iş: T => B)(implicit k: Ordering[B]): T = d.minBy(iş)(k)
    def enİrisi[B >: T](implicit sıralama: Ordering[B]): T = d.max(sıralama)
    def enİrisiİşlevle[B](iş: T => B)(implicit k: Ordering[B]): T = d.maxBy(iş)(k)
  
    // --- ends, searching ------------------------------------------
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

    // --- splitting, grouping ----------------------------------------
    def bölİşle[A1, A2](işlev: T => Either[A1, A2]): (C2[A1], C2[A2]) = d.partitionMap(işlev)
    def bölDoğruKaldıkça(deneme: T => İkil): (Col, Col) = d.span(deneme)
    def bölYerinden(yeri: Sayı): (Col, Col) = d.splitAt(yeri)
    def kayarÖbekli(boy: Sayı): Yineleyici[Col] = d.sliding(boy)
    def kayarÖbekli(boy: Sayı, adım: Sayı): Yineleyici[Col] = d.sliding(boy, adım)
    def öbekleİşle[K, B](anahtar: T => K)(değer: T => B): Eşlek[K, C2[B]] = d.groupMap(anahtar)(değer)
    def öbekleİşleİndirge[K, B](anahtar: T => K)(değer: T => B)(indirge: (B, B) => B): Eşlek[K, B] =
      d.groupMapReduce(anahtar)(değer)(indirge)
    def kombinasyonlar(ögeSayısı: Sayı): Yineleyici[Col] = d.combinations(ögeSayısı)
    def permütasyonlar: Yineleyici[Col] = d.permutations
    def kuyruklar: Yineleyici[Col] = d.tails
    def önler: Yineleyici[Col] = d.inits

    // --- folding, reducing, scanning --------------------------------
    def katla[S >: T](z: S)(işlev: (S, S) => S): S = d.fold(z)(işlev)
    def indirgeSoldan[S >: T](işlem: (S, T) => S): S = d.reduceLeft(işlem)
    def indirgeSağdan[S >: T](işlem: (T, S) => S): S = d.reduceRight(işlem)
    def indirgeBelki[S >: T](işlem: (S, S) => S): Belki[S] = d.reduceOption(işlem)
    def indirgeSoldanBelki[S >: T](işlem: (S, T) => S): Belki[S] = d.reduceLeftOption(işlem)
    def indirgeSağdanBelki[S >: T](işlem: (T, S) => S): Belki[S] = d.reduceRightOption(işlem)
    // tara: like katla, but gives ALL the intermediate results
    def tara[S >: T](z: S)(işlev: (S, S) => S): C2[S] = d.scan(z)(işlev)
    def taraSoldan[B](z: B)(işlev: (B, T) => B): C2[B] = d.scanLeft(z)(işlev)
    def taraSağdan[B](z: B)(işlev: (T, B) => B): C2[B] = d.scanRight(z)(işlev)
    def enUfağıBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.minOption(sıralama)
    def enUfağıBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.minByOption(iş)(karşılaştırma)
    def enİrisiBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.maxOption(sıralama)
    def enİrisiBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.maxByOption(iş)(karşılaştırma)

    // --- adding, removing -------------------------------------------
    def sonunaEkle[S >: T](öge: S): C2[S] = d.appended(öge)
    def önüneEkle[S >: T](öge: S): C2[S] = d.prepended(öge)
    def sonunaEkleHepsini[S >: T](öbürü: YinelenebilirBirKere[S]): C2[S] = d.appendedAll(öbürü)
    def önüneEkleHepsini[S >: T](öbürü: YinelenebilirBirKere[S]): C2[S] = d.prependedAll(öbürü)
    def uzat[S >: T](boy: Sayı, öge: S): C2[S] = d.padTo(boy, öge)
    def yama[S >: T](nereden: Sayı, yenisi: YinelenebilirBirKere[S], kaçTane: Sayı): C2[S] =
      d.patch(nereden, yenisi, kaçTane)
    def fark[S >: T](öbürü: Diz[S]): Col = d.diff(öbürü)
    def kesişim[S >: T](öbürü: Diz[S]): Col = d.intersect(öbürü)
    def bileşim[S >: T](öbürü: Diz[S]): C2[S] = d.concat(öbürü)

    // --- selecting, flattening, pairwise operations -----------------
    def seçİşle[B](işlev: PartialFunction[T, B]): C2[B] = d.collect(işlev)
    def seçİşleİlk[B](işlev: PartialFunction[T, B]): Belki[B] = d.collectFirst(işlev)
    def düzleştir[B](implicit delil: T => YinelenebilirBirKere[B]): C2[B] = d.flatten(delil)
    def devrik[B](implicit delil: T => Yinelenebilir[B]): C2[C2[B]] = d.transpose(delil)
    def ikiliyiAç[A1, A2](implicit delil: T => (A1, A2)): (C2[A1], C2[A2]) = d.unzip(delil)
    def ikileHepsini[B, S >: T](öbürü: Yinelenebilir[B], buDolgu: S, oDolgu: B): C2[(S, B)] =
      d.zipAll(öbürü, buDolgu, oDolgu)
    def tersİşle[B](işlev: T => B): C2[B] = d.reverse.map(işlev)
}

  implicit class DiziMetotları[T](d: Dizi[T]) {
    type Belki[B] = Option[B]
    type Col = Dizi[T]
    type C2[B] = Dizi[B]
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
    def işle[A](işlev: T => A): C2[A] = d.map(işlev)
    def düzİşle[A](işlev: T => C2[A]): C2[A] = d.flatMap(işlev)
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
    def yazıYap(baş: Yazı, ara: Yazı, sonu: Yazı): Yazı = d.mkString(baş, ara, sonu)
    def tersi: Col = d.reverse
    def değiştir[S >: T](yeri: Sayı, değeri: S): C2[S] = d.updated(yeri, değeri)
    def herbiriİçin[S](işlev: T => S): Birim = d.foreach(işlev)
    def varMı(deneme: T => İkil): İkil = d.exists(deneme)
    def hepsiDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def hepsiİçinDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def içeriyorMu[S >: T](öge: S): İkil = d.contains(öge)
    def içeriyorMuDilim(dilim: Dizi[T]): İkil = d.containsSlice(dilim)
    def al(n: Sayı): Col = d.take(n)
    def alDoğruKaldıkça(deneme: T => İkil): Col = d.takeWhile(deneme)
    def alSağdan(n: Sayı): Col = d.takeRight(n)
    def düşür(n: Sayı): Col = d.drop(n)
    def düşürDoğruKaldıkça(deneme: T => İkil): Col = d.dropWhile(deneme)
    def düşürSağdan(n: Sayı): Col = d.dropRight(n)
    def sırası[S >: T](öge: S): Sayı = d.indexOf(öge)
    def sırasıSondan[S >: T](öge: S): Sayı = d.lastIndexOf(öge)

    def dizine: Dizin[T] = d.toList
    def diziye: Dizi[T] = d.toSeq
    def kümeye: Set[T] = d.toSet
    def yöneye: Vector[T] = d.toVector
    // dizime/eşleme are written separately in every wrapper on the desktop; here they go on Diz/Dizi
    // and the sub types (Dizin, Yöney, Aralık, Yığın, Kuyruk, EsnekYazı, MiskinDizin) get them by
    // INHERITANCE -- Scala picks the most specific implicit class, so there is no ambiguity.
    def dizime[S >: T](implicit delil: scala.reflect.ClassTag[S]): Dizim[S] = new Dizim(d.toArray(delil))
    def eşleme[A, D](implicit delil: T <:< (A, D)): Eşlem[A, D] = Eşlem.değişmezden(d.toMap)
    def eşleğe[A, D](implicit delil: T <:< (A, D)): Eşlek[A, D] = d.toMap
    def say(işlev: T => İkil): Sayı = d.count(işlev)

    def dilim(nereden: Sayı, nereye: Sayı): Col = d.slice(nereden, nereye)
    def ikile[S](öbürü: Yinelenebilir[S]) = d.zip(öbürü)
    def ikileSırayla = d.zipWithIndex
    def ikileKonumla = d.zipWithIndex
    def öbekle[A](iş: T => A): Eşlek[A, Col] = d.groupBy(iş)
    def öbekli(boy: Sayı): Yineleyici[Col] = d.grouped(boy)
    def böl(deneme: T => İkil): (Col, Col) = d.partition(deneme)

    def enUfağı[B >: T](implicit sıralama: Ordering[B]): T = d.min(sıralama)
    def enUfağıİşlevle[B](iş: T => B)(implicit k: Ordering[B]): T = d.minBy(iş)(k)
    def enİrisi[B >: T](implicit sıralama: Ordering[B]): T = d.max(sıralama)
    def enİrisiİşlevle[B](iş: T => B)(implicit k: Ordering[B]): T = d.maxBy(iş)(k)
  
    // --- ends, searching ------------------------------------------
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

    // --- splitting, grouping ----------------------------------------
    def bölİşle[A1, A2](işlev: T => Either[A1, A2]): (C2[A1], C2[A2]) = d.partitionMap(işlev)
    def bölDoğruKaldıkça(deneme: T => İkil): (Col, Col) = d.span(deneme)
    def bölYerinden(yeri: Sayı): (Col, Col) = d.splitAt(yeri)
    def kayarÖbekli(boy: Sayı): Yineleyici[Col] = d.sliding(boy)
    def kayarÖbekli(boy: Sayı, adım: Sayı): Yineleyici[Col] = d.sliding(boy, adım)
    def öbekleİşle[K, B](anahtar: T => K)(değer: T => B): Eşlek[K, C2[B]] = d.groupMap(anahtar)(değer)
    def öbekleİşleİndirge[K, B](anahtar: T => K)(değer: T => B)(indirge: (B, B) => B): Eşlek[K, B] =
      d.groupMapReduce(anahtar)(değer)(indirge)
    def kombinasyonlar(ögeSayısı: Sayı): Yineleyici[Col] = d.combinations(ögeSayısı)
    def permütasyonlar: Yineleyici[Col] = d.permutations
    def kuyruklar: Yineleyici[Col] = d.tails
    def önler: Yineleyici[Col] = d.inits

    // --- folding, reducing, scanning --------------------------------
    def katla[S >: T](z: S)(işlev: (S, S) => S): S = d.fold(z)(işlev)
    def indirgeSoldan[S >: T](işlem: (S, T) => S): S = d.reduceLeft(işlem)
    def indirgeSağdan[S >: T](işlem: (T, S) => S): S = d.reduceRight(işlem)
    def indirgeBelki[S >: T](işlem: (S, S) => S): Belki[S] = d.reduceOption(işlem)
    def indirgeSoldanBelki[S >: T](işlem: (S, T) => S): Belki[S] = d.reduceLeftOption(işlem)
    def indirgeSağdanBelki[S >: T](işlem: (T, S) => S): Belki[S] = d.reduceRightOption(işlem)
    // tara: like katla, but gives ALL the intermediate results
    def tara[S >: T](z: S)(işlev: (S, S) => S): C2[S] = d.scan(z)(işlev)
    def taraSoldan[B](z: B)(işlev: (B, T) => B): C2[B] = d.scanLeft(z)(işlev)
    def taraSağdan[B](z: B)(işlev: (T, B) => B): C2[B] = d.scanRight(z)(işlev)
    def enUfağıBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.minOption(sıralama)
    def enUfağıBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.minByOption(iş)(karşılaştırma)
    def enİrisiBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.maxOption(sıralama)
    def enİrisiBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.maxByOption(iş)(karşılaştırma)

    // --- adding, removing -------------------------------------------
    def sonunaEkle[S >: T](öge: S): C2[S] = d.appended(öge)
    def önüneEkle[S >: T](öge: S): C2[S] = d.prepended(öge)
    def sonunaEkleHepsini[S >: T](öbürü: YinelenebilirBirKere[S]): C2[S] = d.appendedAll(öbürü)
    def önüneEkleHepsini[S >: T](öbürü: YinelenebilirBirKere[S]): C2[S] = d.prependedAll(öbürü)
    def uzat[S >: T](boy: Sayı, öge: S): C2[S] = d.padTo(boy, öge)
    def yama[S >: T](nereden: Sayı, yenisi: YinelenebilirBirKere[S], kaçTane: Sayı): C2[S] =
      d.patch(nereden, yenisi, kaçTane)
    def fark[S >: T](öbürü: Diz[S]): Col = d.diff(öbürü)
    def kesişim[S >: T](öbürü: Diz[S]): Col = d.intersect(öbürü)
    def bileşim[S >: T](öbürü: Diz[S]): C2[S] = d.concat(öbürü)

    // --- selecting, flattening, pairwise operations -----------------
    def seçİşle[B](işlev: PartialFunction[T, B]): C2[B] = d.collect(işlev)
    def seçİşleİlk[B](işlev: PartialFunction[T, B]): Belki[B] = d.collectFirst(işlev)
    def düzleştir[B](implicit delil: T => YinelenebilirBirKere[B]): C2[B] = d.flatten(delil)
    def devrik[B](implicit delil: T => Yinelenebilir[B]): C2[C2[B]] = d.transpose(delil)
    def ikiliyiAç[A1, A2](implicit delil: T => (A1, A2)): (C2[A1], C2[A2]) = d.unzip(delil)
    def ikileHepsini[B, S >: T](öbürü: Yinelenebilir[B], buDolgu: S, oDolgu: B): C2[(S, B)] =
      d.zipAll(öbürü, buDolgu, oDolgu)
    def tersİşle[B](işlev: T => B): C2[B] = d.reverse.map(işlev)
}

  /**
   * Yineleyici (Iterator) -- the thing that walks through the elements ONCE, from the start to the end.
   *
   * öbekli, kayarÖbekli, kombinasyonlar, permütasyonlar, kuyruklar, önler... all of them give a
   * Yineleyici. There used to be only five methods here and a learner fell into English (toList) right
   * there. Now it is the same set as on the desktop.
   *
   * IMPORTANT: a Yineleyici can be used only once; most of the methods here CONSUME it. If you need to
   * walk through it twice, duplicate it with ikizYap or first turn it into a collection with dizine/diziye.
   */
  implicit class YineleyiciMetotları[T](d: Yineleyici[T]) {
    type Belki[B] = Option[B]
    type Eşlek[A, D] = collection.immutable.Map[A, D]

    // --- core: walking by hand --------------------------------------
    def dahaVarMı: İkil = d.hasNext
    def sıradaki: T = d.next()

    // --- filtering and processing (the iterator is CONSUMED) --------
    def ele(deneme: T => İkil): Yineleyici[T] = d.filter(deneme)
    def eleDeğilse(deneme: T => İkil): Yineleyici[T] = d.filterNot(deneme)
    def işle[B](işlev: T => B): Yineleyici[B] = d.map(işlev)
    def düzİşle[B](işlev: T => YinelenebilirBirKere[B]): Yineleyici[B] = d.flatMap(işlev)
    def düzleştir[B](implicit delil: T => YinelenebilirBirKere[B]): Yineleyici[B] = d.flatten(delil)
    def seçİşle[B](işlev: PartialFunction[T, B]): Yineleyici[B] = d.collect(işlev)
    def seçİşleİlk[B](işlev: PartialFunction[T, B]): Belki[B] = d.collectFirst(işlev)
    def herbiriİçin[B](işlev: T => B): Birim = d.foreach(işlev)

    // --- searching --------------------------------------------------
    def bul(deneme: T => İkil): Belki[T] = d.find(deneme)
    def varMı(deneme: T => İkil): İkil = d.exists(deneme)
    def hepsiDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def hepsiİçinDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def say(deneme: T => İkil): Sayı = d.count(deneme)
    def içeriyorMu(öge: Her): İkil = d.contains(öge)
    def sırası[S >: T](öge: S): Sayı = d.indexOf(öge)
    def nerede(deneme: T => İkil): Sayı = d.indexWhere(deneme)
    def karşılıklıMı[S](öbürü: YinelenebilirBirKere[S])(deneme: (T, S) => İkil): İkil =
      d.corresponds(öbürü)(deneme)
    def gösterdikleriAynıMı[S >: T](öbürü: YinelenebilirBirKere[S]): İkil = d.sameElements(öbürü)

    // --- cutting and shaping ----------------------------------------
    def al(kaçTane: Sayı): Yineleyici[T] = d.take(kaçTane)
    def alDoğruKaldıkça(deneme: T => İkil): Yineleyici[T] = d.takeWhile(deneme)
    def düşür(kaçTane: Sayı): Yineleyici[T] = d.drop(kaçTane)
    def düşürDoğruKaldıkça(deneme: T => İkil): Yineleyici[T] = d.dropWhile(deneme)
    def dilim(nereden: Sayı, nereye: Sayı): Yineleyici[T] = d.slice(nereden, nereye)
    def böl(deneme: T => İkil): (Yineleyici[T], Yineleyici[T]) = d.partition(deneme)
    def bölDoğruKaldıkça(deneme: T => İkil): (Yineleyici[T], Yineleyici[T]) = d.span(deneme)
    def öbekli(boy: Sayı): Yineleyici[Dizi[T]] = d.grouped(boy).map(_.toSeq)
    def kayarÖbekli(boy: Sayı): Yineleyici[Dizi[T]] = d.sliding(boy).map(_.toSeq)
    def kayarÖbekli(boy: Sayı, adım: Sayı): Yineleyici[Dizi[T]] = d.sliding(boy, adım).map(_.toSeq)
    def yinelemesiz: Yineleyici[T] = d.distinct
    def yinelemesizİşlevle[B](işlev: T => B): Yineleyici[T] = d.distinctBy(işlev)

    // --- combining --------------------------------------------------
    def bileşim[S >: T](öbürü: YinelenebilirBirKere[S]): Yineleyici[S] = d.concat(öbürü)
    def uzat[S >: T](boy: Sayı, öge: S): Yineleyici[S] = d.padTo(boy, öge)
    def yama[S >: T](nereden: Sayı, yenisi: Yineleyici[S], kaçTane: Sayı): Yineleyici[S] =
      d.patch(nereden, yenisi, kaçTane)
    def ikile[S](öbürü: YinelenebilirBirKere[S]): Yineleyici[(T, S)] = d.zip(öbürü)
    def ikileHepsini[S >: T, B](öbürü: Yineleyici[B], buDolgu: S, oDolgu: B): Yineleyici[(S, B)] =
      d.zipAll(öbürü, buDolgu, oDolgu)
    def ikileSırayla: Yineleyici[(T, Sayı)] = d.zipWithIndex

    // --- folding and reducing (consumes) ----------------------------
    def indirge[S >: T](işlem: (S, S) => S): S = d.reduce(işlem)
    def indirgeBelki[S >: T](işlem: (S, S) => S): Belki[S] = d.reduceOption(işlem)
    def indirgeSoldan[S >: T](işlem: (S, T) => S): S = d.reduceLeft(işlem)
    def indirgeSağdan[S >: T](işlem: (T, S) => S): S = d.reduceRight(işlem)
    def katla[S >: T](başlangıç: S)(işlem: (S, S) => S): S = d.fold(başlangıç)(işlem)
    def soldanKatla[B](başlangıç: B)(işlem: (B, T) => B): B = d.foldLeft(başlangıç)(işlem)
    def sağdanKatla[B](başlangıç: B)(işlem: (T, B) => B): B = d.foldRight(başlangıç)(işlem)
    def taraSoldan[B](başlangıç: B)(işlem: (B, T) => B): Yineleyici[B] = d.scanLeft(başlangıç)(işlem)
    def topla[S >: T](implicit sayısal: Numeric[S]): S = d.sum(sayısal)
    def çarp[S >: T](implicit sayısal: Numeric[S]): S = d.product(sayısal)
    def enUfağı[S >: T](implicit sıralama: Ordering[S]): T = d.min(sıralama)
    def enİrisi[S >: T](implicit sıralama: Ordering[S]): T = d.max(sıralama)
    def enUfağıBelki[S >: T](implicit sıralama: Ordering[S]): Belki[T] = d.minOption(sıralama)
    def enİrisiBelki[S >: T](implicit sıralama: Ordering[S]): Belki[T] = d.maxOption(sıralama)
    def enUfağı[B](iş: T => B)(implicit karşılaştırma: Ordering[B]): T = d.minBy(iş)(karşılaştırma)
    def enİrisi[B](iş: T => B)(implicit karşılaştırma: Ordering[B]): T = d.maxBy(iş)(karşılaştırma)
    def enUfağıBelki[B](iş: T => B)(implicit karşılaştırma: Ordering[B]): Belki[T] = d.minByOption(iş)(karşılaştırma)
    def enİrisiBelki[B](iş: T => B)(implicit karşılaştırma: Ordering[B]): Belki[T] = d.maxByOption(iş)(karşılaştırma)
    def indirgeSoldanBelki[S >: T](işlem: (S, T) => S): Belki[S] = d.reduceLeftOption(işlem)
    def indirgeSağdanBelki[S >: T](işlem: (T, S) => S): Belki[S] = d.reduceRightOption(işlem)
    def taraSağdan[B](başlangıç: B)(işlem: (T, B) => B): Yineleyici[B] = d.scanRight(başlangıç)(işlem)
    def bölYerinden(yeri: Sayı): (Yineleyici[T], Yineleyici[T]) = d.splitAt(yeri)
    // the form of sıradaki that does not throw (the same pattern as başıBelki/sonuBelki)
    def sıradakiBelki: Belki[T] = d.nextOption()

    // --- size and emptiness -----------------------------------------
    // CAREFUL: boyu CONSUMES the iterator.
    def boyu: Sayı = d.length
    def boşMu: İkil = d.isEmpty
    def doluMu: İkil = d.nonEmpty

    // --- duplicating and looking ahead ------------------------------
    def ikizYap: (Yineleyici[T], Yineleyici[T]) = d.duplicate
    def bellekli: collection.BufferedIterator[T] = d.buffered

    // --- converting to a collection ---------------------------------
    def dizine: Dizin[T] = d.toList
    def diziye: Dizi[T] = d.toSeq
    def kümeye: Set[T] = d.toSet
    def yöneye: Vector[T] = d.toVector
    def dizime[S >: T](implicit delil: scala.reflect.ClassTag[S]): Dizim[S] = new Dizim(d.toArray(delil))
    def eşleğe[A, D](implicit delil: T <:< (A, D)): Eşlek[A, D] = d.toMap
    def yazıYap: Yazı = d.mkString
    def yazıYap(ara: Yazı): Yazı = d.mkString(ara)
    def yazıYap(başı: Yazı, ara: Yazı, sonu: Yazı): Yazı = d.mkString(başı, ara, sonu)
  }
}
