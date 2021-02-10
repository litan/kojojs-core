package kojo

class SwedishTurtle(val englishTurtle: Turtle, builtins: syntax.Builtins) {
  import kojo.doodle.Color
  def sudda()  = englishTurtle.clear()
  def synlig() = englishTurtle.visible()
  def osynlig() = englishTurtle.invisible()
  def fram(steg: Double) = englishTurtle.forward(steg)
  def fram() = englishTurtle.forward(25)
  def höger(vinkel: Double) = englishTurtle.right(vinkel)
  def höger() = englishTurtle.right(90)
  def vänster(vinkel: Double) = englishTurtle.left(vinkel)
  def vänster() = englishTurtle.left(90)
  def hoppaTill(x: Double, y: Double) = englishTurtle.setPosition(x, y)
  def gåTill(x: Double, y: Double) = englishTurtle.moveTo(x, y)
  def hoppa(steg: Double) = englishTurtle.hop(steg)
  def hoppa(): Unit = hoppa(25)
  //def hem() = englishTurtle.home()
  //def mot(x: Double, y: Double) = englishTurtle.towards(x, y)
  def sättVinkel(vinkel: Double) = englishTurtle.setHeading(vinkel)
  //def vinkel = englishTurtle.heading
  def öster() = englishTurtle.setHeading(0)
  def väster() = englishTurtle.setHeading(180)
  def norr() = englishTurtle.setHeading(90)
  def söder() = englishTurtle.setHeading(-90)  
  def sakta(n: Long) = englishTurtle.setAnimationDelay(n)
  def skriv(t: String) = englishTurtle.write(t)
  def textstorlek(s: Int) = englishTurtle.setPenFontSize(s)
  def båge(radie: Double, vinkel: Double) = englishTurtle.arc(radie, math.round(vinkel).toInt)
  def cirkel(radie: Double) = englishTurtle.circle(radie)
  //def läge = englishTurtle.position
  def pennaNer() = englishTurtle.penDown()
  def pennaUpp() = englishTurtle.penUp()
  //def pennanÄrNere = englishTurtle.style.down
  def färg(c: Color) = englishTurtle.setPenColor(c)
  def fyll(c: Color) = englishTurtle.setFillColor(c)
  def bredd(n: Double) = englishTurtle.setPenThickness(n)
  //def sparaStil() = englishTurtle.saveStyle()
  //def laddaStil() = englishTurtle.restoreStyle()
  def sparaLägeRiktning() = englishTurtle.savePosHe()
  def laddaLägeRiktning() = englishTurtle.restorePosHe()
  def siktePå() = englishTurtle.beamsOn()
  def sikteAv() = englishTurtle.beamsOff()
  //def kostym(filNamn: String) = englishTurtle.setCostume(filNamn)
  //def kostymer(filNamn: String*) = englishTurtle.setCostumes(filNamn: _*)
  //def nästaKostym() = englishTurtle.nextCostume()

  // **** how to do with multiple SwedishTurtle instances, missing newTurtle(startX, startY, costume) etc
  // class Padda(override val englishTurtle: Turtle) extends SwedishTurtle {
  //   def this(startX: Double, startY: Double, kostymFilNamn: String) = 
  //     this(builtins.TSCanvas.newTurtle(startX, startY, kostymFilNamn))
  //   def this(startX: Double, startY: Double) = this(startX, startY, "/images/turtle32.png")
  //   def this() = this(0,0)
  // }
  // class Padda0(t0: => Turtle) extends SwedishTurtle { //by-name construction as turtle0 is volatile }
  //   override def englishTurtle: Turtle = t0
  // }
  // object padda extends Padda0(builtins.TSCanvas.turtle0)

  // **** builtins should be moved to a singelton and not be inside this instance
  def suddaUtdata() = builtins.clearOutput()
  lazy val blå   = builtins.Color.blue 
  lazy val röd   = builtins.Color.red 
  lazy val gul   = builtins.Color.yellow 
  lazy val grön  = builtins.Color.green 
  lazy val lila  = builtins.Color.purple
  lazy val rosa  = builtins.Color.pink 
  lazy val brun  = builtins.Color.brown 
  lazy val svart = builtins.Color.black 
  lazy val vit   = builtins.Color.white
  lazy val genomskinlig = builtins.noColor
  def bakgrund(färg: Color) = builtins.setBackground(färg)
  def bakgrund2(färg1: Color, färg2: Color) = builtins.setBackgroundV(färg1, färg2)
  object KcSwe { //Key codes for Swedish keys
    lazy val VK_Å = 197
    lazy val VK_Ä = 196
    lazy val VK_Ö = 214
  }

  def upprepa(n: Int)(block: => Unit) {
    RepeatCommands.repeat(n) {
      block
    }
  }

  def räkneslinga(n: Int)(block: Int => Unit) {
    RepeatCommands.repeati(n) { i =>
      block(i)
    }
  }

  def sålänge(villkor: => Boolean)(block: => Unit) {
    RepeatCommands.repeatWhile(villkor) {
      block
    }
  }

   //simple IO
  def indata(ledtext: String = "") =  builtins.readln(ledtext)
  
  //math functions
  def avrunda(tal: Number, antalDecimaler: Int = 0): Double = {
    val faktor = math.pow(10, antalDecimaler).toDouble
    math.round(tal.doubleValue * faktor).toLong / faktor
  }
  def slumptal(n: Int) = builtins.random(n)
  def slumptalMedDecimaler(n: Int) = builtins.randomDouble(n)
  
  //some type aliases in Swedish
  type Heltal = Int
  type Decimaltal = Double
  type Sträng = String
  
  //speedTest
  def systemtid = BigDecimal(System.nanoTime) / BigDecimal("1000000000") //sekunder

  // def räknaTill(n: BigInt): Unit = {
  //   var c: BigInt = 1
  //   print("*** Räknar från 1 till ... ")
  //   val startTid = systemtid
  //   while (c < n) { c = c + 1 } //tar tid om n är stort
  //   val stoppTid = systemtid
  //   println("" + n + " *** KLAR!")
  //   val tid = stoppTid - startTid
  //   print("Det tog ")
  //   if (tid < 0.1)
  //     println((tid * 1000).round(new java.math.MathContext(2)) +
  //       " millisekunder.")
  //   else println((tid * 10).toLong / 10.0 + " sekunder.")
  // }
}
