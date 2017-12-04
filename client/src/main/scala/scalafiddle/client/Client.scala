package scalafiddle.client

import java.util.UUID

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw._

import scala.async.Async.{async, await}
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSGlobal}
import scala.scalajs.js.timers.{SetIntervalHandle, SetTimeoutHandle}
import scala.scalajs.js.typedarray.Uint8Array
import scala.scalajs.niocharset.StandardCharsets
import scala.util.Success
import scalafiddle.shared.{CompilationResponse, CompletionResponse, EditorAnnotation}

@JSGlobal("Zlib.Gzip")
@js.native
class Gzip(data: js.Array[Byte]) extends js.Object {
  def compress(): Uint8Array = js.native
}

@JSGlobal("sha1")
@js.native
object SHA1 extends js.Object {
  def apply(s: String): String = js.native
}

case class SourceFile(name: String,
                      code: String,
                      prefix: List[String] = Nil,
                      postfix: List[String] = Nil,
                      indent: Int = 0,
                      fiddleId: Option[String] = None,
                      id: String = UUID.randomUUID().toString)

@js.native
trait EditorAnnotationJS extends js.Object {
  val row: Int
  val col: Int
  val text: js.Array[String]
  val tpe: String
}

@js.native
trait CompilationResponseJS extends js.Object {
  val jsCode: js.Array[String]
  val annotations: js.Array[EditorAnnotationJS]
  val log: String
}

@js.native
trait CompletionResponseJS extends js.Object {
  val completions: js.Array[js.Array[String]]
}

class Client(editURL: String) {
  implicit val RedLogger = scalafiddle.client.Client.RedLogger
  var sourceFiles        = Seq(SourceFile("ScalaFiddle.scala", ""))
  var currentSource      = sourceFiles.head.id
  var currentSourceName  = sourceFiles.head.name

  def currentSourceFile = sourceFiles.find(_.id == currentSource).get

  val runIcon           = dom.document.getElementById("run-icon").asInstanceOf[HTMLElement]
  val resetIcon         = dom.document.getElementById("reset-icon").asInstanceOf[HTMLElement]
  def codeFrame         = dom.document.getElementById("codeframe").asInstanceOf[HTMLIFrameElement]
  val fiddleSelectorDiv = dom.document.getElementById("fiddleSelectorDiv").asInstanceOf[HTMLElement]
  val fiddleSelector    = dom.document.getElementById("fiddleSelector").asInstanceOf[HTMLSelectElement]
  val editLink          = dom.document.getElementById("editLink").asInstanceOf[HTMLAnchorElement]

  editLink.onclick = editClicked _

  def editClicked(e: MouseEvent): Unit = {
    val link = editURL + s"?zrc=${encodeSource(reconstructSource(editor.code, currentSourceFile))}"
    dom.window.open(link, target = "_blank")
    e.stopPropagation()
  }

  def exec(s: String): Unit = {
    Client.clear()

    showStatus("RUNNING")
    js.timers.setTimeout(20) {
      Client.sendFrameCmd("code", s)
    }
  }

  val editor: Editor = new Editor(
    Seq(
      ("Compile", "Enter", () => fastOpt()),
      ("FullOptimize", "Shift-Enter", () => fullOpt()),
      ("Complete", "Space", () => editor.complete()),
      ("Parse", "Alt-Enter", parseFull _)
    ),
    complete _,
    RedLogger
  )

  def beginCompilation(): Future[Unit] = {
    // fully clear the code iframe by reloading it
    val p = Promise[Unit]
    codeFrame.onload = (e: Event) => {
      runIcon.classList.add("active")
      showStatus("COMPILING")
      p.complete(Success(()))
    }
    codeFrame.src = codeFrame.src
    p.future
  }

  def endCompilation(): Unit = {
    runIcon.classList.remove("active")
    showStatus("RESULT")
    editor.focus()
  }

  def showError(errStr: String): Unit = {
    showStatus("Errors")
    Client.clear()
    Client.printOutput(Client.tag("pre")(errStr, Map("class" -> "error")))
  }

  def reconstructSource(source: String, srcFile: SourceFile): String = {
    val indented = source.split("\n").map(l => (" " * srcFile.indent) + l).map(_ + "\n").mkString
    // use map instead of mkString to prevent an empty list from generating a single line
    srcFile.prefix.map(_ + "\n").mkString + indented + "\n" + srcFile.postfix.map(_ + "\n").mkString
  }

