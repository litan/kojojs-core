package kojo.tr

import scala.concurrent.{ExecutionContext, Future}

/**
 * Gelecek (Future) -- stands for a value that is not ready yet.
 *
 * What is not taken from the desktop's `gelecek.scala`, and why:
 *  - `JGelecek` (java.util.concurrent.Future): it is not in Scala.js's javalib.
 *  - `PEtkinlik` (edu.umd.cs.piccolo.activities.PActivity): Piccolo is the desktop's drawing library; the
 *    web Kojo uses PIXI.
 *  - `Gelecek.olmaz` (Future.never): not available in 2.12.
 *
 * The browser has one thread, so `İşletimBağlamı.küresel` in fact queues the events and does not run them
 * in parallel.
 *
 * The execution context: işle, düzİşle, ele and elekle take it as an implicit parameter that has a default,
 * the global one. No implicit VALUE is put into the scope of a script, on purpose: a script that has its
 * own (for example `import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue`, as many samples
 * do, or `import scala.concurrent.ExecutionContext.Implicits.global`, as the Future examples on the
 * internet do) would then find two and the compiler would say "ambiguous implicit values". With the default
 * parameter it uses the one that the script has, and the global one if it has none.
 */
trait GelecekYöntemleri extends TemelTürler {
  type Gelecek[T] = Future[T]
  type İşletimBağlamı = ExecutionContext

  object İşletimBağlamı {
    lazy val küresel: İşletimBağlamı = ExecutionContext.global
  }

  object Gelecek {
    def başarılı[T](sonuç: T): Gelecek[T] = Future.successful(sonuç)
    // A DELIBERATE narrowing: Future.failed takes a Throwable. For children's API a KuralDışı (Exception) is
    // enough, and it is the same as gelecek.scala of desktop Koco.
    def başarısız[T](hata: KuralDışı): Gelecek[T] = Future.failed(hata)
  }

  implicit class GelecekMetotları[T](g: Gelecek[T]) {
    def işle[A](işlev: T => A)(implicit ex: İşletimBağlamı = İşletimBağlamı.küresel): Gelecek[A] = g.map(işlev)(ex)
    def düzİşle[A](işlev: T => Gelecek[A])(implicit ex: İşletimBağlamı = İşletimBağlamı.küresel): Gelecek[A] =
      g.flatMap(işlev)(ex)
    def ele(deneme: T => İkil)(implicit ex: İşletimBağlamı = İşletimBağlamı.küresel): Gelecek[T] = g.filter(deneme)(ex)
    def elekle(deneme: T => İkil)(implicit ex: İşletimBağlamı = İşletimBağlamı.küresel): Gelecek[T] =
      g.withFilter(deneme)(ex)
  }
}
