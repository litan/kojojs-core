package scalafiddle.router

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CacheDirectives.{`max-age`, `no-cache`}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import kamon.Kamon
import kamon.metric.instrument.Counter
import org.slf4j.LoggerFactory
import upickle.default._

import scala.concurrent.Future
import scala.concurrent.duration._
import scalafiddle.router.cache.Cache
import scalafiddle.router.frontend.Static
import scalafiddle.shared._

sealed trait CacheValue

case class CacheResult(data: Array[Byte]) extends CacheValue

case class NoCacheResult(data: Array[Byte]) extends CacheValue

case class CacheError(error: String) extends CacheValue

case object NotFound extends CacheValue

case class CacheCounter(hit: Counter, miss: Counter, error: Counter)

class WebService(system: ActorSystem, cache: Cache, compilerManager: ActorRef) {
  import HttpCharsets._
  import MediaTypes._

  implicit val actorSystem  = system
  implicit val timeout      = Timeout(30.seconds)
  implicit val materializer = ActorMaterializer()
  implicit val ec           = system.dispatcher
  val log                   = LoggerFactory.getLogger(getClass)

  val corsSettings =
    CorsSettings.defaultSettings.withAllowedOrigins(HttpOriginRange(Config.corsOrigins.map(HttpOrigin(_)): _*))

  type ParamValidator = Map[String, Validator]
  val embedValidator: ParamValidator = Map(
    "responsiveWidth" -> IntValidator(0, 3840),
    "theme"           -> ListValidator("dark", "light"),
    "style"           -> EmptyValidator,
    "fullOpt"         -> EmptyValidator,
    "layout"          -> EmptyValidator,
    "hideButtons"     -> EmptyValidator,
    "passive"         -> EmptyValidator,
    "referrer"        -> EmptyValidator
  )
  val codeframeValidator: ParamValidator = Map(
    "theme" -> ListValidator("dark", "light")
  )

  val compileValidator: ParamValidator = Map(
    "sourceSHA1" -> EmptyValidator,
    "sfversion"  -> EmptyValidator,
    "opt"        -> ListValidator("fast", "full")
  )

  val cacheCounters = Seq(
    "compile",
    "embed",
    "complete"
  ).map { name =>
    name -> CacheCounter(
      Kamon.metrics.counter(s"$name-cache-hit"),
      Kamon.metrics.counter(s"$name-cache-miss"),
      Kamon.metrics.counter(s"$name-validation-error")
    )
  }.toMap

  def validateParams(params: Map[String, String], validator: ParamValidator): Option[String] = {
    params
      .foldLeft(List.empty[Option[String]]) {
        case (validated, (key, value)) =>
          validator.get(key).flatMap(v => v.isInvalid(value).map(e => s"$key: $e")) :: validated
      }
      .flatten match {
      case Nil    => None
      case errors => Some(errors.mkString("\n"))
    }
  }

  def buildHash(path: String, params: Map[String, String], validator: ParamValidator): String = {
    val sb = new StringBuilder(path)
    params.filterKeys(validator.contains).toSeq.sortBy(_._1).foreach {
      case (key, value) =>
        sb.append(key).append(value)
    }
    val hash = MessageDigest.getInstance("SHA1").digest(sb.toString().getBytes(StandardCharsets.UTF_8))
    hash.map("%02X".format(_)).mkString
  }

  def gzipCompress(data: Array[Byte]): Array[Byte] = {
    val bos = new ByteArrayOutputStream()
    val zis = new GZIPOutputStream(bos)
    zis.write(data)
    zis.close()
    bos.close()
    bos.toByteArray
  }

  def cacheOr(path: String, params: Map[String, String], validator: ParamValidator, expiration: Int)(
      value: => Future[CacheValue])(toResponse: Array[Byte] => HttpResponse): Future[HttpResponse] = {
    validateParams(params, validator) match {
      case Some(error) =>
        cacheCounters.get(path).foreach { _.error.increment() }
        Future.successful(HttpResponse(StatusCodes.BadRequest, entity = error))
      case None =>
        // check cache
        val hash = buildHash(path, params, validator)
        cache.get(hash, expiration).flatMap {
          case Some(data) =>
            log.debug(s"Cache hit on $path")
            cacheCounters.get(path).foreach { _.hit.increment() }
            Future.successful(
              toResponse(data)
                .withHeaders(`Cache-Control`(`max-age`(expiration)), `Content-Encoding`(HttpEncodings.gzip))
            )
          case None =>
            cacheCounters.get(path).foreach { _.miss.increment() }
            value map {
              case CacheResult(data) =>
                val gzipData = gzipCompress(data)
                cache.put(hash, gzipData, expiration)
                toResponse(gzipData)
                  .withHeaders(`Cache-Control`(`max-age`(expiration)), `Content-Encoding`(HttpEncodings.gzip))
              case NoCacheResult(data) =>
                val gzipData = gzipCompress(data)
                toResponse(gzipData)
                  .withHeaders(`Cache-Control`(`no-cache`), `Content-Encoding`(HttpEncodings.gzip))
              case NotFound =>
                HttpResponse(StatusCodes.NotFound)
              case CacheError(error) =>
                HttpResponse(StatusCodes.BadRequest, entity = error)
            } recover {
              case e: Throwable =>
                log.error("Internal error", e)
                HttpResponse(StatusCodes.InternalServerError)
            }
        }
    }
  }

