package scalafiddle.compiler

import java.io._
import java.nio.channels.{FileLock, OverlappingFileLockException}
import java.nio.file.Paths

import org.scalajs.linker.interface.IRFile
import org.scalajs.linker.standard.MemIRFileImpl
import org.slf4j.LoggerFactory

import scala.tools.nsc.io.AbstractFile
import scalafiddle.compiler.cache._
import scalafiddle.shared.ExtLib

/**
  * Loads the jars that make up the classpath of the scala-js-fiddle
  * compiler and re-shapes it into the correct structure to satisfy
  * scala-compile and the Scala.js linker.
  *
  * Differences from the Scala 2.12 / Scala.js 0.6 version:
  *  - coursier 1.0 / scalaz -> coursier 2.x (Fetch API).
  *  - scalajs-tools VirtualJarFile -> linker 1.x IRFile (MemIRFileImpl); the jar name and path
  *    are given as the IRFile version so that the incremental linker does not reprocess the
  *    libraries on every compilation.
  *  - The Java 8 bootFiles hack is gone (see GlobalInitCompat: jrt).
  *  - Libraries are mapped by jar NAME; module conflicts (different versions of the same
  *    module) are resolved to the highest version with ComparableVersion (the equivalent of the
  *    old resolution in coursier 1).
  *  - baseLibs includes the Scala.js 1.15+ stdlib split: javalib and scalalib are separate jars.
  */
class LibraryManager(val depLibs: Seq[ExtLib]) {
  val log     = LoggerFactory.getLogger(getClass)

  val baseLibs = Seq(
    s"/scala-library-${Config.scalaVersion}.jar",
    s"/scala-reflect-${Config.scalaVersion}.jar",
    s"/scalajs-library_${Config.scalaMainVersion}-${Config.scalaJSVersion}.jar",
    s"/scalajs-javalib-${Config.scalaJSVersion}.jar",
    s"/scalajs-scalalib_${Config.scalaMainVersion}-${Config.scalaVersion}+${Config.scalaJSVersion}.jar",
    s"/page_sjs${Config.scalaJSMajorVersion}_${Config.scalaMainVersion}-${Config.version}.jar"
  )

  val sjsVersion = s"_sjs${Config.scalaJSMajorVersion}_${Config.scalaMainVersion}"

  val commonJars: Seq[(String, InputStream)] = {
    log.debug("Loading common libraries...")
    baseLibs.map { name =>
      // the coursier cache encodes '+' as %2B (the scalalib jar name carries a + in
      // its version), so try both names
      val stream = Option(getClass.getResourceAsStream(name))
        .orElse(Option(getClass.getResourceAsStream(name.replace("+", "%2B"))))
        .getOrElse(throw new Exception(s"Classpath loading failed, jar $name not found"))
      log.debug(s"Loading resource $name")
      name -> stream
    }
  }

  import coursier.{Dependency, Fetch, Module, ModuleName, Organization, Repositories, LocalRepositories}
  import org.apache.maven.artifact.versioning.ComparableVersion

  private def toDependency(lib: ExtLib): Dependency = {
    val exclusions = Set(
      (Organization("org.scala-lang"), ModuleName("scala-reflect")),
      (Organization("org.scala-lang"), ModuleName("scala-library")),
      (Organization("org.scala-js"), ModuleName(s"scalajs-library_${Config.scalaMainVersion}")),
      (Organization("org.scala-js"), ModuleName(s"scalajs-test-interface_${Config.scalaMainVersion}"))
    )
    val artifactName =
      if (lib.compileTimeOnly) s"${lib.artifact}_${Config.scalaMainVersion}"
      else lib.artifact + sjsVersion
    Dependency(Module(Organization(lib.group), ModuleName(artifactName)), lib.version)
      .withExclusions(exclusions)
  }

