package kojo.tr

import scala.collection.mutable.{Stack, Queue, PriorityQueue, ArrayDeque}

/**
 * Yığın (Stack), Kuyruk (Queue) and Öncelik Sırası (PriorityQueue).
 */
trait KuyrukYöntemleri extends TemelTürler with EşlemYöntemleri with DizimYöntemleri {
  type Yığın[T] = Stack[T]
  type Kuyruk[T] = Queue[T]
  type ÖncelikSırası[T] = PriorityQueue[T]
  /**
   * The same type as `ÖncelikSırası` -- for those who prefer the same word as the "kuyruk" family.
   *
   * A type alias is NOT enough by itself: to be able to write `ÖncelikKuyruğu.boş[Sayı]` the companion
   * object needs a name too, or it gives a "not found: value" error. The `val` below provides it.
   */
  type ÖncelikKuyruğu[T] = PriorityQueue[T]
  /** The `deque` of C++: a queue that you can add to and remove from at both ends. */
  type İkiUçluKuyruk[T] = ArrayDeque[T]

  object Yığın {
    // Yığın(1, 2, 3) = "push 1, push 2, push 3", so 3 is on the TOP.
    // Scala's own Stack(1, 2, 3) puts 1 on the top (it is shown from the top); desktop Koco has used the
    // push order from the start and it is closer to a learner's intuition. The two repositories were made
    // equal in this behavior.
    def apply[T](elems: T*): Yığın[T] = { val y = Stack.empty[T]; y.pushAll(elems); y }
    def boş[T]: Yığın[T] = Stack.empty[T]
    // a copy of another stack, in the same order (the top is still on the top)
    def doldur[T](y2: Yığın[T]): Yığın[T] = Stack.from(y2)
  }

  object Kuyruk {
    def apply[T](elems: T*): Kuyruk[T] = Queue(elems: _*)
    def boş[T]: Kuyruk[T] = Queue.empty[T]
  }

  object ÖncelikSırası {
    def apply[T](elems: T*)(implicit sıralama: Ordering[T]): ÖncelikSırası[T] =
      PriorityQueue(elems: _*)(sıralama)
    def boş[T](implicit sıralama: Ordering[T]): ÖncelikSırası[T] = PriorityQueue.empty[T](sıralama)
  }

