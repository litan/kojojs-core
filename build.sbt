import sbt._
import Keys._
import Settings._

// Migration to sbt 1, Scala.js 1.20.2 and Scala 2.13.18. Main differences from the old
// sbt 0.13 / Scala.js 0.6 build:
//  - the scalafmt plugin is gone (no sbt 1 release of the old plugin; formatting was off anyway)
//  - kamon, macro-paradise and kind-projector are gone
//  - scalajs-tools -> scalajs-linker (the compiler server's link API was rewritten)
//  - the runtime module also needs scalajs-javalib and scalajs-scalalib (since Scala.js 1.15 the
//    java and scala standard library IR live in separate artifacts)

val commonSettings = Seq(
  scalacOptions := scalacArgs,
  scalaVersion := "2.13.18",
  version := versions.fiddle
)

lazy val root = project
  .in(file("."))
  .aggregate(page, compilerServer, runtime, client, router)

lazy val shared = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % versions.upickle
    )
  )

lazy val client = project
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(shared)
  .settings(commonSettings)
  .settings(
    scalacOptions += "-Xasync",
    libraryDependencies ++= Seq(
      "org.scala-js"           %%% "scalajs-dom" % versions.dom,
      "com.github.marklister"  %%% "base64"      % versions.base64,
      "org.scala-lang.modules" %%% "scala-async" % versions.async % "provided",
      // for UUID.randomUUID: Scala.js ships SecureRandom in a separate package on purpose
      // (not cryptographic, but good enough for generating fiddle ids)
      "org.scala-js" %%% "scalajs-fake-insecure-java-securerandom" % "1.0.0"
    ),
    // rename output always to -opt.js
    artifactPath in (Compile, fastOptJS) := ((crossTarget in (Compile, fastOptJS)).value /
      ((moduleName in fastOptJS).value + "-opt.js"))
  )

lazy val page = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % versions.dom,
      "com.lihaoyi"  %%% "scalatags"   % versions.scalatags
    )
  )

// Helper module that collects the jars embedded as resources for the fiddle classpath.
// In Scala.js 1.x the stdlib IR is split into two artifacts, javalib (java.*) and
// scalalib (scala.*); the linker needs all three.
lazy val runtime = project
  .settings(commonSettings)
  .settings(
    autoScalaLibrary := false, // scala-library is added explicitly below
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-library"        % scalaVersion.value,
      "org.scala-lang" % "scala-reflect"        % scalaVersion.value,
      "org.scala-js"   % "scalajs-library_2.13" % scalaJSVersion,
      "org.scala-js"   % "scalajs-javalib"      % scalaJSVersion,
      "org.scala-js"   % "scalajs-scalalib_2.13" % s"${scalaVersion.value}+$scalaJSVersion"
    )
  )

