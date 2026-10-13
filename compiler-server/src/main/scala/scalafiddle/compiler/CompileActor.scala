package scalafiddle.compiler

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.ActorMaterializer
import org.scalajs.linker.interface.IRFile
import upickle.default._

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scalafiddle.shared._

case object WatchPong

class CompileActor(out: ActorRef, manager: ActorRef) extends Actor with ActorLogging {
  import context.dispatcher
  implicit val materializer = ActorMaterializer()(context)

  var timer: Cancellable             = _
  var pongTimer: Cancellable         = _
  var libraryManager: LibraryManager = _
  var lastPong                       = System.currentTimeMillis() / 1000

  override def preStart(): Unit = {
    log.debug("Compiler actor starting")
    timer = context.system.scheduler.schedule(30.seconds, 20.seconds, context.self, Ping)
    pongTimer = context.system.scheduler.schedule(1.minute, 1.minute, context.self, WatchPong)
    super.preStart()
  }

  def sendOut(msg: CompilerMessage): Unit = {
    out ! TextMessage.Strict(write[CompilerMessage](msg))
  }

  def receive = {
    case Ping =>
      sendOut(Ping)

    case WatchPong =>
      val now = System.currentTimeMillis() / 1000
      if (now - lastPong > 60) {
        log.error(s"Have not received Pong from router in ${now - lastPong} seconds, terminating")
        context.stop(self)
      }

    case msg: TextMessage =>
      msg.textStream.runReduce(_ + _).map { text =>
        read[CompilerMessage](text) match {
          case UpdateLibraries(extLibs) =>
            log.debug(s"Received ${extLibs.size} libraries")
            // library loading can take some time, run in another thread
            Future(new LibraryManager(extLibs)).map { mgr =>
              libraryManager = mgr
              sendOut(CompilerReady)
            } recover {
              case e =>
                log.error(e, "Error while loading libraries")
                if (libraryManager == null)
                  context.self ! PoisonPill
            }

          case CompilationRequest(id, sourceCode, _, optimizer) =>
            if (libraryManager == null)
              context.self ! PoisonPill
            val compiler = new Compiler(libraryManager, sourceCode)
            try {
              val opt = optimizer match {
                case "fast" => compiler.fastOpt _
                case "full" => compiler.fullOpt _
              }
              val startTime = System.nanoTime()
              val res       = doCompile(compiler, sourceCode, e => opt(e))
              val endTime   = System.nanoTime()
              log.debug(compiler.getInternalLog.mkString("\n"))
              log.debug(f" ==== Full compilation time: ${(endTime - startTime) / 1.0e6}%.1f ms")
              sendOut(res)
            } catch {
              case e: Throwable =>
                log.error(e, s"Error in compilation")
                log.debug(compiler.getLog.mkString("\n"))
                sendOut(
                  CompilationResponse(None,
                                      Seq(EditorAnnotation(0, 0, e.getMessage +: compiler.getLog, "ERROR")),
                                      compiler.getLog.mkString("\n")))
            }

          case CompletionRequest(id, sourceCode, _, offset) =>
            if (libraryManager == null)
              context.self ! PoisonPill
            val compiler = new Compiler(libraryManager, sourceCode)
            try {
              val startTime = System.nanoTime()
              val res       = CompletionResponse(compiler.autocomplete(offset.toInt))
              val endTime   = System.nanoTime()
              sendOut(res)
            } catch {
              case e: Throwable =>
                sendOut(CompletionResponse(List.empty))
            }

          case Pong =>
            lastPong = System.currentTimeMillis() / 1000

          case other =>
            log.error(s"Unsupported compiler message $other")
        }
      }
  }

  val errorStart = """^\w+.scala:(\d+): *(\w+): *(.*)""".r
  val errorEnd   = """ *\^ *$""".r

  def parseErrors(log: String): Seq[EditorAnnotation] = {
    val lines = log.split('\n').toSeq.map(_.replaceAll("[\\n\\r]", ""))
    val (annotations, _) = lines.foldLeft((Seq.empty[EditorAnnotation], Option.empty[EditorAnnotation])) {
      case ((acc, current), line) =>
        line match {
          case errorStart(lineNo, severity, msg) =>
            val ann = EditorAnnotation(lineNo.toInt - 1, 0, Seq(msg), severity)
            (acc, Some(ann))
          case errorEnd() if current.isDefined =>
            val ann = current.map(ann => ann.copy(col = line.length, text = ann.text :+ line)).get
            (acc :+ ann, None)
          case errLine =>
            (acc, current.map(ann => ann.copy(text = ann.text :+ errLine)))
        }
    }
    annotations
  }

  def doCompile(compiler: Compiler,
                sourceCode: String,
                processor: Seq[IRFile] => String): CompilationResponse = {
    val output = mutable.Buffer.empty[String]

    val (logSpam, res) = compiler.compile(output.append(_))
    if (logSpam.nonEmpty)
      log.debug(s"Compiler errors: $logSpam")

    CompilationResponse(res.map(processor), parseErrors(logSpam), logSpam)
  }

  override def postStop(): Unit = {
    manager ! CompilerTerminated
    timer.cancel()
    pongTimer.cancel()
    super.postStop()
  }
}

object CompileActor {
  def props(out: ActorRef, manager: ActorRef) = Props(new CompileActor(out, manager))
}