  def encodeSource(source: String): String = {
    import com.github.marklister.base64.Base64._
    import js.JSConverters._

    implicit def scheme: B64Scheme = base64Url
    val fullSource                 = source.getBytes(StandardCharsets.UTF_8)
    val compressedBuffer           = new Gzip(fullSource.toJSArray).compress()
    val compressedSource           = new Array[Byte](compressedBuffer.length)
    var i                          = 0
    while (i < compressedBuffer.length) {
      compressedSource(i) = compressedBuffer.get(i).toByte
      i += 1
    }
    Encoder(compressedSource).toBase64
  }

  def readCompilationResponse(jsonStr: String): CompilationResponse = {
    val r = js.JSON.parse(jsonStr).asInstanceOf[CompilationResponseJS]
    CompilationResponse(
      if (r.jsCode.isEmpty) None else Some(r.jsCode(0)),
      r.annotations.map { a =>
        EditorAnnotation(a.row, a.col, a.text, a.tpe)
      },
      r.log
    )
  }

  def readCompletionResponse(jsonStr: String): CompletionResponse = {
    val r = js.JSON.parse(jsonStr).asInstanceOf[CompletionResponseJS]
    CompletionResponse(
      r.completions.map(c => (c(0), c(1))).toList
    )
  }

  def compileServer(code: String, opt: String): Future[CompilationResponse] = {
    val tag        = s"${if (Client.initializing) "initial-" else ""}$opt"
    val fullSource = reconstructSource(code, currentSourceFile)

    val startTime = System.nanoTime()
    val compResp: Future[Option[CompilationResponse]] = if (Client.initializing) {
      // first try loading the compilation result using SHA1 hash code only
      val sha1 = SHA1(fullSource).toUpperCase
      Ajax
        .get(
          url = s"compileResult?opt=$opt&sourceSHA1=$sha1"
        )
        .map { res =>
          val compileTime = (System.nanoTime() - startTime) / 1000000
          js.timers.setTimeout(500)(EventTracker.sendEvent("cachedCompile", tag, currentSourceName, compileTime))
          Some(readCompilationResponse(res.responseText))
        } recover {
        case _: dom.ext.AjaxException =>
          None
      }
    } else Future.successful(None)
    compResp.flatMap {
      case Some(r) =>
        Future.successful(r)
      case None =>
        // post actual source code and get compilation result
        Ajax
          .post(
            url = s"compile?opt=$opt",
            data = fullSource
          )
          .map { res =>
            val compileTime = (System.nanoTime() - startTime) / 1000000
            js.timers.setTimeout(500)(EventTracker.sendEvent("compile", tag, currentSourceName, compileTime))
            readCompilationResponse(res.responseText)
          } recover {
          case e: dom.ext.AjaxException =>
            showError(s"Error: ${e.xhr.responseText}")
            throw e
          case e: Throwable =>
            showError(e.toString)
            throw e
        }
    }
  }

  def performCompile(opt: String): Future[Unit] = {
    val cleared              = beginCompilation()
    val pendingCompileResult = compileServer(editor.code, opt)
    for {
      _         <- cleared
      jsCodeOpt <- processCompilationResponse(pendingCompileResult)
    } yield {
      jsCodeOpt.foreach(exec)
    }
  }

  def fullOpt(): Future[Unit] = performCompile("full")

  def fastOpt(): Future[Unit] = performCompile("fast")

  def parseFull() = {
    Client.clear()
    Client.printOutput(Client.tag("h2")("Full source code"))
    Client.printOutput(
      Client.tag("pre")(reconstructSource(editor.code, currentSourceFile).replace("&", "&amp;") replace ("<", "&lt;")))
  }

  // attach handlers to icons
  if (runIcon != null) {
    runIcon.onclick = (e: MouseEvent) => {
      if (e.shiftKey)
        fullOpt()
      else
        fastOpt()
    }
  }

  if (resetIcon != null) {
    resetIcon.onclick = (e: MouseEvent) => {
      EventTracker.sendEvent("reset", "reset", currentSourceName)
      selectSource(currentSource)
      editor.focus()
    }
  }

  fiddleSelector.onchange = (e: Event) => {
    val sel = fiddleSelector.options(fiddleSelector.selectedIndex).value
    dom.console.log(s"Fiddle selected $sel")
    updateSource(editor.code)
    selectSource(sel)
  }

  def showStatus(status: String) = {
    Client.sendFrameCmd("label", status)
  }

  val fiddleStart = """\s*// \$FiddleStart\s*$""".r
  val fiddleEnd   = """\s*// \$FiddleEnd\s*$""".r

