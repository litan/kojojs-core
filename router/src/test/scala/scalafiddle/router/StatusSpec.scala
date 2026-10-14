package scalafiddle.router

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import upickle.default._

class StatusSpec extends AnyFunSuite with Matchers {
  private def status(registered: Int, ready: Int = 0) =
    RouterStatus(registered, ready, registered - ready, 0, 0, 0, Nil)

  test("health: enough registered compilers is healthy") {
    Health.ok(status(2), 2) shouldBe true
    Health.ok(status(3), 2) shouldBe true
  }

  test("health: too few registered compilers is not healthy") {
    Health.ok(status(1), 2) shouldBe false
    Health.ok(status(0), 1) shouldBe false
  }

  test("health: a busy compiler still counts, only a missing registration does not") {
    // 2 registered, none Ready (both compiling): not lost, so healthy
    Health.ok(status(registered = 2, ready = 0), 2) shouldBe true
  }

  test("status survives a JSON round trip") {
    val s = RouterStatus(2, 1, 1, 0, 3, 1, Seq(CompilerStatus("a", "2.13", "Ready", 4, 1), CompilerStatus("b", "2.13", "Compiling", 90, 2)))
    read[RouterStatus](write(s)) shouldBe s
  }
}
