package scalafiddle.compiler

import org.scalajs.core.tools.io._
import org.scalajs.core.tools.linker.Linker
import org.scalajs.core.tools.linker.backend.{ModuleKind, OutputMode}
import org.scalajs.core.tools.logging._
import org.scalajs.core.tools.sem.Semantics
import org.slf4j.LoggerFactory

import scala.reflect.internal.util.Position
import scala.reflect.io
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.StoreReporter
import scalafiddle.compiler.cache.{AutoCompleteCache, CompilerCache, LinkerCache}
import scalafiddle.shared.ExtLib

/**
  * Handles the interaction between scala-js-fiddle and
  * scalac/scalajs-tools to compile and optimize code submitted by users.
  */
class Compiler(libManager: LibraryManager, code: String) { self =>
  val log               = LoggerFactory.getLogger(getClass)
  val sjsLogger         = new Log4jLogger()
  val blacklist         = Set("<init>")
  val dependencyRE      = """ *// \$FiddleDependency (.+)""".r
  private val codeLines = code.replaceAll("\r", "").split('\n')
  val extLibDefs = codeLines.collect {
    case dependencyRE(dep) => dep
  }.toSet

  lazy val extLibs = {
    val userLibs = extLibDefs
      .map(lib => ExtLib(lib))
      .collect {
        case lib if libManager.depLibs.contains(lib) => lib
        case lib                                     => throw new IllegalArgumentException(s"Library $lib is not allowed")
      }
      .toList

    log.debug(s"Full dependencies: $userLibs")
    userLibs.toSet
  }

  /**
    * Converts a bunch of bytes into Scalac's weird VirtualFile class
    */
  def makeFile(src: Array[Byte]) = {
    val singleFile = new io.VirtualFile("ScalaFiddle.scala")
    val output     = singleFile.output
    output.write(src)
    output.close()
    singleFile
  }

  def autocomplete(pos: Int): List[(String, String)] = {
    val startTime = System.nanoTime()
    val compiler = AutoCompleteCache.getOrUpdate(
      extLibs, {
        val vd       = new io.VirtualDirectory("(memory)", None)
        val settings = new Settings
        settings.outputDirs.setSingleOutput(vd)
        settings.processArgumentString("-Ypresentation-any-thread")
        GlobalInitCompat.initInteractiveGlobal(settings, new StoreReporter, libManager.compilerLibraries(extLibs))
      }
    )

    compiler.reporter.reset()
    val startOffset = pos
    val source      = code.take(startOffset) + "_CURSOR_ " + code.drop(startOffset)
    val run         = new compiler.TyperRun
    val unit        = compiler.newCompilationUnit(source, "ScalaFiddle.scala")
    val richUnit    = new compiler.RichCompilationUnit(unit.source)
    //log.debug(s"Source: ${source.take(startOffset)}${scala.Console.RED}|${scala.Console.RESET}${source.drop(startOffset)}")
    compiler.unitOfFile(richUnit.source.file) = richUnit
    val results = compiler.completionsAt(richUnit.position(startOffset)).matchingResults()

    val endTime = System.nanoTime()
    log.debug(s"AutoCompletion time: ${(endTime - startTime) / 1000} us")
    // log.debug(s"AutoCompletion results: ${results.take(20)}")

    results.map(r => (r.sym.signatureString, r.symNameDropLocal.decoded)).distinct
  }

  def compile(logger: String => Unit = _ => ()): (String, Option[Seq[VirtualScalaJSIRFile]]) = {

    val startTime = System.nanoTime()
    log.debug("Compiling source:\n" + code)
    val singleFile = makeFile(code.getBytes("UTF-8"))

    val vd = new io.VirtualDirectory("(memory)", None)

    val compiler = CompilerCache.getOrUpdate(
      extLibs, {
        val settings = new Settings
        settings.processArgumentString("-Ydebug")
        GlobalInitCompat.initGlobal(settings, new StoreReporter, libManager.compilerLibraries(extLibs))
      }
    )

    compiler.reporter.reset()
    compiler.settings.outputDirs.setSingleOutput(vd)
    try {
      val run = new compiler.Run()
      run.compileFiles(List(singleFile))

      val endTime = System.nanoTime()
      log.debug(s"Scalac compilation: ${(endTime - startTime) / 1000} us")
      // print errors
      val errors = compiler.reporter
        .asInstanceOf[StoreReporter]
        .infos
        .map { info =>
          val label = info.severity.toString match {
            case "ERROR"   => "error: "
            case "WARNING" => "warning: "
            case "INFO"    => ""
          }
          Position.formatMessage(info.pos, label + info.msg, false)
        }
        .mkString("\n")
      if (vd.iterator.isEmpty) {
        (errors, None)
      } else {
        val things = for {
          x <- vd.iterator.to[collection.immutable.Traversable]
          if x.name.endsWith(".sjsir")
        } yield {
          val f = new MemVirtualSerializedScalaJSIRFile(x.path)
          f.content = x.toByteArray
          f: VirtualScalaJSIRFile
        }
        (errors, Some(things.toSeq))
      }
    } catch {
      case e: Throwable =>
        CompilerCache.remove(extLibs)
        throw e
    }
  }

  def export(output: VirtualJSFile): String =
    output.content

  def fastOpt(userFiles: Seq[VirtualScalaJSIRFile]): VirtualJSFile =
    link(userFiles, fullOpt = false)

  def fullOpt(userFiles: Seq[VirtualScalaJSIRFile]): VirtualJSFile =
    link(userFiles, fullOpt = true)

  def link(userFiles: Seq[VirtualScalaJSIRFile], fullOpt: Boolean): VirtualJSFile = {
    val semantics =
      if (fullOpt) Semantics.Defaults.optimized
      else Semantics.Defaults

    // add parameters as fake libraries to make caching work correctly
    val libs = extLibs + ExtLib("semantics", "optimized", fullOpt.toString, false)

    val output = WritableMemVirtualJSFile("output.js")
    try {
      val linker =
        LinkerCache.getOrUpdate(libs,
                                Linker(semantics,
                                       OutputMode.Default,
                                       ModuleKind.NoModule,
                                       Linker.Config().withSourceMap(false).withClosureCompiler(fullOpt)))
      linker.link(libManager.linkerLibraries(extLibs) ++ userFiles, Nil, output, sjsLogger)
    } catch {
      case e: Throwable =>
        LinkerCache.remove(libs)
        throw e
    }
    output
  }

  def getLog = sjsLogger.logLines

  def getInternalLog = sjsLogger.internalLogLines

  class Log4jLogger(minLevel: Level = Level.Debug) extends Logger {
    var logLines         = Vector.empty[String]
    var internalLogLines = Vector.empty[String]

    def log(level: Level, message: => String): Unit = if (level >= minLevel) {
      if (level == Level.Warn || level == Level.Error) {
        logLines :+= message
      }
      internalLogLines :+= message
    }

    def success(message: => String): Unit = info(message)
    def trace(t: => Throwable): Unit = {
      self.log.error("Compilation error", t)
    }
  }

}