  def decodeSource(b64: String): String = {
    import com.github.marklister.base64.Base64._
    implicit def scheme: B64Scheme = base64Url
    // decode base64 and gzip
    val compressedSource = Decoder(b64).toByteArray
    val bis              = new ByteArrayInputStream(compressedSource)
    val zis              = new GZIPInputStream(bis)
    val buf              = new Array[Byte](1024)
    val bos              = new ByteArrayOutputStream()
    var len              = 0
    while ({ len = zis.read(buf); len > 0 }) {
      bos.write(buf, 0, len)
    }
    zis.close()
    bos.close()
    val source = new String(bos.toByteArray, StandardCharsets.UTF_8)
    // add default libraries
    source
  }

  def cachedCompile(source: String, paramMap: Map[String, String], clientIP: RemoteAddress): Future[HttpResponse] = {
    val sourceHash =
      MessageDigest.getInstance("SHA1").digest(source.getBytes(StandardCharsets.UTF_8)).map("%02X".format(_)).mkString
    val allParams = paramMap.updated("sourceSHA1", sourceHash).updated("sfversion", Config.version)
    log.debug(s"Source hash: $sourceHash")
    cacheOr("compile", allParams, compileValidator, 3600 * 24 * 90) {
      val remoteIP = clientIP.toIP.map(_.ip.getHostAddress).getOrElse("localhost")
      log.debug(s"Compile request from $remoteIP")
      val compileId = UUID.randomUUID().toString
      ask(compilerManager, CompilationRequest(compileId, source, clientIP.toString, paramMap("opt")))
        .mapTo[Either[String, CompilerResponse]]
        .map {
          case Right(response: CompilationResponse) if response.jsCode.isDefined =>
            CacheResult(write(response).getBytes("UTF-8"))
          case Right(response: CompilationResponse) =>
            NoCacheResult(write(response).getBytes("UTF-8"))
          case Left(error) =>
            CacheError(error)
          case _ =>
            CacheError("Internal error")
        } recover {
        case e: Exception =>
          log.error("Error while compiling", e)
          compilerManager ! CancelCompilation(compileId)
          throw e
      }
    }(data => HttpResponse(entity = HttpEntity(`application/json`, data)))
  }

