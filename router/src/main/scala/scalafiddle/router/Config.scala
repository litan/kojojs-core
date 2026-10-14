package scalafiddle.router

import java.util.Properties
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

import upickle.default._

object Config {
  protected val config = ConfigFactory.load().getConfig("fiddle")

  // read the generated version data
  protected val versionProps = new Properties()
  versionProps.load(getClass.getResourceAsStream("/version.properties"))

  val interface            = config.getString("interface")
  val port                 = config.getInt("port")
  val analyticsID          = config.getString("analyticsID")
  val secret               = config.getString("secret")
  val scalaFiddleSourceUrl = config.getString("scalaFiddleSourceUrl")
  val scalaFiddleEditUrl   = config.getString("scalaFiddleEditUrl")

  val scalaVersions    = read[Seq[String]](config.getString("scalaVersions"))
  val defaultLibs      = read[Map[String, Seq[String]]](config.getString("defaultLibs"))
  val extLibs          = read[Map[String, String]](config.getString("extLibs"))
  val refreshLibraries = FiniteDuration(config.getDuration("refreshLibraries").toMillis, TimeUnit.MILLISECONDS)

  val corsOrigins = config.getStringList("corsOrigins").asScala.toSeq

  object compiler {
    val c    = config.getConfig("compiler")
    val host = c.getString("host")
    val port = c.getInt("port")
  }

  val version    = versionProps.getProperty("version")
  val aceVersion = versionProps.getProperty("aceVersion")

  val logoLight = config.getString("logoLight")
  val logoDark  = config.getString("logoDark")

  val extJS       = config.getStringList("extJS").asScala.toSeq
  val extCSS      = config.getStringList("extCSS").asScala.toSeq
  val baseEnv     = config.getString("baseEnv")
  val clientFiles = config.getStringList("clientFiles").asScala.toSeq

  val cacheDir = config.getString("cacheDir")
}