  // separate source code into pre,main,post blocks
  def extractCode(src: SourceFile): SourceFile = {
    val lines = src.code.split('\n')
    val (pre, main, post) = lines.foldLeft((List.empty[String], List.empty[String], List.empty[String])) {
      case ((preList, mainList, postList), line) =>
        line match {
          case fiddleStart() =>
            (line :: mainList ::: preList, Nil, Nil)
          case fiddleEnd() =>
            (preList, mainList, line :: postList)
          case l if postList.nonEmpty =>
            (preList, mainList, line :: postList)
          case _ =>
            (preList, line :: mainList, postList)
        }
    }
    // remove indentation from main part
    val indent = main.filter(_.nonEmpty).map(_.takeWhile(_ == ' ').length) match {
      case l if l.nonEmpty => l.min
      case _               => 0
    }
    src.copy(code = main.reverse.map(_.drop(indent)).mkString("", "\n", "\n"),
             prefix = pre.reverse,
             postfix = post.reverse,
             indent = indent)
  }

  def setSources(sources: Seq[SourceFile]): Unit = {
    sourceFiles = sources.map(extractCode)
    fiddleSelector.innerHTML = sourceFiles
      .map { source =>
        Client.tag("option")(source.name, Map("value" -> source.id))
      }
      .mkString("")
    if (sourceFiles.size > 1) {
      fiddleSelectorDiv.style.display = "inherit"
    } else {
      fiddleSelectorDiv.style.display = "none"
    }
    selectSource(sourceFiles.head.id)
  }

  def selectSource(id: String): Unit = {
    sourceFiles.find(_.id == id).foreach { src =>
      currentSource = src.id
      currentSourceName = src.name
      editor.sess.setValue(src.code)
    /*
      editLink.href = src.fiddleId match {
        case Some(fiddleId) => editURL + "sf/" + fiddleId
        case None           => editURL + s"?zrc=${encodeSource(reconstructSource(editor.code, currentSourceFile))}"
      }
     */
    }
  }

  def updateSource(code: String): Unit = {
    sourceFiles = sourceFiles.collect {
      case sf: SourceFile if sf.id == currentSource => sf.copy(code = code)
      case sf                                       => sf
    }
  }

  def processCompilationResponse(res: Future[CompilationResponse]): Future[Option[String]] = {
    res
      .map { response =>
        endCompilation()
        val prefixLines = currentSourceFile.prefix.size
        editor.setAnnotations(response.annotations.map(a => a.copy(row = a.row - prefixLines)))
        if (response.jsCode.isEmpty) {
          // show compiler errors in output
          val allErrors = response.annotations
            .map { ann =>
              s"ScalaFiddle.scala:${ann.row + 1 - prefixLines}: ${ann.tpe}: ${ann.text.mkString("\n")}"
            }
            .mkString("\n")
          showError(allErrors)
        }
        response.jsCode
      }
      .recover {
        case e: Exception =>
          endCompilation()
          Client.logError(e.toString)
          None
      }
  }

  def complete(): Future[CompletionResponse] = async {
    val code = reconstructSource(editor.code, currentSourceFile)
    val row  = editor.row + currentSourceFile.prefix.size
    val col  = editor.column + currentSourceFile.indent
    val intOffset = col + code
      .split("\n")
      .take(row)
      .map(_.length + 1)
      .sum

    val startTime = System.nanoTime()

    val f = Ajax
      .post(
        url = s"complete?offset=$intOffset",
        data = code
      )
      .map { res =>
        val completeTime = (System.nanoTime() - startTime) / 1000000
        js.timers.setTimeout(500)(EventTracker.sendEvent("complete", "complete", currentSourceName, completeTime))
        readCompletionResponse(res.responseText)
      } recover {
      case e: dom.ext.AjaxException =>
        showError(s"Error: ${e.xhr.responseText}")
        throw e
      case e: Throwable =>
        showError(e.toString)
        throw e
    }

    val res = await(f)
    res
  }
}

@JSExportTopLevel("Client")
object Client {
  implicit val RedLogger = new Logger(logError)

  var intervalHandles = List.empty[SetIntervalHandle]
  var timeoutHandles  = List.empty[SetTimeoutHandle]

  dom.window.onerror = { (event: dom.Event, source: String, fileno: Int, columnNumber: Int, x: Any) =>
    dom.console.log("dom.onerror")
    Client.logError(event.toString)
  }

  // listen to messages from the iframe
  dom.window.addEventListener("message", (e: MessageEvent) => {
    sendFrameCmd("label", "RESULT")
  })

