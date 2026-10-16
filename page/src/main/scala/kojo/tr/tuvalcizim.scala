package kojo.tr

/**
 * The drawing surface of a sketch: the same names as the desktop `TuvalÇizim` (= lite.CanvasDraw,
 * lite/i18n/tr/tuvalcizim.scala). The real thing is kojo.CanvasDraw (an HTML canvas).
 *
 * WHERE IT DIFFERS from the desktop (there is no AWT): there is no `şekil(java.awt.Shape)` and no
 * `imge(...)`; `karışımKipi` takes the name of the canvas blend mode instead of a `java.awt.Composite`
 * (for example `tuval.karışımKipi("lighter")`).
 */
trait TuvalÇizimYöntemleri extends TemelTürler with RenkYöntemleri with YazıyüzüYöntemleri {
  type TuvalÇizim = kojo.CanvasDraw
  type Eskiz = kojo.tr.Eskiz

  implicit class canvasDrawMethods(tç: TuvalÇizim) {
    def fırça(kırmızı: Sayı, yeşil: Sayı, mavi: Sayı, saydam: Sayı = 255): Birim = tç.stroke(kırmızı, yeşil, mavi, saydam)
    def fırça(r: Renk): Birim = tç.stroke(r)
    def fırçasız(): Birim = tç.noStroke()
    def fırçaAğırlığı(k: Kesir): Birim = tç.strokeWeight(k)
    def boya(kırmızı: Sayı, yeşil: Sayı, mavi: Sayı, saydam: Sayı = 255): Birim = tç.fill(kırmızı, yeşil, mavi, saydam)
    def boya(renk: Renk): Birim = tç.fill(renk)
    def boyasız(): Birim = tç.noFill()
    def artalan(kırmızı: Sayı, yeşil: Sayı, mavi: Sayı, saydam: Sayı = 255): Birim = tç.background(kırmızı, yeşil, mavi, saydam)
    def artalan(renk: Renk): Birim = tç.background(renk)

    def elips(x: Kesir, y: Kesir, en: Kesir, boy: Kesir): Birim = tç.ellipse(x, y, en, boy)
    def yay(merkezX: Kesir, merkezY: Kesir, enYarıçapı: Kesir, boyYarıçapı: Kesir, başlangıçAçısı: Kesir, yayınAçısı: Kesir): Birim =
      tç.arc(merkezX, merkezY, enYarıçapı, boyYarıçapı, başlangıçAçısı, yayınAçısı)
    def nokta(x: Kesir, y: Kesir): Birim = tç.point(x, y)
    def çizgi(x1: Kesir, y1: Kesir, x2: Kesir, y2: Kesir): Birim = tç.line(x1, y1, x2, y2)
    def dikdörtgen(x1: Kesir, y1: Kesir, x2: Kesir, y2: Kesir): Birim = tç.rect(x1, y1, x2, y2)
    def yazıYüzü(yy: Yazıyüzü): Birim = tç.textFont(yy)
    def yazı(yazı: Yazı, x: Kesir, y: Kesir): Birim = tç.text(yazı, x, y)

    // Randomness and the loop
    def rastgeleTohum(tohum: Uzun): Birim = tç.randomSeed(tohum)
    def döngüyüDurdur(): Birim = tç.noLoop()
    def döngüdeMi: İkil = tç.loop

    // Transforms: götür/döndür/büyüt, the same names as on Resim.
    // On the canvas döndürRadyan works in radians and döndürDerece in degrees (rotate / rotateDegrees).
    def götür(x: Kesir, y: Kesir): Birim = tç.translate(x, y)
    def döndürRadyan(açı: Kesir): Birim = tç.rotate(açı)
    def döndürDerece(derece: Kesir): Birim = tç.rotateDegrees(derece)
    def büyüt(çarpan: Kesir): Birim = tç.scale(çarpan)
    def büyüt(xÇarpanı: Kesir, yÇarpanı: Kesir): Birim = tç.scale(xÇarpanı, yÇarpanı)
    def dönüşümüKaydet(): Birim = tç.pushMatrix()
    def dönüşümüGeriYükle(): Birim = tç.popMatrix()

    // The pen cap and join, the blend mode
    def fırçaUcu(uç: Sayı): Birim = tç.strokeCap(uç)
    def fırçaBirleşimi(birleşim: Sayı): Birim = tç.strokeJoin(birleşim)
    def yuvarlakUç: Sayı = tç.ROUND        // fırçaUcu(tuval.yuvarlakUç)
    def projeUç: Sayı = tç.PROJECT
    def kareUç: Sayı = tç.SQUARE
    def sivriBirleşim: Sayı = tç.MITER     // fırçaBirleşimi(tuval.sivriBirleşim)
    def eğikBirleşim: Sayı = tç.BEVEL
    /** A `java.awt.Composite` on the desktop; here a canvas blend name: "source-over", "lighter", "multiply", ... */
    def karışımKipi(kip: Yazı): Birim = tç.blendMode(kip)

    // Drawing a shape made of vertices
    def şekilBaşla(): Birim = tç.beginShape()
    def şekilBitir(): Birim = tç.endShape()
    def köşe(x: Kesir, y: Kesir): Birim = tç.vertex(x, y)
    def ikinciDereceKöşe(mx: Kesir, my: Kesir, x2: Kesir, y2: Kesir): Birim = tç.quadraticVertex(mx, my, x2, y2)
    def bezierKöşesi(mx1: Kesir, my1: Kesir, mx2: Kesir, my2: Kesir, x2: Kesir, y2: Kesir): Birim =
      tç.bezierVertex(mx1, my1, mx2, my2, x2, y2)
    def eğriKöşesi(x: Kesir, y: Kesir): Birim = tç.curveVertex(x, y)
    def yayKöşesi(x: Kesir, y: Kesir, açı: Kesir): Birim = tç.arcVertex(x, y, açı)
    def açısalKöşe(boyu: Kesir, açısı: Kesir): Birim = tç.vertexRt(boyu, açısı)
    def açısalEğriKöşesi(boyu: Kesir, açısı: Kesir): Birim = tç.curveVertexRt(boyu, açısı)
  }
}