lazy val compilerServer = project
  .in(file("compiler-server"))
  .dependsOn(shared, page)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(commonSettings)
  .settings(Revolver.settings: _*)
  .settings(
    name := "scalafiddle-core",
    libraryDependencies ++= Seq(
      "org.scala-lang"   % "scala-compiler"   % scalaVersion.value,
      "org.scala-js"     % "scalajs-compiler" % scalaJSVersion cross CrossVersion.full,
      "org.scala-js"     %% "scalajs-linker"  % scalaJSVersion,
      "com.lihaoyi"      %% "scalatags"       % versions.scalatags,
      "com.lihaoyi"      %% "upickle"         % versions.upickle,
      "io.get-coursier"  %% "coursier"        % versions.coursier,
      "org.apache.maven" % "maven-artifact"   % "3.3.9",
      "org.xerial.snappy" % "snappy-java"     % "1.1.10.5"
    ) ++ akka ++ logging,
    (resources in Compile) ++= {
      (managedClasspath in (runtime, Compile)).value.map(_.data) ++ Seq(
        (packageBin in (page, Compile)).value
      )
    },
    javaOptions in reStart ++= Seq("-Xmx3g", "-Xss4m"),
    javaOptions in Universal ++= Seq("-J-Xss4m"),
    resourceGenerators in Compile += Def.task {
      // store build version in a property file
      val file = (resourceManaged in Compile).value / "version.properties"
      val contents =
        s"""
           |version=${version.value}
           |scalaVersion=${scalaVersion.value}
           |scalaJSVersion=$scalaJSVersion
           |aceVersion=${versions.ace}
           |""".stripMargin
      IO.write(file, contents)
      Seq(file)
    }.taskValue,
    scriptClasspath := Seq("../config/") ++ scriptClasspath.value,
    dockerfile in docker := {
      val appDir: File = stage.value
      val targetDir    = "/app"

      new Dockerfile {
        from("eclipse-temurin:21-jre-jammy")
        entryPoint(s"$targetDir/bin/${executableScriptName.value}")
        copy(appDir, targetDir)
      }
    },
    imageNames in docker := Seq(
      ImageName(
        namespace = Some("scalafiddle"),
        repository = s"scalafiddle-core-${scalaBinaryVersion.value}",
        tag = Some("latest")
      ),
      ImageName(
        namespace = Some("scalafiddle"),
        repository = s"scalafiddle-core-${scalaBinaryVersion.value}",
        tag = Some(version.value)
      )
    )
  )

lazy val router = (project in file("router"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(sbtdocker.DockerPlugin)
  .dependsOn(shared)
  .settings(Revolver.settings: _*)
  .settings(commonSettings)
  .settings(
    name := "scalafiddle-router",
    libraryDependencies ++= Seq(
      "com.lihaoyi"           %% "scalatags"      % versions.scalatags,
      "org.webjars"           % "ace"             % versions.ace,
      "org.webjars"           % "normalize.css"   % "2.1.3",
      "org.webjars"           % "jquery"          % "2.2.2",
      "org.webjars.npm"       % "js-sha1"         % "0.4.0",
      "com.lihaoyi"           %% "upickle"        % versions.upickle,
      "com.github.marklister" %% "base64"         % versions.base64,
      "ch.megard"             %% "akka-http-cors" % "0.4.3",
      // /translate: the lexer in router/lib/scalariform.jar needs scala.xml.parsing.TokenTests at run
      // time. scalatest already brings it onto the Test classpath, so the unit tests pass without it
      // while a real server fails with NoClassDefFoundError on the first request. Keep it explicit.
      "org.scala-lang.modules" %% "scala-xml"     % "2.1.0",
      "org.scalatest"         %% "scalatest"      % versions.scalatest % Test
    ) ++ akka ++ logging,
    javaOptions in reStart ++= Seq("-Xmx1g"),
    scriptClasspath := Seq("../config/") ++ scriptClasspath.value,
    resourceGenerators in Compile += Def.task {
      // store build version in a property file
      val file = (resourceManaged in Compile).value / "version.properties"
      val contents =
        s"""
           |version=${version.value}
           |scalaVersion=${scalaVersion.value}
           |scalaJSVersion=$scalaJSVersion
           |aceVersion=${versions.ace}
           |""".stripMargin
      IO.write(file, contents)
      Seq(file)
    }.taskValue,
    (resources in Compile) ++= {
      // Seq((fullOptJS in (client, Compile)).value.data)
      Seq((fastOptJS in (client, Compile)).value.data)
    },
    dockerfile in docker := {
      val appDir: File = stage.value
      val targetDir    = "/app"

      new Dockerfile {
        from("eclipse-temurin:21-jre-jammy")
        entryPoint(s"$targetDir/bin/${executableScriptName.value}")
        copy(appDir, targetDir)
        expose(8880)
      }
    },
    imageNames in docker := Seq(
      ImageName(
        namespace = Some("scalafiddle"),
        repository = "scalafiddle-router",
        tag = Some("latest")
      ),
      ImageName(
        namespace = Some("scalafiddle"),
        repository = "scalafiddle-router",
        tag = Some(version.value)
      )
    )
  )