  implicit class YığınMetotları[T](d: Yığın[T]) {
    def it(öge: T): Yığın[T] = d.push(öge)
    def çek(): T = d.pop()
    def koy(öge: T): Yığın[T] = d.push(öge) // the same as it (the name in the booklet)
    def al(): T = d.pop()          // the same as çek (the name in the booklet)
    def tepesi: T = d.top
    def boyu: Sayı = d.size
    def tane: Sayı = d.size // the same as boyu (the name in the booklet)
    def boşMu: İkil = d.isEmpty
    def doluMu: İkil = d.nonEmpty
    def sil(): Birim = d.clear()
    def dizine: Dizin[T] = d.toList
    def herbiriİçin[S](işlev: T => S): Birim = d.foreach(işlev)
    // in step with the names on the desktop (see kojo/lite/i18n/tr/kuyruk.scala)
    def tepe: T = d.top            // the same as tepesi
    def koyHepsini(dizi: YinelenebilirBirKere[T]): Yığın[T] = d.pushAll(dizi)
    def itHepsini(dizi: YinelenebilirBirKere[T]): Yığın[T] = d.pushAll(dizi)
    def dizi: Dizi[T] = d.toSeq
    type Col = Yığın[T]
    type Belki[B] = Option[B]

    // --- emptying a stack in bulk -----------------------------------
    // The naming rule: START WITH THE ACTION -- çekHepsini, ekleAraya, çıkarSondan.
    def çekHepsini: Diz[T] = d.popAll()                    // from the top to the bottom
    def alHepsini: Dizi[T] = d.removeAll()                 // the same as çekHepsini
    def alHepsiniTersten: Dizi[T] = d.removeAllReverse()   // from the bottom to the top
    def çekDoğruKaldıkça(deneme: T => İkil): Diz[T] = d.popWhile(deneme)
    def çekBelki: Belki[T] = d.removeHeadOption()
    // In a stack the "last" = the BOTTOM. çıkarSondan takes the one at the bottom, not the one on the top.
    def çıkarSondan(): T = d.removeLast()
    def çıkarSondanBelki: Belki[T] = d.removeLastOption()
    def sondanÇıkar(): T = d.removeLast()                  // an alias of çıkarSondan
    def sondanÇıkarBelki: Belki[T] = d.removeLastOption()  // an alias of çıkarSondanBelki
    def çıkarSondanDoğruKaldıkça(deneme: T => İkil): Dizi[T] = d.removeLastWhile(deneme)
    def çıkarİlkUyanı(deneme: T => İkil): Belki[T] = d.removeFirst(deneme)

    // --- the ones that change in place ------------------------------
    def eleYerinde(deneme: T => İkil): Col = { d.filterInPlace(deneme); d }
    def işleYerinde(işlev: T => T): Col = { d.mapInPlace(işlev); d }
    def düzİşleYerinde(işlev: T => YinelenebilirBirKere[T]): Col = { d.flatMapInPlace(işlev); d }
    def sıralıYerinde(implicit sıralama: Ordering[T]): Col = { d.sortInPlace()(sıralama); d }
    def sıralaYerinde[B](iş: T => B)(implicit sıralama: Ordering[B]): Col = { d.sortInPlaceBy(iş)(sıralama); d }
    def sırayaSokYerinde(önce: (T, T) => İkil): Col = { d.sortInPlaceWith(önce); d }
    def alYerinde(kaçTane: Sayı): Col = { d.takeInPlace(kaçTane); d }
    def alSağdanYerinde(kaçTane: Sayı): Col = { d.takeRightInPlace(kaçTane); d }
    def alDoğruKaldıkçaYerinde(deneme: T => İkil): Col = { d.takeWhileInPlace(deneme); d }
    def düşürYerinde(kaçTane: Sayı): Col = { d.dropInPlace(kaçTane); d }
    def düşürSağdanYerinde(kaçTane: Sayı): Col = { d.dropRightInPlace(kaçTane); d }
    def düşürDoğruKaldıkçaYerinde(deneme: T => İkil): Col = { d.dropWhileInPlace(deneme); d }
    def dilimYerinde(nereden: Sayı, nereye: Sayı): Col = { d.sliceInPlace(nereden, nereye); d }
    def uzatYerinde(boy: Sayı, öge: T): Col = { d.padToInPlace(boy, öge); d }
    def yamaYerinde(nereden: Sayı, yenisi: YinelenebilirBirKere[T], kaçTane: Sayı): Col = {
      d.patchInPlace(nereden, yenisi, kaçTane); d
    }

    // --- access by position (0 = the TOP) ---------------------------
    def güncelle(yeri: Sayı, öge: T): Birim = d.update(yeri, öge)
    def ekleAraya(yeri: Sayı, öge: T): Birim = d.insert(yeri, öge)
    def ekleArayaHepsini(yeri: Sayı, ögeler: YinelenebilirBirKere[T]): Birim = d.insertAll(yeri, ögeler)
    def çıkar(yeri: Sayı): T = d.remove(yeri)
    def ekleHepsini(ögeler: YinelenebilirBirKere[T]): Col = { d.addAll(ögeler); d }
    def çıkarHepsini(ögeler: YinelenebilirBirKere[T]): Col = { d.subtractAll(ögeler); d }
    def dizime[S >: T](implicit delil: scala.reflect.ClassTag[S]): Dizim[S] = new Dizim(d.toArray(delil))

  }

