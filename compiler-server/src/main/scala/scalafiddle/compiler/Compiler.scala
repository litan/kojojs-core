package scalafiddle.compiler

import java.nio.charset.StandardCharsets

import org.scalajs.linker.interface._
import org.scalajs.linker.{MemOutputDirectory, StandardImpl}
import org.scalajs.linker.standard.MemIRFileImpl
import org.scalajs.logging.{Level, Logger}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.reflect.internal.util.Position
import scala.reflect.io
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.StoreReporter
import scalafiddle.compiler.cache.{AutoCompleteCache, CompilerCache, LinkerCache}
import scalafiddle.shared.ExtLib

/**
  * Handles the interaction between scala-js-fiddle and
  * scalac/scalajs-linker to compile and optimize code submitted by users.
  *
  * scalajs-tools (0.6) -> scalajs-linker (1.x). Visible differences:
  *  - IR files are now linker-interface IRFile instead of VirtualScalaJSIRFile.
  *  - The linker output is read from a MemOutputDirectory; link() returns a Future.
  *  - Closure is only used in fullOpt (withClosureCompilerIfAvailable).
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
        GlobalInitCompat.initInteractiveGlobal(settings, new StoreReporter(settings), libManager.compilerLibraries(extLibs))
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

  def compile(logger: String => Unit = _ => ()): (String, Option[Seq[IRFile]]) = {

    val startTime = System.nanoTime()
    log.debug("Compiling source:\n" + code)
    val singleFile = makeFile(code.getBytes("UTF-8"))

    val vd = new io.VirtualDirectory("(memory)", None)

    val compiler = CompilerCache.getOrUpdate(
      extLibs, {
        val settings = new Settings
        GlobalInitCompat.initGlobal(settings, new StoreReporter(settings), libManager.compilerLibraries(extLibs))
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
          x <- vd.iterator.toSeq
          if x.name.endsWith(".sjsir")
        } yield {
          new MemIRFileImpl(x.path, org.scalajs.ir.Version.Unversioned, x.toByteArray): IRFile
        }
        (errors, Some(things))
      }
    } catch {
      case e: Throwable =>
        CompilerCache.remove(extLibs)
        throw e
    }
  }

  def fastOpt(userFiles: Seq[IRFile]): String =
    link(userFiles, fullOpt = false)

  def fullOpt(userFiles: Seq[IRFile]): String =
    link(userFiles, fullOpt = true)

  def link(userFiles: Seq[IRFile], fullOpt: Boolean): String = {
    implicit val ec = ExecutionContext.global
    val semantics =
      if (fullOpt) Semantics.Defaults.optimized
      else Semantics.Defaults

    // add parameters as fake libraries to make caching work correctly
    val libs = extLibs + ExtLib("semantics", "optimized", fullOpt.toString, false)

    try {
      val linker = LinkerCache.getOrUpdate(
        libs,
        StandardImpl.linker(
          StandardConfig()
            .withSemantics(semantics)
            .withModuleKind(ModuleKind.NoModule)
            .withSourceMap(false)
            .withClosureCompilerIfAvailable(fullOpt)
        )
      )
      val output = MemOutputDirectory()
      val report = Await.result(
        linker.link(libManager.linkerLibraries(extLibs) ++ userFiles, Nil, output, sjsLogger),
        5.minutes
      )
      val fileName = report.publicModules.headOption
        .map(_.jsFileName)
        .getOrElse(throw new Exception("Linker produced no public module"))
      val content = output
        .content(fileName)
        .getOrElse(throw new Exception(s"Linker output $fileName not found"))
      new String(content, StandardCharsets.UTF_8)
    } catch {
      case e: Throwable =>
        LinkerCache.remove(libs)
        throw e
    }
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

    def trace(t: => Throwable): Unit = {
      self.log.error("Compilation error", t)
    }
  }

}
