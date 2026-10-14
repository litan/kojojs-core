package scalafiddle.compiler.cache

import java.io.{FileOutputStream, InputStream, RandomAccessFile}
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.zip.{ZipEntry, ZipInputStream}

import org.slf4j.LoggerFactory
import org.xerial.snappy.Snappy
import upickle.default._

case class FlatFile(path: String, offset: Long, compressedSize: Int, origSize: Int)

object FlatFile { implicit val rw: ReadWriter[FlatFile] = macroRW }

case class FlatJar(name: String, files: Seq[FlatFile])

object FlatJar { implicit val rw: ReadWriter[FlatJar] = macroRW }

// larray has no Scala 2.13 release, so the data file is read through a java.nio FileChannel:
// for each file the relevant region is read and decompressed with Snappy in memory.
class FlatFileSystem(dataChannel: FileChannel, val jars: Seq[FlatJar], val index: Map[String, FlatFile]) {

  def exists(path: String) = index.contains(path)

  def load(flatJar: FlatJar, path: String): Array[Byte] = {
    load(jars.find(_.name == flatJar.name).get.files.find(_.path == path).get)
  }

  def load(path: String): Array[Byte] = {
    load(index(path))
  }

  def load(file: FlatFile): Array[Byte] = {
    val compressed = java.nio.ByteBuffer.allocate(file.compressedSize)
    var pos        = file.offset
    while (compressed.hasRemaining) {
      val n = dataChannel.read(compressed, pos)
      if (n < 0) throw new java.io.EOFException(s"Unexpected EOF reading ${file.path}")
      pos += n
    }
    Snappy.uncompress(compressed.array())
  }

  def filter(f: Set[String]): FlatFileSystem = {
    val newJars = jars.filter(j => f.contains(j.name))
    new FlatFileSystem(dataChannel, newJars, FlatFileSystem.createIndex(newJars))
  }
}

object FlatFileSystem {
  val log = LoggerFactory.getLogger(getClass)

  def apply(location: Path): FlatFileSystem = {
    location.toFile.mkdirs()
    val jars                         = readMetadata(location)
    val index: Map[String, FlatFile] = createIndex(jars)
    new FlatFileSystem(openData(location), jars, index)
  }

  private def openData(location: Path): FileChannel =
    new RandomAccessFile(location.resolve("data").toFile, "r").getChannel

  private def createIndex(jars: Seq[FlatJar]): Map[String, FlatFile] = {
    jars.iterator.flatMap(_.files.map(file => (file.path, file))).toMap
  }

  private def readMetadata(location: Path): Seq[FlatJar] = {
    read[Seq[FlatJar]](new String(Files.readAllBytes(location.resolve("index.json")), StandardCharsets.UTF_8))
  }

  private val validExtensions = Set("class", "sjsir")
  private def validFile(entry: ZipEntry) = {
    val name = entry.getName
    val dot  = name.lastIndexOf('.')
    val ext  = if (dot < 0) "" else name.substring(dot + 1)
    !entry.isDirectory && validExtensions.contains(ext)
  }

  def build(location: Path, jars: Seq[(String, InputStream)]): FlatFileSystem = {
    // if metadata already exists, read it in
    val existingJars = if (location.resolve("index.json").toFile.exists()) readMetadata(location) else Seq.empty[FlatJar]

    val newJars = jars.filterNot(p => existingJars.exists(_.name == p._1))

    // make location path
    location.toFile.mkdirs()

    val dataFile = location.resolve("data").toFile
    val fos      = new FileOutputStream(dataFile, true)
    var offset   = dataFile.length()

    // read through all new JARs, append contents to data and create metadata
    val addedJars = newJars.map { jarPath =>
      val name = jarPath._1
      log.debug(s"Extracting JAR $name")
      val fis       = jarPath._2
      val jarStream = new ZipInputStream(fis)
      val entries = Iterator
        .continually(jarStream.getNextEntry)
        .takeWhile(_ != null)
        .filter(validFile)

      val files = entries.map { entry =>
        // read and compress the file
        val content    = jarStream.readAllBytes()
        val compressed = Snappy.compress(content)
        fos.write(compressed)
        val ff = FlatFile(entry.getName, offset, compressed.length, content.length)
        offset += compressed.length
        ff
      }.toList
      jarStream.close()
      FlatJar(name, files)
    }
    fos.close()

    val finalJars = existingJars ++ addedJars
    val json      = write(finalJars)
    Files.write(location.resolve("index.json"), json.getBytes(StandardCharsets.UTF_8))

    new FlatFileSystem(openData(location), finalJars, createIndex(finalJars))
  }
}