  implicit class KuyrukMetotları[T](d: Kuyruk[T]) {
    type Col = Kuyruk[T]
    type Eşlek[K, D] = collection.immutable.Map[K, D]
    type Belki[B] = Option[B]
    def ekle(öge: T): Kuyruk[T] = d += öge
    // SHARED NAME: `koy` means "put an element in" in all three structures (push in Yığın, enqueue here).
    // No shared name was given for removing: the `al(n)` here is already take(n) -- it removes nothing, it
    // gives a copy. Adding `al()` would put the two on the same name. The name of removing is `baştanAl`:
    // it says which end it takes from.
    def koy(öge: T): Kuyruk[T] = d += öge
    def çıkar(): T = d.dequeue()
    def baştanAl(): T = d.dequeue() // the same as çıkar (the name in the booklet)
    def başı: T = d.head
    def boyu: Sayı = d.size
    def tane: Sayı = d.size // the same as boyu (the name in the booklet)
    def boşMu: İkil = d.isEmpty
    def doluMu: İkil = d.nonEmpty
    def sil(): Birim = d.clear()
    def dizine: Dizin[T] = d.toList
    def herbiriİçin[S](işlev: T => S): Birim = d.foreach(işlev)
  
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
    def böl(deneme: T => İkil): (Kuyruk[T], Kuyruk[T]) = d.partition(deneme)
    def bölİşle[A1, A2](işlev: T => Either[A1, A2]): (Kuyruk[A1], Kuyruk[A2]) = d.partitionMap(işlev)
    def bölDoğruKaldıkça(deneme: T => İkil): (Kuyruk[T], Kuyruk[T]) = d.span(deneme)
    def bölYerinden(yeri: Sayı): (Kuyruk[T], Kuyruk[T]) = d.splitAt(yeri)
    def öbekli(boy: Sayı): Yineleyici[Kuyruk[T]] = d.grouped(boy)
    def kayarÖbekli(boy: Sayı): Yineleyici[Kuyruk[T]] = d.sliding(boy)
    def kayarÖbekli(boy: Sayı, adım: Sayı): Yineleyici[Kuyruk[T]] = d.sliding(boy, adım)
    def öbekleİşle[K, B](anahtar: T => K)(değer: T => B): Eşlek[K, Kuyruk[B]] = d.groupMap(anahtar)(değer)
    def öbekleİşleİndirge[K, B](anahtar: T => K)(değer: T => B)(indirge: (B, B) => B): Eşlek[K, B] =
      d.groupMapReduce(anahtar)(değer)(indirge)
    def kombinasyonlar(ögeSayısı: Sayı): Yineleyici[Kuyruk[T]] = d.combinations(ögeSayısı)
    def permütasyonlar: Yineleyici[Kuyruk[T]] = d.permutations
    def kuyruklar: Yineleyici[Kuyruk[T]] = d.tails
    def önler: Yineleyici[Kuyruk[T]] = d.inits

    // --- folding, reducing, scanning --------------------------------
    def katla[S >: T](z: S)(işlev: (S, S) => S): S = d.fold(z)(işlev)
    def indirgeSoldan[S >: T](işlem: (S, T) => S): S = d.reduceLeft(işlem)
    def indirgeSağdan[S >: T](işlem: (T, S) => S): S = d.reduceRight(işlem)
    def indirgeBelki[S >: T](işlem: (S, S) => S): Belki[S] = d.reduceOption(işlem)
    def indirgeSoldanBelki[S >: T](işlem: (S, T) => S): Belki[S] = d.reduceLeftOption(işlem)
    def indirgeSağdanBelki[S >: T](işlem: (T, S) => S): Belki[S] = d.reduceRightOption(işlem)
    // tara: like katla, but gives ALL the intermediate results
    def tara[S >: T](z: S)(işlev: (S, S) => S): Kuyruk[S] = d.scan(z)(işlev)
    def taraSoldan[B](z: B)(işlev: (B, T) => B): Kuyruk[B] = d.scanLeft(z)(işlev)
    def taraSağdan[B](z: B)(işlev: (T, B) => B): Kuyruk[B] = d.scanRight(z)(işlev)
    def enUfağıBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.minOption(sıralama)
    def enUfağıBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.minByOption(iş)(karşılaştırma)
    def enİrisiBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.maxOption(sıralama)
    def enİrisiBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.maxByOption(iş)(karşılaştırma)

    // --- adding, removing -------------------------------------------
    def sonunaEkle[S >: T](öge: S): Kuyruk[S] = d.appended(öge)
    def önüneEkle[S >: T](öge: S): Kuyruk[S] = d.prepended(öge)
    def sonunaEkleHepsini[S >: T](öbürü: YinelenebilirBirKere[S]): Kuyruk[S] = d.appendedAll(öbürü)
    def önüneEkleHepsini[S >: T](öbürü: YinelenebilirBirKere[S]): Kuyruk[S] = d.prependedAll(öbürü)
    def uzat[S >: T](boy: Sayı, öge: S): Kuyruk[S] = d.padTo(boy, öge)
    def yama[S >: T](nereden: Sayı, yenisi: YinelenebilirBirKere[S], kaçTane: Sayı): Kuyruk[S] =
      d.patch(nereden, yenisi, kaçTane)
    def fark[S >: T](öbürü: Diz[S]): Kuyruk[T] = d.diff(öbürü)
    def kesişim[S >: T](öbürü: Diz[S]): Kuyruk[T] = d.intersect(öbürü)
    def bileşim[S >: T](öbürü: Diz[S]): Kuyruk[S] = d.concat(öbürü)

    // --- selecting, flattening, pairwise operations -----------------
    def seçİşle[B](işlev: PartialFunction[T, B]): Kuyruk[B] = d.collect(işlev)
    def seçİşleİlk[B](işlev: PartialFunction[T, B]): Belki[B] = d.collectFirst(işlev)
    def düzleştir[B](implicit delil: T => YinelenebilirBirKere[B]): Kuyruk[B] = d.flatten(delil)
    def devrik[B](implicit delil: T => Yinelenebilir[B]): Kuyruk[Kuyruk[B]] = d.transpose(delil)
    def ikiliyiAç[A1, A2](implicit delil: T => (A1, A2)): (Kuyruk[A1], Kuyruk[A2]) = d.unzip(delil)
    def ikileHepsini[B, S >: T](öbürü: Yinelenebilir[B], buDolgu: S, oDolgu: B): Kuyruk[(S, B)] =
      d.zipAll(öbürü, buDolgu, oDolgu)
    def tersİşle[B](işlev: T => B): Kuyruk[B] = d.reverse.map(işlev)

    // --- the ones that change IN PLACE ------------------------------
    // They change the queue ITSELF. The logic of a queue: kuyruğaEkle puts at the end, baştanÇıkar takes
    // from the start (first in, first out).
    def kuyruğaEkle(öge: T): Col = { d.enqueue(öge); d }
    def kuyruğaEkleHepsini(ögeler: YinelenebilirBirKere[T]): Col = { d.enqueueAll(ögeler); d }
    def baştanÇıkar(): T = d.dequeue()
    def baştanÇıkarBelki: Belki[T] = d.removeHeadOption()
    def baştanÇıkarDoğruKaldıkça(deneme: T => İkil): Diz[T] = d.removeHeadWhile(deneme)
    def baştanÇıkarKoşulla(deneme: T => İkil): Belki[T] = d.dequeueFirst(deneme)
    def baştanÇıkarHepsiniKoşulla(deneme: T => İkil): Diz[T] = d.dequeueWhile(deneme)
    // CAREFUL: dequeueWhile starts from the front and stops at the first one that does not fit; dequeueAll
    // collects those that fit from EVERYWHERE in the queue. The name comes from the desktop.
    def baştanAlHepsini(deneme: T => İkil): Dizi[T] = d.dequeueAll(deneme).toList
    def sondanÇıkar(): T = d.removeLast()
    def sondanÇıkarBelki: Belki[T] = d.removeLastOption()
    def ilki: T = d.front
    def eleYerinde(deneme: T => İkil): Col = { d.filterInPlace(deneme); d }
    def işleYerinde(işlev: T => T): Col = { d.mapInPlace(işlev); d }
    def sıralıYerinde(implicit sıralama: Ordering[T]): Col = { d.sortInPlace()(sıralama); d }
    def sıralaYerinde[B](iş: T => B)(implicit sıralama: Ordering[B]): Col = { d.sortInPlaceBy(iş)(sıralama); d }
    @deprecated("eylemle başlayan ada geçildi: ekleHepsini kullanın", "Eylül 2026")
    def hepsiniEkle(ögeler: YinelenebilirBirKere[T]): Col = ekleHepsini(ögeler)
    def ekleHepsini(ögeler: YinelenebilirBirKere[T]): Col = { d.addAll(ögeler); d }
    def koyHepsini(ögeler: YinelenebilirBirKere[T]): Col = { d.addAll(ögeler); d }
    @deprecated("eylemle başlayan ada geçildi: ekleAraya kullanın", "Eylül 2026")
    def araEkle(yeri: Sayı, öge: T): Birim = ekleAraya(yeri, öge)
    def çıkar(yeri: Sayı): T = d.remove(yeri)
    def çıkarHepsini(ögeler: YinelenebilirBirKere[T]): Col = { d.subtractAll(ögeler); d }
    def boşalt(): Birim = d.clear()
    def sıralı(implicit sıralama: Ordering[T]): Diz[T] = d.sorted(sıralama)
    def sırala[B](iş: T => B)(implicit sıralama: Ordering[B]): Diz[T] = d.sortBy(iş)(sıralama)
    def sırayaSok(önce: (T, T) => İkil): Diz[T] = d.sortWith(önce)
    def içeriyorMu[S >: T](öge: S): İkil = d.contains(öge)
    def sırası[S >: T](öge: S): Sayı = d.indexOf(öge)
    def sırasıSondan[S >: T](öge: S): Sayı = d.lastIndexOf(öge)
    def yinelemesiz: Diz[T] = d.distinct
    def yinelemesizİşlevle[B](işlev: T => B): Diz[T] = d.distinctBy(işlev)
    // --- the in-place methods of ArrayDeque --------------------------
    // The naming rule: START WITH THE ACTION -- ekleAraya, alHepsini, çıkarSondan.
    def alYerinde(kaçTane: Sayı): Col = { d.takeInPlace(kaçTane); d }
    def alSağdanYerinde(kaçTane: Sayı): Col = { d.takeRightInPlace(kaçTane); d }
    def alDoğruKaldıkçaYerinde(deneme: T => İkil): Col = { d.takeWhileInPlace(deneme); d }
    def düşürYerinde(kaçTane: Sayı): Col = { d.dropInPlace(kaçTane); d }
    def düşürSağdanYerinde(kaçTane: Sayı): Col = { d.dropRightInPlace(kaçTane); d }
    def düşürDoğruKaldıkçaYerinde(deneme: T => İkil): Col = { d.dropWhileInPlace(deneme); d }
    def dilimYerinde(nereden: Sayı, nereye: Sayı): Col = { d.sliceInPlace(nereden, nereye); d }
    def uzatYerinde(boy: Sayı, öge: T): Col = { d.padToInPlace(boy, öge); d }
    def yamaYerinde(nereden: Sayı, yenisi: YinelenebilirBirKere[T], kaçTane: Sayı): Col = {
      d.patchInPlace(nereden, yenisi, kaçTane); d
    }
    def düzİşleYerinde(işlev: T => YinelenebilirBirKere[T]): Col = { d.flatMapInPlace(işlev); d }
    def sırayaSokYerinde(önce: (T, T) => İkil): Col = { d.sortInPlaceWith(önce); d }

    // --- access by position, removing in bulk -----------------------
    def güncelle(yeri: Sayı, öge: T): Birim = d.update(yeri, öge)
    def ekleAraya(yeri: Sayı, öge: T): Birim = d.insert(yeri, öge)
    def ekleArayaHepsini(yeri: Sayı, ögeler: YinelenebilirBirKere[T]): Birim = d.insertAll(yeri, ögeler)
    def alHepsini: Dizi[T] = d.removeAll()
    def alHepsiniTersten: Dizi[T] = d.removeAllReverse()
    def çıkarİlkUyanı(deneme: T => İkil): Belki[T] = d.removeFirst(deneme)
    def çıkarSondanDoğruKaldıkça(deneme: T => İkil): Dizi[T] = d.removeLastWhile(deneme)
    // çıkarSondan/çıkarSondanBelki: the form of the sondanÇıkar family that starts with the action
    def çıkarSondan(): T = d.removeLast()
    def çıkarSondanBelki: Belki[T] = d.removeLastOption()
    def dizime[S >: T](implicit delil: scala.reflect.ClassTag[S]): Dizim[S] = new Dizim(d.toArray(delil))

}

