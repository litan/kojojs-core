package scalafiddle.shared

import upickle.default.{macroRW, ReadWriter => RW}

sealed trait CompilerMessage

case object CompilerReady extends CompilerMessage

case object Ping extends CompilerMessage

case object Pong extends CompilerMessage

case class UpdateLibraries(libs: Seq[ExtLib]) extends CompilerMessage

object UpdateLibraries { implicit val rw: RW[UpdateLibraries] = macroRW }

sealed abstract class CompilerRequest {
  def id: String
  def source: String
  def clientAddress: String
  def updated(f: String => String): CompilerRequest
}

case class CompilationRequest(id: String, source: String, clientAddress: String, opt: String)
    extends CompilerRequest
    with CompilerMessage {
  def updated(f: String => String) = copy(source = f(source))
}

object CompilationRequest { implicit val rw: RW[CompilationRequest] = macroRW }

case class CompletionRequest(id: String, source: String, clientAddress: String, offset: Int)
    extends CompilerRequest
    with CompilerMessage {
  def updated(f: String => String) = copy(source = f(source))
}

object CompletionRequest { implicit val rw: RW[CompletionRequest] = macroRW }

trait CompilerResponse

case class EditorAnnotation(row: Int, col: Int, text: Seq[String], tpe: String)

object EditorAnnotation { implicit val rw: RW[EditorAnnotation] = macroRW }

case class CompilationResponse(jsCode: Option[String], annotations: Seq[EditorAnnotation], log: String)
    extends CompilerResponse
    with CompilerMessage

object CompilationResponse { implicit val rw: RW[CompilationResponse] = macroRW }

case class CompletionResponse(completions: List[(String, String)]) extends CompilerResponse with CompilerMessage

object CompletionResponse { implicit val rw: RW[CompletionResponse] = macroRW }

object CompilerMessage {
  // upickle 1.x: explicit ReadWriters replace the fully automatic derivation of 0.4. The wire
  // format is JSON tagged with $type; both ends (router and compilerServer/client) move
  // together, so compatibility with the old format is not needed.
  implicit val readyRw: RW[CompilerReady.type] = macroRW
  implicit val pingRw: RW[Ping.type]           = macroRW
  implicit val pongRw: RW[Pong.type]           = macroRW
  implicit val rw: RW[CompilerMessage]         = macroRW
}
