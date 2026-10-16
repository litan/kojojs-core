package kojo.tr

import scala.reflect.ClassTag
import scala.collection.mutable.ArrayBuffer

/**
 * Wrappers for Array and ArrayBuffer (the older naming; dizik.scala is newer).
 */
trait DizimYöntemleri extends TemelTürler {

  class EsnekDizim[T](val a: ArrayBuffer[T]) {
    type Col = EsnekDizim[T]
    type C2[A] = EsnekDizim[A]
    def apply(yer: Sayı) = a(yer)
    def sayı = a.size
    def boyu = a.size
    def ekle(öge: T) = { a.append(öge); this }
    def +=(öge: T) = ekle(öge)
    def çıkar(yer: Sayı) = a.remove(yer)
    def sil() = a.clear()
    def dizi = a.toVector
    def diziye = a.toVector
    def dizine = a.toList
    def boşMu: İkil = a.isEmpty
    def doluMu: İkil = a.nonEmpty
    def ele(deneme: T => İkil): Col = new EsnekDizim(a.filter(deneme))
    def eleYerinde(deneme: T => İkil): this.type = { a.filterInPlace(deneme); this }
    def işle[B](işlev: T => B): C2[B] = new EsnekDizim(a.map(işlev))
    def herbiriİçin[B](işlev: T => B): Birim = a.foreach(işlev)
    override def toString = a.mkString("EsnekDizim(", ", ", ")")
  }

  object EsnekDizim {
    def apply[T](ögeler: T*) = new EsnekDizim[T](ArrayBuffer(ögeler: _*))
    def boş[T] = new EsnekDizim[T](ArrayBuffer.empty[T])
  }

  class Dizim[T](val a: Array[T]) {
    def diziye = a.toSeq
    def dizine = a.toList
    def boyu = a.length
    def apply(b1: Sayı) = a(b1)
    def güncelle(yer: Sayı, değer: T): Birim = a(yer) = değer
    def yazıya = toString
    override def toString = a.mkString("Dizim(", ", ", ")")
  }

  object Dizim {
    def apply[T: ClassTag](ögeler: T*) = new Dizim(ögeler.toArray)
    // with parentheses: without them the call Dizim.boş[Nokta](a, b) would take (a, b) for the
    // implicit list (a Scala 2 trap; seen in the genart-tri-mesh sample)
    def boş[T: ClassTag]() = new Dizim(Array.empty[T])
    def boş[T: ClassTag](b1: Sayı) = new Dizim(Array.ofDim[T](b1))
    // In the multi-dimensional ones the inner layer is an Array (as on the desktop), NOT a Dizim:
    // scripts write `tahta(x)(y) = değer`, which expands to `tahta(x).update(y, değer)`. Dizim has no
    // `update` (it has `güncelle`), Array has. Making the inner layer a Dizim broke the tic-tac-toe
    // and genart-tri-mesh samples with "value update is not a member".
    def boş[T: ClassTag](b1: Sayı, b2: Sayı) = new Dizim(Array.ofDim[T](b1, b2))
    def boş[T: ClassTag](b1: Sayı, b2: Sayı, b3: Sayı) = new Dizim(Array.ofDim[T](b1, b2, b3))
    def boşBoyutlu[T: ClassTag](b1: Sayı) = new Dizim(Array.ofDim[T](b1))
    def doldur[T: ClassTag](b1: Sayı)(e: => T) = new Dizim(Array.fill[T](b1)(e))
    def doldur[T: ClassTag](b1: Sayı, b2: Sayı)(e: => T) = new Dizim(Array.fill[T](b1, b2)(e))
    def doldur[T: ClassTag](b1: Sayı, b2: Sayı, b3: Sayı)(e: => T) = new Dizim(Array.fill[T](b1, b2, b3)(e))
  }
}
