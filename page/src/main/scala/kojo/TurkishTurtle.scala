package kojo


// Turkish names for the turtle commands, the same idea as SwedishTurtle. The names follow desktop Kojo's
// Turkish layer (net.kogics.kojo.lite.i18n.trInit), so a script reads the same in both.
//
// englishTurtle is the TurtleAPI that Builtins passes in: its `turtle` (GlobalTurtleForPicture), not
// `turtle0`. Picture { ... } swaps the global turtle for the picture's own turtle while its body runs, and
// a wrapper bound to turtle0 would draw such a picture's lines on the canvas and leave the picture empty.
//
// Also the speeds, and through the traits it extends the colors, the numbers, the collections and the sketch
// drawing, with the names of desktop Kojo's Turkish layer.
//
// Only commands that TurtleAPI has are here. Others (home, towards, costumes, saved styles...) follow when
// the English API has them.
class TurkishTurtle(val englishTurtle: TurtleAPI, builtins: syntax.Builtins)(implicit kojoWorld: KojoWorld)
    extends kojo.tr.SayıYöntemleri
    with kojo.tr.KlavyeYöntemleri
    with kojo.tr.GelecekYöntemleri
    with kojo.tr.MatematikYöntemleri
    with kojo.tr.BelkiYöntemleri
    with kojo.tr.NoktaYöntemleri
    with kojo.tr.DiziYöntemleri
    with kojo.tr.DizikYöntemleri
    with kojo.tr.HarfYöntemleri
    with kojo.tr.İkisindenBiriYöntemleri
    with kojo.tr.BölümselİşlevYöntemleri
    with kojo.tr.KökTürYöntemleri
    with kojo.tr.BuAnYöntemleri
    with kojo.tr.AralıkYöntemleri
    with kojo.tr.DizimYöntemleri
    with kojo.tr.DizinYöntemleri
    with kojo.tr.MiskinDizinYöntemleri
    with kojo.tr.YöneyYöntemleri
    with kojo.tr.Yöney2BYöntemleri
    with kojo.tr.RenkYöntemleri
    with kojo.tr.YazıyüzüYöntemleri
    with kojo.tr.TuvalÇizimYöntemleri
    with kojo.tr.YinelenebilirYöntemleri
    with kojo.tr.EşlemYöntemleri
    with kojo.tr.KümeYöntemleri
    with kojo.tr.KuyrukYöntemleri
    with kojo.tr.YazıYöntemleri {
  // clearing
  def sil(): Unit      = englishTurtle.clear()
  def temizle(): Unit  = englishTurtle.cleari()
  def görünür(): Unit  = englishTurtle.visible()
  def görünmez(): Unit = englishTurtle.invisible()

  // moving and turning
  def ileri(adım: Double): Unit                = englishTurtle.forward(adım)
  def ileri(): Unit                            = englishTurtle.forward()
  def geri(adım: Double): Unit                 = englishTurtle.back(adım)
  def geri(): Unit                             = englishTurtle.back()
  def sağ(açı: Double, yarıçap: Double): Unit  = englishTurtle.right(açı, yarıçap)
  def sağ(açı: Double): Unit                   = englishTurtle.right(açı)
  def sağ(): Unit                              = englishTurtle.right()
  def sol(açı: Double, yarıçap: Double): Unit  = englishTurtle.left(açı, yarıçap)
  def sol(açı: Double): Unit                   = englishTurtle.left(açı)
  def sol(): Unit                              = englishTurtle.left()
  def dön(açı: Double, yarıçap: Double): Unit  = englishTurtle.turn(açı, yarıçap)
  def dön(açı: Double): Unit                   = englishTurtle.turn(açı)
  def zıpla(adım: Double): Unit                = englishTurtle.hop(adım)
  def zıpla(): Unit                            = englishTurtle.hop()
  def atla(x: Double, y: Double): Unit         = englishTurtle.setPosition(x, y)
  def ilerle(x: Double, y: Double): Unit       = englishTurtle.moveTo(x, y)
  def noktayaGit(x: Double, y: Double): Unit   = englishTurtle.lineTo(x, y)
  def açıyaDön(açı: Double): Unit              = englishTurtle.setHeading(açı)
  def doğu(): Unit                             = englishTurtle.setHeading(0)
  def batı(): Unit                             = englishTurtle.setHeading(180)
  def kuzey(): Unit                            = englishTurtle.setHeading(90)
  def güney(): Unit                            = englishTurtle.setHeading(-90)
  def konumVeYönüBelleğeYaz(): Unit            = englishTurtle.savePosHe()
  def konumVeYönüGeriYükle(): Unit             = englishTurtle.restorePosHe()

  // pen
  def kalemiİndir(): Unit                      = englishTurtle.penDown()
  def kalemiKaldır(): Unit                     = englishTurtle.penUp()
  def kalemRenginiKur(renk: Renk): Unit       = englishTurtle.setPenColor(renk)
  def boyamaRenginiKur(renk: Renk): Unit      = englishTurtle.setFillColor(renk)
  def kalemKalınlığınıKur(kalınlık: Double): Unit = englishTurtle.setPenThickness(kalınlık)

  // shapes and text
  def yay(yarıçap: Double, açı: Double): Unit  = englishTurtle.arc(yarıçap, açı)
  def daire(yarıçap: Double): Unit             = englishTurtle.circle(yarıçap)
  def yazı(metin: String): Unit                = englishTurtle.write(metin)
  def yazıBoyunuKur(boy: Int): Unit            = englishTurtle.setPenFontSize(boy)

  // speed and waiting
  def hızıKur(hız: Speed.Speed): Unit          = englishTurtle.setSpeed(hız)
  def canlandırmaHızınıKur(n: Long): Unit      = englishTurtle.setAnimationDelay(n)
  def bekle(saniye: Double): Unit              = englishTurtle.pause(saniye)

  // repeating
  def yinele(n: Int)(komutlar: => Unit): Unit = RepeatCommands.repeat(n)(komutlar)

  // speeds
  lazy val yavaş    = Speed.slow
  lazy val orta     = Speed.medium
  lazy val hızlı    = Speed.fast
  lazy val çokHızlı = Speed.superFast


  // sketches (Processing-style canvas drawing): the same names as on the desktop, see tr/eskiz.scala
  def canlandırTuvalÇizimle(görüntüyüÇiz: TuvalÇizim => Unit): Unit = builtins.animateWithCanvasDraw(görüntüyüÇiz)
  def canlandırKurulumluTuvalÇizimle(kurulum: TuvalÇizim => Unit)(görüntüyüÇiz: TuvalÇizim => Unit): Unit =
    builtins.animateWithSetupCanvasDraw(kurulum)(görüntüyüÇiz)
  def tuvalEskizi(eskiz: Eskiz, ölçekÇarpanı: Double = 1): Unit =
    new SketchPic(eskiz.kurulum, eskiz.çizimDöngüsü, ölçekÇarpanı, builtins.setRandomSeed).draw()

  // games and interaction (the key codes are in tr/klavye.scala)
  def tuşBasılıMı(tuşKodu: Int): Boolean = builtins.isKeyPressed(tuşKodu)
  def canlandır(işlev: => Unit): Unit = builtins.animate(işlev)
  def canlandırmayıDurdur(): Unit = builtins.stopAnimation()

  // output: the Turkish names of println and print
  /**
   * NOT `Predef.println`/`print`: in Scala.js those go to `console.log`, not to the output panel of the page,
   * and a learner who wrote `satıryaz("merhaba")` saw nothing on the screen.
   *
   * The `fiddle.Fiddle` that writes to the panel lives in kojojs-core's page and is not in this repository,
   * so the DOM contract that it uses (the element with id="output") is written to directly -- in the same way
   * as `KojoWorldImpl` is tied directly to fiddle-container and canvas-holder. If the panel is not found (a run
   * outside the browser, tests) it falls back to the console.
   */
  def satıryaz(): Unit = satıryaz("")
  def satıryaz(veri: Any): Unit = writeToPanel(veri, newline = true)
  def yaz(veri: Any): Unit = writeToPanel(veri, newline = false)

  /**
   * The open line (one that has not been given its line end yet). `yaz` adds to it, `satıryaz` adds and
   * CLOSES it -- like print/println on a terminal.
   */
  private var openLine: org.scalajs.dom.Element = null

  /**
   * The text to write. `String.valueOf` for almost everything, but not for `Aralık`: it is a type alias, so
   * `toString` cannot be overridden and Scala's own form would come out -- "inexact Range 1 until 200 by 7".
   * The form for learners is `Aralık.gösterim`.
   */
  private def display(veri: Any): String = veri match {
    case r: Range => Aralık.gösterim(r)
    case _        => String.valueOf(veri)
  }

  private def writeToPanel(veri: Any, newline: Boolean): Unit = {
    val document = org.scalajs.dom.document
    val panel = document.getElementById("output")
    val text = display(veri)
    if (panel == null) {
      if (newline) println(text) else print(text)
    }
    else {
      // Is the open line still the LAST child of the panel? If not, someone else has written in between
      // (fiddle.Fiddle.println adds its own div, a clear empties the panel) and adding to the old one would
      // put the text in the wrong place.
      if (openLine == null || !(panel.lastChild eq openLine)) {
        openLine = document.createElement("div")
        panel.appendChild(openLine)
      }
      openLine.appendChild(document.createTextNode(text))
      if (newline) {
        // An empty div has zero height: satıryaz() did not show a blank line.
        if (openLine.textContent.isEmpty) openLine.appendChild(document.createTextNode("\u00a0"))
        openLine = null
      }
      panel.scrollTop = panel.scrollHeight - panel.clientHeight
    }
  }
}