  /** An alias of the `ÖncelikSırası` object -- for `ÖncelikKuyruğu.boş` / `ÖncelikKuyruğu(...)`. */
  val ÖncelikKuyruğu = ÖncelikSırası

  implicit class ÖncelikSırasıMetotları[T](d: ÖncelikSırası[T]) {
    type Col = ÖncelikSırası[T]
    type C2[B] = ÖncelikSırası[B]
    type Belki[B] = Option[B]
    type Eşlek[K, D] = collection.immutable.Map[K, D]
    type Iter[A] = collection.mutable.Iterable[A]
    def ekle(öge: T): ÖncelikSırası[T] = d.addOne(öge) // the same as `d += öge`
    def koy(öge: T): ÖncelikSırası[T] = d.addOne(öge) // a name shared with Yığın/Kuyruk
    def çıkar(): T = d.dequeue()
    def başı: T = d.head
    def boyu: Sayı = d.length
    def tane: Sayı = d.size // the same as boyu (the name in the booklet)
    def boşMu: İkil = d.isEmpty
    def doluMu: İkil = d.nonEmpty
    def sil(): Birim = d.clear()
    def dizine: Dizin[T] = d.toList
  
    // --- the core shared with the other collections ---------------
    // NOTE: the methods that PRODUCE a collection give a Dizi, not an ÖncelikSırası. The reason: making a
    // new priority queue wants an implicit Ordering and would make the signatures needlessly complicated.
    // If an ordered result is needed, build it again explicitly with ÖncelikSırası(...).
    def başıBelki: Belki[T] = d.headOption
    def sonuBelki: Belki[T] = d.lastOption
    def bul(deneme: T => İkil): Belki[T] = d.find(deneme)
    def böl(deneme: T => İkil): (Dizi[T], Dizi[T]) = {
      val (e, h) = d.iterator.toSeq.partition(deneme); (e, h)
    }
    def bölDoğruKaldıkça(deneme: T => İkil): (Dizi[T], Dizi[T]) = d.iterator.toSeq.span(deneme)
    def bölYerinden(yeri: Sayı): (Dizi[T], Dizi[T]) = d.iterator.toSeq.splitAt(yeri)
    def öbekli(boy: Sayı): Yineleyici[Dizi[T]] = d.iterator.toSeq.grouped(boy)
    def kayarÖbekli(boy: Sayı): Yineleyici[Dizi[T]] = d.iterator.toSeq.sliding(boy)
    def öbekleİşle[K, B](anahtar: T => K)(değer: T => B): Eşlek[K, Dizi[B]] =
      d.iterator.toSeq.groupMap(anahtar)(değer)
    def öbekleİşleİndirge[K, B](anahtar: T => K)(değer: T => B)(indirge: (B, B) => B): Eşlek[K, B] =
      d.iterator.toSeq.groupMapReduce(anahtar)(değer)(indirge)
    def kuyruklar: Yineleyici[Dizi[T]] = d.iterator.toSeq.tails
    def önler: Yineleyici[Dizi[T]] = d.iterator.toSeq.inits
    def katla[S >: T](z: S)(işlev: (S, S) => S): S = d.fold(z)(işlev)
    def indirgeSoldan[S >: T](işlem: (S, T) => S): S = d.reduceLeft(işlem)
    def indirgeSağdan[S >: T](işlem: (T, S) => S): S = d.reduceRight(işlem)
    def indirgeBelki[S >: T](işlem: (S, S) => S): Belki[S] = d.reduceOption(işlem)
    def indirgeSoldanBelki[S >: T](işlem: (S, T) => S): Belki[S] = d.reduceLeftOption(işlem)
    def indirgeSağdanBelki[S >: T](işlem: (T, S) => S): Belki[S] = d.reduceRightOption(işlem)
    def tara[S >: T](z: S)(işlev: (S, S) => S): Dizi[S] = d.iterator.toSeq.scan(z)(işlev)
    def taraSoldan[B](z: B)(işlev: (B, T) => B): Dizi[B] = d.iterator.toSeq.scanLeft(z)(işlev)
    def taraSağdan[B](z: B)(işlev: (T, B) => B): Dizi[B] = d.iterator.toSeq.scanRight(z)(işlev)
    def enUfağıBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.minOption(sıralama)
    def enUfağıBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.minByOption(iş)(karşılaştırma)
    def enİrisiBelki[S >: T](implicit sıralama: math.Ordering[S]): Belki[T] = d.maxOption(sıralama)
    def enİrisiBelki[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): Belki[T] = d.maxByOption(iş)(karşılaştırma)
    def seçİşle[B](işlev: PartialFunction[T, B]): Dizi[B] = d.iterator.toSeq.collect(işlev)
    def seçİşleİlk[B](işlev: PartialFunction[T, B]): Belki[B] = d.collectFirst(işlev)
    def düzleştir[B](implicit delil: T => YinelenebilirBirKere[B]): Dizi[B] = d.iterator.toSeq.flatten(delil)
    def devrik[B](implicit delil: T => Yinelenebilir[B]): Dizi[Dizi[B]] = d.iterator.toSeq.transpose(delil)
    def ikiliyiAç[A1, A2](implicit delil: T => (A1, A2)): (Dizi[A1], Dizi[A2]) = d.iterator.toSeq.unzip(delil)
    def ikileHepsini[B, S >: T](öbürü: Yinelenebilir[B], buDolgu: S, oDolgu: B): Dizi[(S, B)] =
      d.iterator.toSeq.zipAll(öbürü, buDolgu, oDolgu)

    // --- the ones that change IN PLACE ------------------------------
    def işleYerinde(işlev: T => T): Col = { d.mapInPlace(işlev); d }
    def ekleHepsini(ögeler: YinelenebilirBirKere[T]): Col = { d.addAll(ögeler); d }
    def koyHepsini(ögeler: YinelenebilirBirKere[T]): Col = { d.addAll(ögeler); d }
    @deprecated("eylemle başlayan ada geçildi: ekleHepsini kullanın", "Eylül 2026")
    def hepsiniEkle(ögeler: YinelenebilirBirKere[T]): Col = ekleHepsini(ögeler)
    def kuyruğa: Kuyruk[T] = d.toQueue

    // --- in step with desktop Koco (see lite/i18n/tr/kuyruk.scala) ---
    // The ones that were in mutPriQueMethods of the desktop from the start and were missing in the web Kojo.
    def ekle(ögeler: T*) = d.enqueue(ögeler: _*)
    def baştanAl(): T = d.dequeue()
    def baştanAlHepsini[T2 >: T]: Dizi[T2] = d.dequeueAll.toList // it used to give an ArraySeq: the output showed a DizikDizisi
    def ikizle(): Col = d.clone()
    def kuyruğu: Col = d.tail
    def önü: Col = d.init
    def sonu: T = d.last
    def ele(deneme: T => İkil): Col = d.filter(deneme)
    def eleDeğilse(deneme: T => İkil): Col = d.filterNot(deneme)
    def işle[A](işlev: T => A): Iter[A] = d.map(işlev)
    def düzİşle[A](işlev: T => C2[A]): Iter[A] = d.flatMap(işlev)
    def indirge[B >: T](işlem: (B, B) => B): B = d.reduce(işlem)
    def soldanKatla[T2](z: T2)(işlev: (T2, T) => T2): T2 = d.foldLeft(z)(işlev)
    def sağdanKatla[T2](z: T2)(işlev: (T, T2) => T2): T2 = d.foldRight(z)(işlev)
    def topla[T2 >: T](implicit num: scala.math.Numeric[T2]) = d.sum(num)
    def çarp[T2 >: T](implicit num: scala.math.Numeric[T2]) = d.product(num)
    def yazıYap: Yazı = d.mkString
    def yazıYap(ara: Yazı): Yazı = d.mkString(ara)
    def yazıYap(başı: Yazı, ara: Yazı, sonu: Yazı): Yazı = d.mkString(başı, ara, sonu)
    def tersi = d.reverse
    def herbiriİçin[S](işlev: T => S): Birim = d.foreach(işlev)
    def varMı(deneme: T => İkil): İkil = d.exists(deneme)
    def hepsiDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def hepsiİçinDoğruMu(deneme: T => İkil): İkil = d.forall(deneme)
    def al(n: Sayı): Col = d.take(n)
    def alDoğruKaldıkça(deneme: T => İkil): Col = d.takeWhile(deneme)
    def alSağdan(n: Sayı): Col = d.takeRight(n)
    def düşür(n: Sayı): Col = d.drop(n)
    def düşürDoğruKaldıkça(deneme: T => İkil): Col = d.dropWhile(deneme)
    def düşürSağdan(n: Sayı): Col = d.dropRight(n)
    def diziye = d.toSeq
    def kümeye = d.toSet
    def yöneye = d.toVector
    def dizime[S >: T](implicit delil: scala.reflect.ClassTag[S]): Dizim[S] = new Dizim(d.toArray(delil))
    def eşleğe[A, D](implicit delil: T <:< (A, D)): Eşlek[A, D] = d.toMap
    def eşleme[A, D](implicit delil: T <:< (A, D)): Eşlem[A, D] = Eşlem.değişmezden(d.toMap)
    def say(işlev: T => İkil): Sayı = d.count(işlev)
    def dilim(nereden: Sayı, nereye: Sayı) = d.slice(nereden, nereye)
    def ikile[S](öbürü: YinelenebilirBirKere[S]) = d.zip(öbürü)
    def ikileSırayla = d.zipWithIndex
    def ikileKonumla = d.zipWithIndex
    def öbekle[A](iş: T => A): Eşlek[A, Col] = d.groupBy(iş)
    def enUfağı[B >: T](implicit sıralama: math.Ordering[B]): T = d.min(sıralama)
    def enUfağı[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): T = d.minBy(iş)(karşılaştırma)
    def enİrisi[B >: T](implicit sıralama: math.Ordering[B]): T = d.max(sıralama)
    def enİrisi[B](iş: T => B)(implicit karşılaştırma: math.Ordering[B]): T = d.maxBy(iş)(karşılaştırma)
}

