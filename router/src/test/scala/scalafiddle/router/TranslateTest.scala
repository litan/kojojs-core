package scalafiddle.router

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import upickle.default._

/** The core of `/translate`. The translator's own behaviour is tested in the desktop repo
  * (TranslatorTest, TranslatorCompileTest); here only "does it work on this server, on this classpath,
  * with these resources" and the rules of the thin layer: direction detection, invalid direction,
  * JSON shape.
  */
class TranslateTest extends AnyFunSuite with Matchers {
  val halka =
    """tanım kenar(x0: Kesir, y0: Kesir): Birim =
      |  için (i <- 1 to 10) noktayaGit(x0 * i / 10, y0 * i / 10)
      |dez halka = Resim { kalemKalınlığınıKur(0); kenar(10, 20) }
      |çiz(halka)
      |halka.fareyeTıklayınca { (x, y) => satıryaz(f"$x%.2f") }
      |yinele(3) { ileri() }
      |""".stripMargin

  test("Turkish script is translated to English; the direction is detected from the script") {
    val s = Translate.translate(halka, None).toOption.get
    s.direction shouldBe Translate.tr2en
    s.code should include("def kenar(x0: Double, y0: Double): Unit =")
    s.code should include("val halka = Picture {")
    s.code should include("setPenThickness(0)")
    s.code should include("repeat(3) { forward() }")
    s.code should include("halka.onMouseClick")
    s.remainingKeywords shouldBe empty
    s.report.translated should be > 10
  }

  test("sketch: Resim.eskizden -> Picture.fromSketch, tuval.nokta -> point (the editor's `Picture.eskizden` error)") {
    val eskiz =
      """sınıf Gezginler yayar Eskiz {
        |  tanım kurulum(tuval: TuvalÇizim) { tuval.artalan(siyah) }
        |  tanım çizimDöngüsü(tuval: TuvalÇizim) { tuval.fırça(kırmızı); tuval.nokta(10, 20) }
        |}
        |çiz(Resim.eskizden(yeni Gezginler))
        |canlandırTuvalÇizimle { t => t.nokta(1, 2) }
        |""".stripMargin
    val s = Translate.translate(eskiz, None).toOption.get
    s.direction shouldBe Translate.tr2en
    s.code should include("draw(Picture.fromSketch(new Gezginler))")
    s.code should not include "eskizden"
    s.code should include("tuval.point(10, 20)")
    s.code should include("t.point(1, 2)")
    s.code should not include "vertex"
  }

  test("English script is translated to Turkish; with no Turkish keyword it counts as English") {
    val s = Translate.translate("val r = Picture.circle(50)\ndraw(r)\nrepeat(3) { forward(10) }\n", None).toOption.get
    s.direction shouldBe Translate.en2tr
    s.code should include("dez r = Resim.daire(50)")
    s.code should include("çiz(r)")
    s.code should include("yinele(3) { ileri(10) }")
    Translate.detectDirection("ileri(10)") shouldBe Translate.en2tr
    Translate.detectDirection("dez a = 1") shouldBe Translate.tr2en
  }

  test("a name with several translations is reported as ambiguous, with the chosen one and the others") {
    val s = Translate.translate("dez a = Resim.daire(50)\n", None).toOption.get
    s.report.ambiguous should contain(Translate.Ambiguous("daire", "Picture.circle", Seq("circle"), 1))
  }

  test("an explicit direction overrides detection; 'auto' and an empty direction detect from the script") {
    Translate.translate("dez a = 1", Some("en2tr")).toOption.get.direction shouldBe Translate.en2tr
    Translate.translate("dez a = 1", Some("auto")).toOption.get.direction shouldBe Translate.tr2en
    Translate.translate("dez a = 1", Some("")).toOption.get.direction shouldBe Translate.tr2en
  }

  test("invalid direction is a Left; the message names the value") {
    Translate.translate("dez a = 1", Some("xx")).swap.toOption.get should include("'xx'")
  }

