package scalafiddle.router

import upickle.default._

/** Ask the CompilerManager for a snapshot of its state (read only, changes nothing). */
case object GetStatus

/**
  * One registered compiler.
  *
  * `lastActivitySeconds` is the time spent in the current `state` (it is written when the state
  * changes, a ping does not touch it). `lastSeenSeconds` is refreshed by every ping and answers
  * "is the process alive". The failure to look for is a compiler that is alive and pinging but
  * stuck in Compiling: high `lastActivitySeconds`, low `lastSeenSeconds`, `state` = "Compiling".
  */
case class CompilerStatus(id: String, scalaVersion: String, state: String, lastActivitySeconds: Long, lastSeenSeconds: Long)

object CompilerStatus { implicit val rw: ReadWriter[CompilerStatus] = macroRW }

case class RouterStatus(registered: Int,
                        ready: Int,
                        compiling: Int,
                        initializing: Int,
                        queued: Int,
                        pending: Int,
                        compilers: Seq[CompilerStatus])

object RouterStatus { implicit val rw: ReadWriter[RouterStatus] = macroRW }

object Health {

  /**
    * Is there enough compiler capacity? The measure is `registered`, not `ready`: a compiler that
    * is busy compiling is not lost. What a health check looks for is "a compiler process died and
    * did not come back", which shows as the registration itself disappearing.
    */
  def ok(status: RouterStatus, min: Int): Boolean = status.registered >= min
}
