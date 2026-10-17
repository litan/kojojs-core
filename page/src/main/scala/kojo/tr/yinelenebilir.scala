/*
 * Copyright (C) 2026
 *   Bulent Basaran <ben@scala.org> https://github.com/bulent2k2
 *
 * The contents of this file are subject to the GNU General Public License
 * Version 3 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.gnu.org/copyleft/gpl.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 */
package kojo.tr

/**
 * The LOW-PRIORITY base: the Yinelenebilir (Iterable) above every collection.
 *
 * Why a separate trait: an Eşlek is an Yinelenebilir too, so for names like `ele` and `dizine` both the
 * wrapper of Eşlek and this one apply, and the compiler says "ambiguous implicit". Scala orders implicit
 * wrappers by inheritance: one in a SUB trait beats one in its parent. That is why the specific collection
 * traits extend this one.
 */
trait YinelenebilirYöntemleri extends TemelTürler with DizimYöntemleri {
  /**
   * Yinelenebilir (Iterable) -- the type ABOVE Diz/Dizi.
   *
   * Why it is needed: Eşlek.anahtarlar and Eşlek.değerler give an Iterable, not a Diz. Without this
   * wrapper `Eşlek("a" -> 1).anahtarlar.dizine` did not compile -- one of the examples in the glossary was
   * exactly that. The counterpart of IterableMethods in the desktop's dizi.scala.
   */
  implicit class YinelenebilirMetotları[T](d: Yinelenebilir[T]) {
    type Col = Yinelenebilir[T]
    type C2[B] = Yinelenebilir[B]
    type Eşlek[A, D] = collection.immutable.Map[A, D]

    def ele(deneme: T => İkil): Col = d.filter(deneme)
    def eleDeğilse(deneme: T => İkil): Col = d.filterNot(deneme)
    def işle[A](işlev: T => A): C2[A] = d.map(işlev)
    def düzİşle[A](işlev: T => C2[A]): C2[A] = d.flatMap(işlev)
    def herbiriİçin[S](işlev: T => S): Birim = d.foreach(işlev)

    def dizine = d.toList
    def diziye = d.toSeq
    def kümeye = d.toSet
    def yöneye = d.toVector
    def dizime[S >: T](implicit delil: scala.reflect.ClassTag[S]): Dizim[S] = new Dizim(d.toArray(delil))
    def eşleğe[A, D](implicit delil: T <:< (A, D)): Eşlek[A, D] = d.toMap
    def ikile[S](öbürü: YinelenebilirBirKere[S]) = d.zip(öbürü)
    def ikileSırayla = d.zipWithIndex
    def ikileKonumla = d.zipWithIndex
    def öbekle[A](iş: (T) => A): Eşlek[A, Col] = d.groupBy(iş)
  }

  /**
   * The iterator that bellekli gives. All the methods above apply to it too (a BufferedIterator is a
   * Yineleyici); the only thing missing was `head`, which lets you look ahead WITHOUT consuming.
   */
  implicit class BellekliYineleyiciMetotları[T](b: collection.BufferedIterator[T]) {
    // reading the head does NOT advance the iterator -- that is the difference from sıradaki
    def başı: T = b.head
  }
}