  val extRoute: Route = {
    post {
      path("compile") {
        handleRejections(CorsDirectives.corsRejectionHandler) {
          CorsDirectives.cors(corsSettings) {
            parameterMap { paramMap =>
              withSizeLimit(64 * 1024) {
                extractClientIP { clientIP =>
                  extractRequest { request =>
                    complete {
                      request.entity.toStrict(5.seconds).flatMap { entity =>
                        val source = entity.data.decodeString(StandardCharsets.UTF_8)
                        cachedCompile(source, paramMap, clientIP)
                      }
                    }
                  }
                }
              }
            }
          }
        }
      } ~ path("complete") {
        handleRejections(CorsDirectives.corsRejectionHandler) {
          CorsDirectives.cors(corsSettings) {
            parameterMap { paramMap =>
              withSizeLimit(64 * 1024) {
                extractClientIP { clientIP =>
                  extractRequest { request =>
                    complete {
                      request.entity.toStrict(5.seconds).flatMap { entity =>
                        val source    = entity.data.decodeString(StandardCharsets.UTF_8)
                        val compileId = UUID.randomUUID().toString
                        ask(compilerManager,
                            CompletionRequest(compileId, source, clientIP.toString, paramMap("offset").toInt))
                          .mapTo[Either[String, CompletionResponse]]
                          .map {
                            case Right(response: CompletionResponse) =>
                              HttpResponse(entity = HttpEntity(`application/json`, write(response).getBytes("UTF-8")))
                            case Left(error) =>
                              HttpResponse(StatusCodes.BadRequest, entity = error)
                          } recover {
                          case e: Exception =>
                            compilerManager ! CancelCompilation(compileId)
                            HttpResponse(StatusCodes.InternalServerError, entity = "Internal error")
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } ~
      get {
        path("embed") {
          parameterMap { paramMap =>
            complete {
              cacheOr("embed", paramMap, embedValidator, 3600)(
                Future.successful(CacheResult(Static.renderPage(Config.clientFiles, paramMap)))) { data =>
                HttpResponse(entity = HttpEntity(`text/html` withCharset `UTF-8`, data))
              }
            }
          }
        } ~ path("codeframe") {
          parameterMap { paramMap =>
            complete {
              cacheOr("codeframe", paramMap, codeframeValidator, 3600)(
                Future.successful(CacheResult(Static.renderCodeFrame(paramMap)))) { data =>
                HttpResponse(entity = HttpEntity(`text/html` withCharset `UTF-8`, data))
              }
            }
          }
        } ~ path("compile") {
          handleRejections(CorsDirectives.corsRejectionHandler) {
            CorsDirectives.cors(corsSettings) {
              parameterMap { paramMap =>
                extractClientIP { clientIP =>
                  complete {
                    val source = decodeSource(paramMap("source"))
                    cachedCompile(source, paramMap, clientIP)
                  }
                }
              }
            }
          }
        } ~ path("complete") {
          handleRejections(CorsDirectives.corsRejectionHandler) {
            CorsDirectives.cors(corsSettings) {
              parameterMap { paramMap =>
                withSizeLimit(64 * 1024) {
                  extractClientIP { clientIP =>
                    extractRequest { request =>
                      complete {
                        request.entity.toStrict(5.seconds).flatMap { entity =>
                          val source = decodeSource(paramMap("source"))
//                          val source    = entity.data.decodeString(StandardCharsets.UTF_8)
                          val compileId = UUID.randomUUID().toString
                          ask(compilerManager,
                              CompletionRequest(compileId, source, clientIP.toString, paramMap("offset").toInt))
                            .mapTo[Either[String, CompletionResponse]]
                            .map {
                              case Right(response: CompletionResponse) =>
                                HttpResponse(entity = HttpEntity(`application/json`, write(response).getBytes("UTF-8")))
                              case Left(error) =>
                                HttpResponse(StatusCodes.BadRequest, entity = error)
                            } recover {
                            case e: Exception =>
                              compilerManager ! CancelCompilation(compileId)
                              HttpResponse(StatusCodes.InternalServerError, entity = "Internal error")
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        } ~ path("compileResult") {
          handleRejections(CorsDirectives.corsRejectionHandler) {
            CorsDirectives.cors(corsSettings) {
              parameterMap { paramMap =>
                complete {
                  cacheOr("compile", paramMap.updated("sfversion", Config.version), compileValidator, 3600 * 24 * 90) {
                    Future.successful(NotFound)
                  }(data => HttpResponse(entity = HttpEntity(`application/json`, data)))
                }
              }
            }
          }
        } ~ path("cache" / Segment) { res =>
          // resources identified by a hash can be cached "forever" (2 years in this case)
          complete {
            val (hash, ext) = res.span(_ != '.')
            val contentType: ContentType = ext match {
              case ".css" => `text/css` withCharset `UTF-8`
              case ".js"  => `application/javascript` withCharset `UTF-8`
              case _      => `application/octet-stream`
            }
            cacheOr(s"cache/$res", Map.empty, Map.empty, 3600 * 24 * 365 * 2) {
              Future.successful {
                Static.fetchResource(hash) match {
                  case Some(src) =>
                    CacheResult(src)
                  case None =>
                    CacheError("")
                }
              }
            }(data => HttpResponse(entity = HttpEntity(contentType, data)))
          }
        } ~ respondWithHeader(`Cache-Control`(`max-age`(3600 * 24))) {
          getFromResourceDirectory("/web")
        }
      }
  }

  def wsFlow(scalaVersion: String): Flow[Message, Message, Any] =
    ActorFlow.actorRef[Message, Message](out => CompilerService.props(out, compilerManager, scalaVersion), () => ())

  val compilerRoute: Route = {
    (path("compiler") & parameters(('secret, 'scalaVersion))) { (secret, scalaVersion) =>
      if (secret == Config.secret && Config.scalaVersions.contains(scalaVersion))
        handleWebSocketMessages(wsFlow(scalaVersion))
      else
        complete(HttpResponse(StatusCodes.Forbidden))
    }
  }
  val route = extRoute ~ compilerRoute

  val bindingFuture = Http().bindAndHandle(route, Config.interface, Config.port)

  bindingFuture map { binding =>
    log.info(s"Scala Fiddle router ${Config.version} at ${Config.interface}:${Config.port}")
  } recover {
    case ex =>
      log.error(s"Failed to bind to ${Config.interface}:${Config.port}", ex)
  }

  // clean cache
  cache.clean(3600)
}
