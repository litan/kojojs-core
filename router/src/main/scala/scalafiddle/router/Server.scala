package scalafiddle.router

import java.nio.file.Paths

import akka.actor.ActorSystem
import org.slf4j.LoggerFactory

import scalafiddle.router.cache.FileCache

object Server extends App {
  private val log = LoggerFactory.getLogger(getClass)

  // The default `secret = "secret"` in reference.conf works but protects nothing: the value is public
  // in the repository. It guards the /compiler WebSocket (a registered compiler receives users'
  // source code and the JavaScript it returns runs in users' browsers) and /status. Starting is not
  // refused, so local development keeps working, but the misconfiguration is logged as an error.
  if (Config.secret == "secret") {
    log.error(
      "SCALAFIDDLE_SECRET is not set: the router is running with the public default secret, so the " +
        "/compiler registration gate and /status are effectively unprotected.")
  }

  val system = ActorSystem()

  val cache           = new FileCache(Paths.get(Config.cacheDir))
  val compilerManager = system.actorOf(CompilerManager.props, "CompilerManager")

  val webService = new WebService(system, cache, compilerManager)
}