  def loadLibraries(libs: Seq[ExtLib]) = {
    log.debug(s"Loading: $libs")

    val repositories = Seq(
      LocalRepositories.ivy2Local,
      Repositories.central
    )

    // resolve each library (with its transitive dependencies) separately so that the
    // ExtLib -> jar list mapping is kept; also keep module and version so that conflicts
    // can be resolved by version
    val resolved: Seq[(ExtLib, Seq[(Module, String, File)])] = libs.map { lib =>
      val result = Fetch()
        .withRepositories(repositories)
        .addDependencies(toDependency(lib))
        .runResult()
      val jars = result.detailedArtifacts.collect {
        case (dep, _, _, file) if file.getName.endsWith(".jar") => (dep.module, dep.version, file)
      }
      lib -> jars
    }

    // if the same module was resolved with several versions the highest wins; the losers are
    // logged. Each library's classpath is pointed at the winning jar.
    val chosenByModule: Map[Module, File] = resolved
      .flatMap(_._2)
      .groupBy(_._1)
      .map {
        case (module, candidates) =>
          val byVersion = candidates.distinctBy(_._2)
          val chosen    = byVersion.maxBy(c => new ComparableVersion(c._2))
          if (byVersion.size > 1)
            log.warn(
              s"Multiple versions of $module resolved: ${byVersion.map(_._2).sorted.mkString(", ")} -- using ${chosen._2}")
          module -> chosen._3
      }

    val extJars = chosenByModule.values.toSeq.distinctBy(_.getName)

    // acquire an exclusive lock to prevent others from updating the FFS at the same time
    Paths.get(Config.libCache).toFile.mkdirs()
    val lockFile       = Paths.get(Config.libCache).resolve("ffs.lck").toFile
    val lockChannel    = new RandomAccessFile(lockFile, "rw").getChannel
    var lock: FileLock = null
    try {
      while (lock == null) {
        try {
          lock = lockChannel.tryLock()
        } catch {
          case e: OverlappingFileLockException =>
            lock = null
        }
        if (lock == null) {
          print("\rAcquiring lock...")
          Thread.sleep(1000)
        }
      }

      val extStreams = extJars.map(f => (f.getName, new FileInputStream(f): InputStream))
      val ffs        = FlatFileSystem.build(Paths.get(Config.libCache), extStreams ++ commonJars)
      val absffs     = new AbstractFlatFileSystem(ffs)

      val commonLibs = commonJars.map { case (name, _) => name -> absffs.roots(name) }
      val extLibMap = resolved.map {
        case (lib, jars) =>
          lib -> jars.map {
            case (module, _, _) =>
              val f = chosenByModule(module)
              f.getName -> absffs.roots(f.getName)
          }.toMap
      }.toMap

      (commonLibs, extLibMap, ffs)
    } finally {
      lock.release()
      lockChannel.close()
    }
  }

  /**
    * External libraries loaded from repository
    */
  log.debug("Loading external libraries")
  val (commonLibs, extLibraries, ffs) = loadLibraries(depLibs)

  /**
    * In memory cache of all the jars used in the compiler.
    */
  val commonLibraries4compiler: Seq[AbstractFile] = commonLibs.map { case (_, jar) => jar.root }

  private def extJars(extLibs: Set[ExtLib]): Seq[(String, AbstractFlatJar)] =
    extLibs.toSeq.flatMap(lib => extLibraries.getOrElse(lib, Map.empty).toSeq).distinctBy(_._1)

  def compilerLibraries(extLibs: Set[ExtLib]): Seq[AbstractFile] = {
    commonLibraries4compiler ++ extJars(extLibs).map(_._2.root)
  }

  /**
    * IRFile list for the 1.x linker. The IRFile version is derived from the jar name and the
    * file path, as the old code did with the jar path.
    */
  private def irFilesOfJar(jarName: String, jar: FlatJar): Seq[IRFile] = {
    jar.files.filter(_.path.endsWith(".sjsir")).map { file =>
      val content = ffs.load(jar, file.path)
      val version = org.scalajs.ir.Version.fromUTF8String(org.scalajs.ir.UTF8String(s"$jarName:${file.path}"))
      new MemIRFileImpl(s"$jarName:${file.path}", version, content)
    }
  }

  val linkerCaches = new LRUCache[Seq[IRFile]]("IRFiles")

  def linkerLibraries(extLibs: Set[ExtLib]): Seq[IRFile] = {
    this.synchronized {
      linkerCaches.getOrUpdate(
        extLibs, {
          val commonIr = commonLibs.flatMap { case (name, absJar) => irFilesOfJar(name, absJar.flatJar) }
          val extIr    = extJars(extLibs).flatMap { case (name, absJar) => irFilesOfJar(name, absJar.flatJar) }
          commonIr ++ extIr
        }
      )
    }
  }
}
