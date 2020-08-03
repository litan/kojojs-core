package howlerscalajs.howler

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("Howl")
class Howl(o: js.Dynamic) extends js.Object {
  def play(): Int = js.native
  def stop(id: Int): Unit = js.native
  def playing(): Boolean = js.native
}