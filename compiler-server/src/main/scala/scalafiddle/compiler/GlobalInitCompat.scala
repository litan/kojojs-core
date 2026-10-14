package scalafiddle.compiler

import java.net.{URL, URLClassLoader}

import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.reflect.io
import scala.tools.nsc
import scala.tools.nsc.Settings
import scala.tools.nsc.classpath.{AggregateClassPath, FileUtils, VirtualDirectoryClassPath}
import scala.tools.nsc.io.{AbstractFile, VirtualDirectory}
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.util.ClassPath
import scala.tools.util.PathResolver
import scala.util.Try

/**
  * Notes on the Scala 2.13 / Scala.js 1.x version (differences from the 2.12 one):
  *  - A single source file: the scala-2.11/scala-2.12 source directories are gone, the target is 2.13 only.
  *  - The plugin is org.scalajs.nscplugin.ScalaJSPlugin in Scala.js 1.x.
  *  - macro-paradise and kind-projector were removed (not needed on 2.13).
  *  - JDK classes: the old code took java.lang from Java 8's sun.boot.class.path (the LibraryManager
  *    bootFiles); that property does not exist on Java 9+. The platform classpath (jrt) from
  *    PathResolver is now added to the aggregate.
  */
object GlobalInitCompat {
  val log = LoggerFactory.getLogger(getClass)

  private def inMemClassloader(libs: Seq[io.AbstractFile]): ClassLoader = {
    new URLClassLoader(new Array[URL](0), this.getClass.getClassLoader) {
      private val classCache = mutable.Map.empty[String, Option[Class[_]]]

      override def findClass(name: String): Class[_] = {
        def findClassInLibs(): Option[AbstractFile] = {
          val parts = name.split('.')
          libs
            .map(dir => {
              Try {
                parts
                  .dropRight(1)
                  .foldLeft[AbstractFile](dir)((parent, next) => parent.lookupName(next, directory = true))
                  .lookupName(parts.last + ".class", directory = false)
              } getOrElse null
            })
            .find(_ != null)
        }

        val res = classCache.getOrElseUpdate(
          name,
          findClassInLibs().map { f =>
            val data = f.toByteArray
            this.defineClass(name, data, 0, data.length)
          }
        )
        res match {
          case None =>
            log.error("Not Found Class " + name)
            throw new ClassNotFoundException()
          case Some(cls) =>
            cls
        }
      }

      override def close() = {}
    }
  }

  private final def lookupPath(base: AbstractFile)(pathParts: Seq[String], directory: Boolean): AbstractFile = {
    var file: AbstractFile = base
    for (dirPart <- pathParts.init) {
      file = file.lookupName(dirPart, directory = true)
      if (file == null)
        return null
    }

    file.lookupName(pathParts.last, directory = directory)
  }

  private def buildClassPath(absFile: AbstractFile) =
    new VirtualDirectoryClassPath(new VirtualDirectory(absFile.name, None) {
      override def iterator = absFile.iterator

      override def lookupName(name: String, directory: Boolean) = absFile.lookupName(name, directory)

      override def subdirectoryNamed(name: String) = absFile.subdirectoryNamed(name)
    }) {
      override def getSubDir(packageDirName: String): Option[AbstractFile] = {
        Option(lookupPath(absFile)(packageDirName.split('/').toIndexedSeq, directory = true))
      }

      override def findClassFile(className: String): Option[AbstractFile] = {
        val relativePath = FileUtils.dirPath(className) + ".class"
        Option(lookupPath(absFile)(relativePath.split('/').toIndexedSeq, directory = false))
      }
    }

  // The JDK's own classes (java.*), through jrt. Resolved once.
  private lazy val jdkClassPath: Seq[ClassPath] = {
    val settings = new Settings
    // keep the default user classpath ("." = the server's working directory) from leaking
    // into the compiler; only the platform (jrt) entries should remain
    settings.classpath.value = ""
    new PathResolver(settings, new nsc.CloseableRegistry).result match {
      case AggregateClassPath(entries) => entries
      case single                      => List(single)
    }
  }

  private def makeClassPath(libs: Seq[io.AbstractFile]): ClassPath =
    AggregateClassPath(libs.map(buildClassPath) ++ jdkClassPath)

  def initGlobal(settings: Settings, reporter: StoreReporter, libs: Seq[io.AbstractFile]): nsc.Global = {
    val cp = makeClassPath(libs)
    val cl = inMemClassloader(libs)

    new nsc.Global(settings, reporter) { g =>
      override def classPath = cp

      override lazy val plugins = List[Plugin](
        new org.scalajs.nscplugin.ScalaJSPlugin(this)
      )

      override lazy val platform: ThisPlatform = new GlobalPlatform {
        override val global    = g
        override val settings  = g.settings
        override def classPath = cp
      }

      // 2.13: the macro classloader hook moved from Analyzer to Global
      override def findMacroClassLoader(): ClassLoader = cl
    }
  }

  def initInteractiveGlobal(settings: Settings,
                            reporter: StoreReporter,
                            libs: Seq[io.AbstractFile]): nsc.interactive.Global = {
    val cp = makeClassPath(libs)
    new nsc.interactive.Global(settings, reporter) { g =>
      override def classPath = cp

      override lazy val plugins = List[Plugin](
        new org.scalajs.nscplugin.ScalaJSPlugin(this)
      )

      override lazy val platform: ThisPlatform = new GlobalPlatform {
        override val global    = g
        override val settings  = g.settings
        override def classPath = cp
      }

      // 2.13: the macro classloader hook moved from Analyzer to Global
      private val cl = inMemClassloader(libs)
      override def findMacroClassLoader(): ClassLoader = cl
    }
  }
}
