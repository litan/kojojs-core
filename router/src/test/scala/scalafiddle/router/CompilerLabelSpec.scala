package scalafiddle.router

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CompilerLabelSpec extends AnyFunSuite with Matchers {
  import CompilerManager.{baseVersion, forLabel}

  test("a label with a suffix stands for its base version") {
    baseVersion("2.13") shouldBe "2.13"
    baseVersion("2.13-tr") shouldBe "2.13"
  }

  test("a label without an entry of its own takes the entry of its base version") {
    val libs = Map("2.13" -> "libs 2.13")
    forLabel(libs, "2.13") shouldBe Some("libs 2.13")
    forLabel(libs, "2.13-tr") shouldBe Some("libs 2.13")
  }

  test("an entry of the label itself wins over the one of the base version") {
    val libs = Map("2.13" -> "libs 2.13", "2.13-tr" -> "libs tr")
    forLabel(libs, "2.13-tr") shouldBe Some("libs tr")
    forLabel(libs, "2.13") shouldBe Some("libs 2.13")
  }

  test("a version with no entry at all gives None") {
    forLabel(Map("2.13" -> "x"), "3.3-tr") shouldBe None
  }
}
