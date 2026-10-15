package kojo.tr

/**
 * The "miskin" (lazy) list: the real `LazyList`, as on the desktop (in the 2.12 days it was imitated with
 * `Stream`).
 *
 * The set of methods is kept the same as lite/i18n/tr/miskindizin.scala of desktop Kojo. Those that are
 * specific to the lazy sequence:
 *   önüneEkle(öge)            öge #:: dizi  -- the element is not computed until first accessed
 *   sonunaEkleHepsini(dizi)   lazyAppendedAll -- can append even to an infinite sequence
 *   hepsiniHesapla            force -- computes all the elements (never returns on an infinite sequence!)
 * `ikile` (zip) is already lazy on a lazy sequence; no separate miskinİkile is needed.
 */
trait MiskinDizinYöntemleri extends TemelTürler {
  type MiskinDizin[C] = LazyList[C]

  object MiskinDizin {
    type Belki[T] = Option[T]
    def apply[A](ögeler: A*): MiskinDizin[A] = LazyList.from(ögeler)
    def ekle[A](diziler: Yinelenebilir[A]*): MiskinDizin[A] = LazyList.concat(diziler: _*)
    def sürekli[A](öge: => A): MiskinDizin[A] = LazyList.continually(öge)
    def boş[A]: MiskinDizin[A] = LazyList.empty[A]
    def doldur[A](s: Sayı)(öge: => A): MiskinDizin[A] = LazyList.fill(s)(öge)
    // like Dizi.doldur: element i is işlev(i) -- LazyList.tabulate
    def sıraylaDoldur[A](s: Sayı)(işlev: Sayı => A): MiskinDizin[A] = LazyList.tabulate(s)(işlev)
    def sayalım(başlangıç: Sayı, kaçarKaçar: Sayı = 1): MiskinDizin[Sayı] =
      LazyList.from(başlangıç, kaçarKaçar)
    // turns the given sequence into a lazy one -- LazyList.from(Iterable)
    def diziden[A](dizi: YinelenebilirBirKere[A]): MiskinDizin[A] = LazyList.from(dizi)
    // from the start to the end (the end excluded) -- LazyList.range
    def aralık(baş: Sayı, bitiş: Sayı): MiskinDizin[Sayı] = LazyList.range(baş, bitiş)
    def aralık(baş: Sayı, bitiş: Sayı, adım: Sayı): MiskinDizin[Sayı] = LazyList.range(baş, bitiş, adım)
    def yinele[S](başlangıç: => S)(işlev: S => S): MiskinDizin[S] =
      LazyList.iterate(başlangıç)(işlev)
    // builds a sequence from a state by producing (element, nextState); ends when Hiçbiri is returned -- LazyList.unfold
    def türet[A, S](başlangıç: S)(işlev: S => Belki[(A, S)]): MiskinDizin[A] = LazyList.unfold(başlangıç)(işlev)
  }