  def parseUriParameters(search: String): Map[String, String] = {
    search
      .drop(1)
      .split('&')
      .filter(_.nonEmpty)
      .map { part =>
        val pair  = part.split("=")
        val key   = js.URIUtils.decodeURIComponent(pair(0))
        val value = if (pair.length > 1) js.URIUtils.decodeURIComponent(pair(1)) else ""
        key -> value
      }
      .toMap
  }

  var initializing   = true
  val queryParams    = parseUriParameters(dom.window.location.search)
  val previewMode    = queryParams.contains("preview")
  val envId          = queryParams.getOrElse("env", "default")
  lazy val codeFrame = dom.document.getElementById("codeframe").asInstanceOf[HTMLIFrameElement]

  dom.console.log(s"queryParams: $queryParams")

  private[Client] def tag(name: String)(content: String, attrs: Map[String, String] = Map.empty): String = {
    s"""<$name${attrs.map { case (a, v) => s"""$a="$v"""" }.mkString(" ", " ", "")}>$content</$name>"""
  }

  def logError(s: String): Unit = {
    dom.console.error(s)
  }

  def sendFrameCmd(cmd: String, data: String = "") = {
    val msg = js.Dynamic.literal(cmd = cmd, data = data)
    try {
      codeFrame.contentWindow.postMessage(msg, "*")
    } catch {
      case e: Throwable =>
        dom.console.log(s"Error sending message to worker thread: $e")
    }
  }

  def clear() = {
    dom.console.log(s"Clearing ${timeoutHandles.size} timeouts and ${intervalHandles.size} intervals")
    // clear all timers
    timeoutHandles.foreach(js.timers.clearTimeout)
    timeoutHandles = Nil
    intervalHandles.foreach(js.timers.clearInterval)
    intervalHandles = Nil
    sendFrameCmd("clear")
  }

  val subFiddleRE = """// \$SubFiddle (\w.+)""".r
  def parseFiddles(srcCode: String): Seq[SourceFile] = {
    val lines = srcCode.split('\n').toSeq.map(_.replaceAll("[\\n\\r]", ""))
    val (prevSources, lastSource) = lines.foldLeft((List.empty[SourceFile], SourceFile("ScalaFiddle.scala", ""))) {
      case ((acc, src), line) =>
        line match {
          case subFiddleRE(name) =>
            val newSrc = SourceFile(name.trim, "")
            (src :: acc, newSrc)
          case _ =>
            (acc, src.copy(code = src.code + line + "\n"))
        }
    }
    (lastSource :: prevSources).filter(_.code.replaceAll("\\s", "").nonEmpty).reverse
  }

  def defaultCode(id: String) =
    s"""println("There was an error loading fiddle with identifier '$id'")
        |println("Loading an empty application so you can get started")
    """.stripMargin

  @JSExport
  def main(useFull: Boolean,
           scalaFiddleSourceUrl: String,
           scalaFiddleEditUrl: String,
           baseEnv: String,
           passive: Boolean): Unit =
    task * async {
      initializing = true
      clear()
      Editor.initEditor
      val client = new Client(scalaFiddleEditUrl)
      val f: Future[Any] = if (queryParams.contains("sfid")) {
        val ids = queryParams("sfid").split(",").toList
        val sources = await(Future.sequence(ids.map { id =>
          loadSource(scalaFiddleSourceUrl, id) recover {
            case e => SourceFile("ScalaFiddle.scala", baseEnv).copy(code = defaultCode(id))
          }
        }))
        client.setSources(sources)
        if (!passive) {
          client.fullOpt()
        } else Future.successful(())
      } else if (queryParams.contains("source")) {
        val srcCode = queryParams("source")
        client.showStatus("Loading")
        // check for sub-fiddles
        val sources = await(Future(parseFiddles(srcCode)))
        client.setSources(sources)
        if (!passive) {
          client.fullOpt()
        } else Future.successful(())
      } else {
        client.setSources(Seq(SourceFile("ScalaFiddle.scala", baseEnv)))
        Future.successful(())
      }
      f.foreach(_ => initializing = false)
    }

  val fiddleName = """\s*// \$FiddleName\s+(.+)$""".r

  def parseName(source: String): Option[String] = {
    source.split("\n").collectFirst {
      case fiddleName(name) => name
    }
  }

  def loadSource(url: String, id: String): Future[SourceFile] = {
    Ajax.get(url + id).map { res =>
      val source = res.responseText
      val name   = parseName(source).getOrElse("ScalaFiddle")
      SourceFile(name, source, fiddleId = Some(id))
    }
  }

  def printOutput(ss: String) = {
    val html = tag("div")(ss)
    Client.sendFrameCmd("print", html)
  }
}
