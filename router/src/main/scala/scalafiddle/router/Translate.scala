package scalafiddle.router

import net.kogics.kojo.lite.i18n.tr.Translator
import net.kogics.kojo.lite.i18n.tr.Translator.{Direction, EnglishToTurkish, TurkishToEnglish}
import upickle.default._

import java.util.concurrent.{Executors, ThreadFactory}
import scala.concurrent.ExecutionContext

/** `POST /translate`: Koco (Turkish) script <-> Kojo (English) script.
  *
  * The translator itself is the desktop Koco library (bulent2k2/kojo:
  * lite/i18n/tr/{translator,translationdictionary,dict}.scala + two TSVs); it is kept here as a
  * BYTE-FOR-BYTE copy and is never edited by hand (see router/TRANSLATOR.md). This file is only the
  * thin layer that binds it to HTTP.
  *
  * Translation is lexical and needs no compiler: the patched scalariform lexer plus two
  * dictionaries. A request takes milliseconds; the dictionary loads on the first request
  * (`Translator.dictionary` is lazy).
  */
object Translate {

  /** A SEPARATE small pool for translation: it runs in the same JVM as the router's /compile and
    * WebSocket forwarding, and a 64 KB request costs ~350 ms of CPU in the worst case (measured). On
    * the default dispatcher a client sending 64 KB continuously could keep it busy. Two threads:
    * translation requests queue among themselves, compiler work is not affected.
    */
  val ec: ExecutionContext = ExecutionContext.fromExecutorService(
    Executors.newFixedThreadPool(2, new ThreadFactory {
      def newThread(r: Runnable): Thread = {
        val t = new Thread(r, "translate")
        t.setDaemon(true)
        t
      }
    })
  )

  /** A name from the report and how many times it occurs. */
  case class NameCount(name: String, count: Int)
  object NameCount { implicit val rw: ReadWriter[NameCount] = macroRW }

  /** A name with several possible translations that was translated with the most frequent one. */
  case class Ambiguous(name: String, chosen: String, others: Seq[String], line: Int)
  object Ambiguous { implicit val rw: ReadWriter[Ambiguous] = macroRW }

  /** `leftovers`: names left untranslated that the translator calls "still Turkish": names with
    * Turkish letters or names the dictionary knows (the user's own names and gaps in the dictionary).
    * An ASCII user name (`halka`) stays untranslated but is not listed here. Filled for tr2en only;
    * empty for en2tr (English names are valid in Koco too, so leaving them is cosmetic). No line
    * numbers: the translator keeps only counts.
    */
  case class Report(translated: Int, leftovers: Seq[NameCount], ambiguous: Seq[Ambiguous])
  object Report { implicit val rw: ReadWriter[Report] = macroRW }

  /** `remainingKeywords`: the output still contains a keyword of the source language (translation incomplete). */
  case class Result(direction: String, code: String, report: Report, remainingKeywords: Seq[String])
  object Result { implicit val rw: ReadWriter[Result] = macroRW }

  val tr2en = "tr2en"
  val en2tr = "en2tr"
  val auto  = "auto"

  /** With no direction given it is detected from the script: a Turkish keyword (dez, den, tanım,
    * eğer, ...) means Turkish, otherwise English. With no keyword at all (just `ileri(10)`) it counts
    * as English: the code stays valid whichever way it is translated (English names are valid in
    * Koco), so a wrong guess is cheap.
    */
  def detectDirection(code: String): String =
    if (Translator.leftoverKeywords(code, TurkishToEnglish).nonEmpty) tr2en else en2tr

  def parseDirection(name: String): Option[Direction] = name match {
    case `tr2en` => Some(TurkishToEnglish)
    case `en2tr` => Some(EnglishToTurkish)
    case _       => None
  }

  /** `direction`: "tr2en" | "en2tr" | empty/"auto" (detect from the script). Anything else: Left(message). */
  def translate(code: String, direction: Option[String]): Either[String, Result] = {
    val name = direction.filter(d => d.nonEmpty && d != auto).getOrElse(detectDirection(code))
    parseDirection(name) match {
      case None => Left(s"invalid direction: '$name' (must be tr2en, en2tr or auto)")
      case Some(d) =>
        val (translated, report) = Translator.translateScript(code, d)
        val leftovers = d match {
          case TurkishToEnglish => report.remainingTurkishNames
          case _                => Map.empty[String, Int]
        }
        Right(
          Result(
            name,
            translated,
            Report(
              report.translatedCount,
              leftovers.toSeq.sortBy { case (n, c) => (-c, n) }.map { case (n, c) => NameCount(n, c) },
              report.ambiguities.map(a => Ambiguous(a.name, a.chosen, a.alternatives, a.line))
            ),
            Translator.leftoverKeywords(translated, d)
          ))
    }
  }
}