  /**
   * The double-ended queue (the `deque` of C++, `ArrayDeque` in Scala).
   *
   * You can put at both ends and take from both ends. For jobs that need to walk through the order in two
   * directions (undo/redo, a sliding window, a search from both ends) it is easier than keeping a queue and
   * a stack separately.
   *
   * NAMES: `koy`/`ekle` put at the end (the same as Kuyruk), `başaKoy` at the front. Because taking is
   * at both ends there is NO shared `al` -- `baştanAl`/`sondanAl` say which end it is taken from.
   *
   * ============ CAREFUL: THESE METHODS LEAK INTO YIĞIN AND KUYRUK TOO ==========
   *
   * In Scala 2.13 `mutable.Stack` AND `mutable.Queue` both derive from `ArrayDeque`. Because
   * `İkiUçluKuyruk` is a type ALIAS (not a wrapper class) the methods below can be used on `Yığın` and
   * `Kuyruk` values too -- and they work with the deque meaning. Measured:
   *
   *   Yığın:  koy(1); koy(2)  -> tepe = 2          (right, push)
   *           başaKoy(0)      -> List(0, 2, 1)
   *           sondanAl()      -> 1                 took from the BOTTOM OF THE STACK
   *
   *   Kuyruk: koy(1); koy(2); başaKoy(0) -> başı = 0   it QUEUE-JUMPED
   *           sondanAl()      -> 2                 took from the END of the queue
   *
   * So `yığın.sondanAl()` breaks LIFO and `kuyruk.başaKoy(x)` breaks FIFO, and the compiler says nothing.
   * This is NOT AN ACCIDENT, it is a known cost: without the type alias `İkiUçluKuyruk` would lose all the
   * Diz/Yinelenebilir methods (ele, işle, bul...). Turning it into a wrapper would stop the leak but bring
   * that cost.
   *
   * There is no problem with the SHARED names: for names like `koy`, `başı` and `boyu` the MORE SPECIFIC
   * implicit class wins (Stack <: ArrayDeque), so `yığın.koy` works as a push. What leaks is only the names
   * that are SPECIFIC to İkiUçluKuyruk.
   *
   * Today's behavior is pinned by the tests -- if it changes silently tomorrow they go red.
   */
  object İkiUçluKuyruk {
    def apply[T](ögeler: T*): İkiUçluKuyruk[T] = ArrayDeque.from(ögeler)
    def boş[T]: İkiUçluKuyruk[T] = ArrayDeque.empty[T]
    def doldur[T](başkası: YinelenebilirBirKere[T]): İkiUçluKuyruk[T] = ArrayDeque.from(başkası)
  }