  implicit class MiskinDizinMetotları[T](d: MiskinDizin[T]) {
    type Eşlek[A, D] = collection.immutable.Map[A, D]
    type Belki[A] = Option[A]
    type Col = MiskinDizin[T]
    def başı: T = d.head
    def kuyruğu: Col = d.tail
    def önü: Col = d.init
    def sonu: T = d.last
    def başıBelki: Belki[T] = d.headOption
    def sonuBelki: Belki[T] = d.lastOption
    def boyu: Sayı = d.length
    def boşMu: İkil = d.isEmpty
    def doluMu: İkil = d.nonEmpty
    def ele(deneme: T => İkil): Col = d.filter(deneme)
    def eleDeğilse(deneme: T => İkil): Col = d.filterNot(deneme)
    def işle[A](işlev: T => A): MiskinDizin[A] = d.map(işlev)
    def düzİşle[A](işlev: T => MiskinDizin[A]): MiskinDizin[A] = d.flatMap(işlev)
    def seçİşle[A](işlev: PartialFunction[T, A]): MiskinDizin[A] = d.collect(işlev)
    def seçİşleİlk[A](işlev: PartialFunction[T, A]): Belki[A] = d.collectFirst(işlev)
    def düzleştir[A](implicit delil: T => YinelenebilirBirKere[A]): MiskinDizin[A] = d.flatten(delil)
    def devrik[A](implicit delil: T => Yinelenebilir[A]): MiskinDizin[MiskinDizin[A]] = d.transpose(delil)
    def tersİşle[A](işlev: T => A): MiskinDizin[A] = d.reverse.map(işlev)
    def sıralı(implicit ord: Ordering[T]): Col = d.sorted(ord)
    def sırala[A](i: T => A)(implicit ord: Ordering[A]): Col = d.sortBy(i)
    def sırayaSok(önce: (T, T) => İkil): Col = d.sortWith(önce)
    def indirge[B >: T](işlem: (B, B) => B): B = d.reduce(işlem)
    def indirgeSoldan[B >: T](işlem: (B, T) => B): B = d.reduceLeft(işlem)
    def indirgeSağdan[B >: T](işlem: (T, B) => B): B = d.reduceRight(işlem)
    def indirgeBelki[B >: T](işlem: (B, B) => B): Belki[B] = d.reduceOption(işlem)
    def indirgeSoldanBelki[B >: T](işlem: (B, T) => B): Belki[B] = d.reduceLeftOption(işlem)
    def indirgeSağdanBelki[B >: T](işlem: (T, B) => B): Belki[B] = d.reduceRightOption(işlem)
    def katla[B >: T](z: B)(işlev: (B, B) => B): B = d.fold(z)(işlev)
    def soldanKatla[T2](z: T2)(işlev: (T2, T) => T2): T2 = d.foldLeft(z)(işlev)
    def sağdanKatla[T2](z: T2)(işlev: (T, T2) => T2): T2 = d.foldRight(z)(işlev)
    // like katla, but gives all the intermediate totals as a sequence -- scan
    def tara[B >: T](z: B)(işlev: (B, B) => B): MiskinDizin[B] = d.scan(z)(işlev)
    def taraSoldan[B](z: B)(işlev: (B, T) => B): MiskinDizin[B] = d.scanLeft(z)(işlev)
    def taraSağdan[B](z: B)(işlev: (T, B) => B): MiskinDizin[B] = d.scanRight(z)(işlev)
    def topla[T2 >: T](implicit num: Numeric[T2]) = d.sum(num)
    def çarp[T2 >: T](implicit num: Numeric[T2]) = d.product(num)
    def yinelemesiz = d.distinct
    def yinelemesizİşlevle[T2](işlev: T => T2): Col = d.distinctBy(işlev)
    def yazıYap: Yazı = d.mkString
    def yazıYap(ara: Yazı): Yazı = d.mkString(ara)
    def yazıYap(başı: Yazı, ara: Yazı, sonu: Yazı): Yazı = d.mkString(başı, ara, sonu)
    def tersi = d.reverse
    def değiştir[S >: T](yeri: Sayı, değeri: S): MiskinDizin[S] = d.updated(yeri, değeri)
    def herbiriİçin[S](işlev: T => S): Birim = d.foreach(işlev)
    def varMı(deneme: T => İkil): İkil = d.exists(deneme)
    def hepsiDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def hepsiİçinDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def içeriyorMu[S >: T](öge: S): İkil = d.contains(öge)
    def içeriyorMuDilim[T](dilim: Col): İkil = d.containsSlice(dilim)
    def bul(deneme: T => İkil): Belki[T] = d.find(deneme)
    def bulSondan(deneme: T => İkil): Belki[T] = d.findLast(deneme)
    def başındaMı[S >: T](dizi: Yinelenebilir[S]): İkil = d.startsWith(dizi)
    def sonundaMı[S >: T](dizi: Yinelenebilir[S]): İkil = d.endsWith(dizi)
    def karşılıklıMı[A](öbürü: Dizi[A])(deneme: (T, A) => İkil): İkil = d.corresponds(öbürü)(deneme)
    def al(n: Sayı): Col = d.take(n)
    def alDoğruKaldıkça(deneme: T => İkil): Col = d.takeWhile(deneme)
    def alSağdan(n: Sayı): Col = d.takeRight(n)
    def düşür(n: Sayı): Col = d.drop(n)
    def düşürDoğruKaldıkça(deneme: T => İkil): Col = d.dropWhile(deneme)
    def düşürSağdan(n: Sayı): Col = d.dropRight(n)
    def sırası[S >: T](öge: S): Sayı = d.indexOf(öge)
    def sırası[S >: T](öge: S, başlamaNoktası: Sayı): Sayı = d.indexOf(öge, başlamaNoktası)
    def sırasıSondan[S >: T](öge: S): Sayı = d.lastIndexOf(öge)
    def sırasıSondan[S >: T](öge: S, sonNokta: Sayı): Sayı = d.lastIndexOf(öge, sonNokta)
    // the index of the first/last element that satisfies the test -- indexWhere / lastIndexWhere
    def nerede(deneme: T => İkil): Sayı = d.indexWhere(deneme)
    def nerede(deneme: T => İkil, başlamaNoktası: Sayı): Sayı = d.indexWhere(deneme, başlamaNoktası)
    def neredeSondan(deneme: T => İkil): Sayı = d.lastIndexWhere(deneme)
    def sıralar: Range = d.indices

    // adding: önüneEkle does not COMPUTE the element at once but at the first access (öge #:: dizi);
    // sonunaEkleHepsini is lazy too: it can append even to an infinite sequence (lazyAppendedAll)
    def önüneEkle[S >: T](öge: => S): MiskinDizin[S] = LazyList.cons(öge, d)
    def sonunaEkle[S >: T](öge: S): MiskinDizin[S] = d.appended(öge)
    def önüneEkleHepsini[S >: T](öbürü: YinelenebilirBirKere[S]): MiskinDizin[S] = d.prependedAll(öbürü)
    def sonunaEkleHepsini[S >: T](öbürü: => Yinelenebilir[S]): MiskinDizin[S] = d.lazyAppendedAll(öbürü)
    // computes all the elements -- force. It never returns on an infinite sequence!
    def hepsiniHesapla: Col = d.force
    def uzat[S >: T](boy: Sayı, öge: S): MiskinDizin[S] = d.padTo(boy, öge)
    def yama[S >: T](nereden: Sayı, yenisi: YinelenebilirBirKere[S], kaçTane: Sayı): MiskinDizin[S] = d.patch(nereden, yenisi, kaçTane)
    def fark[S >: T](öbürü: Dizi[S]): Col = d.diff(öbürü)
    def kesişim[S >: T](öbürü: Dizi[S]): Col = d.intersect(öbürü)
    def bileşim[S >: T](öbürü: Dizi[S]): MiskinDizin[S] = d.concat(öbürü)

    def dizine: Dizin[T] = d.toList
    def diziye: Dizi[T] = d.toSeq
    def kümeye: Set[T] = d.toSet
    def yöneye: Vector[T] = d.toVector
    def dizime[S >: T](implicit delil: scala.reflect.ClassTag[S]): Array[S] = d.toArray(delil)
    def eşleğe[A, D](implicit delil: T <:< (A, D)): Eşlek[A, D] = d.toMap
    def say(işlev: T => İkil): Sayı = d.count(işlev)

    def dilim(nereden: Sayı, nereye: Sayı) = d.slice(nereden, nereye)
    def böl(deneme: T => İkil): (Col, Col) = d.partition(deneme)
    def bölİşle[A, B](işlev: T => Either[A, B]): (MiskinDizin[A], MiskinDizin[B]) = d.partitionMap(işlev)
    def bölDoğruKaldıkça(deneme: T => İkil): (Col, Col) = d.span(deneme)
    def bölYerinden(yeri: Sayı): (Col, Col) = d.splitAt(yeri)
    def öbekli(boy: Sayı): Yineleyici[Col] = d.grouped(boy)
    def kayarÖbekli(boy: Sayı): Yineleyici[Col] = d.sliding(boy)
    def kayarÖbekli(boy: Sayı, adım: Sayı): Yineleyici[Col] = d.sliding(boy, adım)
    def kombinasyonlar(ögeSayısı: Sayı): Yineleyici[Col] = d.combinations(ögeSayısı)
    def permütasyonlar: Yineleyici[Col] = d.permutations
    def kuyruklar: Yineleyici[Col] = d.tails
    def önler: Yineleyici[Col] = d.inits
    def ikile[S](öbürü: YinelenebilirBirKere[S]) = d.zip(öbürü)
    def ikileSırayla = d.zipWithIndex
    def ikileKonumla = d.zipWithIndex
    def ikileHepsini[A, S >: T](öbürü: Yinelenebilir[A], buDolgu: S, oDolgu: A): MiskinDizin[(S, A)] = d.zipAll(öbürü, buDolgu, oDolgu)
    def ikiliyiAç[A, B](implicit delil: T => (A, B)): (MiskinDizin[A], MiskinDizin[B]) = d.unzip(delil)
    def öbekle[A](iş: (T) => A): Eşlek[A, Col] = d.groupBy(iş)
    def öbekleİşle[A, B](anahtar: T => A)(değer: T => B): Eşlek[A, MiskinDizin[B]] = d.groupMap(anahtar)(değer)
    def öbekleİşleİndirge[A, B](anahtar: T => A)(değer: T => B)(indirge: (B, B) => B): Eşlek[A, B] =
      d.groupMapReduce(anahtar)(değer)(indirge)

    def enUfağı[B >: T](implicit sıralama: math.Ordering[B]): T = d.min(sıralama)
    def enUfağı[B](iş: (T) => B)(implicit karşılaştırma: math.Ordering[B]): T = d.minBy(iş)(karşılaştırma)
    def enİrisi[B >: T](implicit sıralama: math.Ordering[B]): T = d.max(sıralama)
    def enİrisi[B](iş: (T) => B)(implicit karşılaştırma: math.Ordering[B]): T = d.maxBy(iş)(karşılaştırma)
    def enUfağıBelki[B >: T](implicit sıralama: math.Ordering[B]): Belki[T] = d.minOption(sıralama)
    def enUfağıBelki[B](iş: (T) => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.minByOption(iş)(karşılaştırma)
    def enİrisiBelki[B >: T](implicit sıralama: math.Ordering[B]): Belki[T] = d.maxOption(sıralama)
    def enİrisiBelki[B](iş: (T) => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.maxByOption(iş)(karşılaştırma)
  }
}