  test("the user's own names are not translated; those with Turkish letters appear in the report's 'leftovers', with counts") {
    // The report lists only what the TRANSLATOR calls "still Turkish": names with Turkish letters (ı ş ğ ö ü ç)
    // or names known to the dictionary. An ASCII user name such as `halka` stays untranslated but is not
    // listed (it may be an English name; measured).
    val s = Translate.translate("dez başlık = \"a\"\nsatıryaz(başlık)\nsatıryaz(başlık + başlık)\n", None).toOption.get
    s.code should include("val başlık")
    s.report.leftovers should contain(Translate.NameCount("başlık", 4))
    val h = Translate.translate(halka, None).toOption.get
    h.code should include("val halka")
    h.report.leftovers.map(_.name) should not contain "halka"
  }

  test("iKojo-specific konumuOku / yönüOku -> readPosition / readHeading (kojo#79)") {
    val s = Translate.translate("dez a = 1\nkonumuOku { n => satıryaz(n.x) }\nyönüOku { y => satıryaz(y) }\n", None).toOption.get
    s.code should include("readPosition {")
    s.code should include("readHeading {")
  }

  test("iKojo-specific soluk / renkliYazı / canlandırmayıDurdur and the user's own time variable (kojo#84)") {
    val k = "dez a = yeşil.soluk(0.8)\nden zaman = 0\nzaman += 1\nçiz(kalemKalınlığı(2) -> Resim.renkliYazı(\"x\", 40, mavi))\ncanlandırmayıDurdur()\n"
    val s = Translate.translate(k, None).toOption.get
    s.code should include("green.fadeOut(0.8)")
    s.code should include("var zaman = 0")
    s.code should include("zaman += 1")
    s.code should include("penThickness(2) -> Picture.textu(\"x\", 40, blue)")
    s.code should include("stopAnimation()")
  }

  test("notaÇalarıKapat / notaÇalarıDurdur <-> stopNotePlayer, both directions (kojo#81)") {
    val e = Translate.translate("dez a = 1\nnotaÇalarıKapat()\nnotaÇalarıDurdur()\n", None).toOption.get
    e.code should include("stopNotePlayer()\nstopNotePlayer()")
    // on a tie the primary name (Kapat) is chosen: translation-rules.tsv
    val t = Translate.translate("stopNotePlayer()\nplayNote(60, 500)\n", None).toOption.get
    t.code should include("notaÇalarıKapat()")
    t.code should include("notaÇal(60, 500)")
  }

  test("göster (member) visible, Mp3Çalar.durdur stopMp3, Dizim.boş with sizes ofDim (kojo#82)") {
    val e = Translate.translate("dez a = 1\nateş.göster()\nfrenSesiÇalar.durdur()\ndurdur()\nDizim.boş[Nokta](3, 4)\n", None).toOption.get
    e.code should include("ateş.visible()")
    e.code should include("frenSesiÇalar.stopMp3()")
    e.code should include("\nstopAnimation()\n")
    e.code should include("Array.ofDim[Point](3, 4)")
  }

  test("the result is written to JSON and read back (the shape the client sees)") {
    val s = Translate.translate(halka, None).toOption.get
    val json = write(s)
    ujson.read(json).obj.keySet should contain allOf ("direction", "code", "report", "remainingKeywords")
    read[Translate.Result](json) shouldBe s
  }

  test("Turkish characters (ı ş ğ ö ü ç İ) pass through intact") {
    val s = Translate.translate("dez çiçekAdı = \"İstanbul\"\nsatıryaz(çiçekAdı)\n", None).toOption.get
    s.code should include("\"İstanbul\"")
    s.code should include("çiçekAdı")
  }

  test("the translation pool is separate and daemon: it does not occupy the default dispatcher") {
    import scala.concurrent.{Await, Future}
    import scala.concurrent.duration._
    val (name, daemon) = Await.result(
      Future((Thread.currentThread.getName, Thread.currentThread.isDaemon))(Translate.ec), 5.seconds)
    name shouldBe "translate"
    daemon shouldBe true
  }
}
