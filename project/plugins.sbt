// Migration from sbt 0.13 + Scala.js 0.6 to sbt 1 + Scala.js 1.x. The dead
// http://repo.typesafe.com resolver is gone (sbt 1 plugins are on Maven Central) and so is the
// scalafmt plugin (the old lucidchart plugin has no sbt 1 release; formatting was off anyway).
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.2")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.8.3")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")
