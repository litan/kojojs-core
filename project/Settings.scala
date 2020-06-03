import sbt._

/**
  * Application settings. Configure the build for your application here.
  * You normally don't have to touch the actual build definition after this.
  */
object Settings {

  /** Options for the scala compiler */
  val scalacArgs = Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  )

  /** Declare global dependency versions here to avoid mismatches in multi part dependencies */
  object versions {
    val fiddle        = "1.1.1"
    val scalatest     = "3.0.3"
    val macroParadise = "2.1.0"
    val kindProjector = "0.9.6"
    val akka          = "2.5.12"
    val akkaHttp      = "10.1.1"
    val upickle       = "0.4.4"
    val ace           = "1.2.2"
    val dom           = "0.9.8"
    val scalatags     = "0.6.7"
    val async         = "0.9.7"
    val coursier      = "1.0.3"
    val kamon         = "0.6.7"
    val base64        = "0.2.4"
  }

  val kamon = Seq(
    "io.kamon" %% "kamon-core"   % versions.kamon,
    "io.kamon" %% "kamon-statsd" % versions.kamon
  )

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor"  % versions.akka,
    "com.typesafe.akka" %% "akka-stream" % versions.akka,
    "com.typesafe.akka" %% "akka-slf4j"  % versions.akka,
    "com.typesafe.akka" %% "akka-http"   % versions.akkaHttp
  )

  val logging = Seq(
    "net.logstash.logback" % "logstash-logback-encoder" % "5.0",
    "ch.qos.logback"       % "logback-classic"          % "1.2.3"
  )
}
