package kojo

object RepeatCommands extends RepeatCommands

trait RepeatCommands {
  def repeat(n: Int)(fn: => Unit) {
    var i = 0
    while (i < n) {
      fn
      i += 1
    }
  }

  def repeati(n: Int)(fn: Int => Unit) {
    var i = 0
    while (i < n) {
      fn(i + 1)
      i += 1
    }
  }

  def repeatWhile(cond: => Boolean)(fn: => Unit) {
    while (cond) {
      fn
    }
  }

  def repeatUntil(cond: => Boolean)(fn: => Unit) {
    while (!cond) {
      fn
    }
  }

  def repeatFor[T](seq: Iterable[T])(fn: T => Unit) {
    val iter = seq.iterator
    while (iter.hasNext) {
      fn(iter.next)
    }
  }
}
