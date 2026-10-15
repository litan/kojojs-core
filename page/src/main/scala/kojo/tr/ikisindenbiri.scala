/*
 * Copyright (C) 2026
 *   Bulent Basaran <ben@scala.org> https://github.com/bulent2k2
 *   Lalit Pant <pant.lalit@gmail.com>
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
 * İkisindenBiri (Either): either the value on the left or the one on the right, never both.
 *
 * Like Belki, but the empty side carries a value too: in Belki "nothing" is only Hiçbiri, here you can
 * also say why there is nothing. Convention: the RIGHT side is the one where things go well, so işle,
 * düzİşle and alYoksa all look to the right. (In English "right" also means "correct", hence the joke.)
 *
 * Why it was added: bölİşle (partitionMap) needs an İkisindenBiri, and without a Turkish name that
 * method could not be documented.
 */
trait İkisindenBiriYöntemleri extends TemelTürler {
  type İkisindenBiri[A, B] = Either[A, B]

  object Sol {
    def apply[A, B](değer: A): İkisindenBiri[A, B] = Left(değer)
    def açımla[A, B](i: İkisindenBiri[A, B]): Option[A] = Left.unapply(i.asInstanceOf[Left[A, B]])
  }
  object Sağ {
    def apply[A, B](değer: B): İkisindenBiri[A, B] = Right(değer)
  }

  object İkisindenBiri {
    // Sağ if the condition is true, Sol if not
    def koşulla[A, B](koşul: İkil, sağdaki: => B, soldaki: => A): İkisindenBiri[A, B] =
      Either.cond(koşul, sağdaki, soldaki)
  }

  implicit class İkisindenBiriYöntem[A, B](i: İkisindenBiri[A, B]) {
    type Belki[T] = Option[T]

    // --- which side -------------------------------------------------------
    def solMu: İkil = i.isLeft
    def sağMı: İkil = i.isRight

    // --- handling both sides at once --------------------------------------
    // katla: runs the function that fits the side it is on and gives ONE result.
    // (This is Either's only fold; katla on collections does something else.)
    def katla[C](solİşlev: A => C, sağİşlev: B => C): C = i.fold(solİşlev, sağİşlev)
    // birleştir: if both sides have the same type, gives the value of whichever side it is
    def birleştir[C >: A](implicit delil: B <:< C): C = i.fold(a => a, b => delil(b))
    // takasla: left and right change places
    def takasla: İkisindenBiri[B, A] = i.swap

    // --- working with the right side (convention: right = the side where things go well)
    def işle[C](işlev: B => C): İkisindenBiri[A, C] = i.map(işlev)
    def düzİşle[A1 >: A, C](işlev: B => İkisindenBiri[A1, C]): İkisindenBiri[A1, C] = i.flatMap(işlev)
    def düzleştir[A1 >: A, C](implicit delil: B <:< İkisindenBiri[A1, C]): İkisindenBiri[A1, C] =
      i.flatten(delil)
    def alYoksa[B1 >: B](varsayılan: => B1): B1 = i.getOrElse(varsayılan)
    def boşsaÖbürü[A1 >: A, B1 >: B](öbürü: => İkisindenBiri[A1, B1]): İkisindenBiri[A1, B1] =
      i.orElse(öbürü)
    def içeriyorMu[B1 >: B](değer: B1): İkil = i.contains(değer)
    def varMı(deneme: B => İkil): İkil = i.exists(deneme)
    def hepsiDoğruMu(deneme: B => İkil): İkil = i.forall(deneme)
    def herbiriİçin[U](işlev: B => U): Birim = i.foreach(işlev)

    // --- converting -------------------------------------------------------
    def belkiye: Belki[B] = i.toOption // Biri if on the right, Hiçbiri if on the left
    def diziye: Dizi[B] = i.toSeq
  }
}
