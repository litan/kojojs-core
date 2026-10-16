package kojo.tr

/**
 * A Processing-style sketch: `kurulum` runs once and `çizimDöngüsü` on every frame; both draw on the same
 * canvas (TuvalÇizim). It has the SAME names and signatures as the `Eskiz` of desktop Kojo
 * (lite/i18n/tr/eskiz.scala), so a sketch written on the desktop compiles here too.
 *
 *   class İzler extends Eskiz {
 *     def kurulum(tuval: TuvalÇizim) = tuval.artalan(siyah)
 *     def çizimDöngüsü(tuval: TuvalÇizim) = { tuval.boya(kırmızı); tuval.elips(rastgele(100), rastgele(100), 5, 5) }
 *   }
 *   tuvalEskizi(new İzler)
 *
 * (On the desktop the same is written with Turkish keywords: `sınıf İzler yayar Eskiz` and `tanım`.)
 *
 * Why a sketch: the canvas is one picture and what it draws is pixels. A new Picture object is not made
 * for every drawing, so the stage and `PicCache` do not grow. See kojo.SketchPic.
 *
 * On the desktop this feature is tied to the English side by a structural type (setup/drawLoop) and needs
 * an `EskizUyarlayıcı`; here the Turkish layer gives the functions to SketchPic directly (no reflection).
 */
trait Eskiz {
  def kurulum(tuval: kojo.CanvasDraw): Unit
  def çizimDöngüsü(tuval: kojo.CanvasDraw): Unit
}
