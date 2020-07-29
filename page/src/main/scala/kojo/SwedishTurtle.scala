package kojo

class SwedishTurtle(val englishTurtle: Turtle) {
  def sudda() = englishTurtle.clear()

  def fram(steg: Double) = englishTurtle.forward(steg)

  def fram() = englishTurtle.forward(25)

  def höger(vinkel: Double) = englishTurtle.right(vinkel)

  def höger() = englishTurtle.right(90)

  def vänster(vinkel: Double) = englishTurtle.left(vinkel)

  def vänster() = englishTurtle.left(90)

  def hoppa(steg: Double) = englishTurtle.hop(steg)

  def hoppa() = englishTurtle.hop(25)

  def sakta(n: Long) = englishTurtle.setAnimationDelay(n)

  //loops in Swedish
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
}