  implicit class İkiUçluKuyrukMetotları[T](d: İkiUçluKuyruk[T]) {
    type Col = İkiUçluKuyruk[T]
    type Belki[B] = Option[B]

    // --- putting: two ends ---
    def koy(öge: T): Col = d.append(öge)
    def ekle(öge: T): Col = d.append(öge)      // the same as koy
    def sonaKoy(öge: T): Col = d.append(öge)   // the same as koy, but says the end explicitly
    def başaKoy(öge: T): Col = d.prepend(öge)
    def koyHepsini(ögeler: YinelenebilirBirKere[T]): Col = { d.addAll(ögeler); d }
    def ekleHepsini(ögeler: YinelenebilirBirKere[T]): Col = { d.addAll(ögeler); d }

    // --- taking: two ends (there is no shared `al`, see the class note) ---
    def baştanAl(): T = d.removeHead()
    def sondanAl(): T = d.removeLast()
    def baştanAlBelki: Belki[T] = d.removeHeadOption()
    def sondanAlBelki: Belki[T] = d.removeLastOption()

    // --- looking (without removing) ---
    def başı: T = d.head
    def sonu: T = d.last
    def başıBelki: Belki[T] = d.headOption
    def sonuBelki: Belki[T] = d.lastOption

    // --- size and upkeep ---
    def boyu: Sayı = d.length
    def tane: Sayı = d.size
    def boşMu: İkil = d.isEmpty
    def doluMu: İkil = d.nonEmpty
    def sil(): Birim = d.clear()
    def ikizle(): Col = d.clone()
    def dizi: Dizi[T] = d.toList
    def tersi: Col = ArrayDeque.from(d.reverse)
  }
}
