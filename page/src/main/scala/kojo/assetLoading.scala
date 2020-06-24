package kojo

import kojo.doodle.Color
import kojo.syntax.Builtins
import pixiscalajs.PIXI
import pixiscalajs.PIXI.Pixi

import scala.collection.mutable

object AssetLoader {
  private val loader = Pixi.loader
  case class QEntry(name: String, url: String, doneFn: (PIXI.loaders.Loader, Any) => Unit)

  val queue = mutable.Queue.empty[QEntry]
  var loadProgress: LoadProgress = _

  def addAndLoad(name: String, url: String, doneFn: (PIXI.loaders.Loader, Any) => Unit)(implicit kojoWorld: KojoWorld): Unit = {
    if (loadProgress == null) {
      loadProgress = new LoadProgress
    }
    loadProgress.show()

    def checkQ(): Unit = {
      if (queue.nonEmpty) {
        val qe = queue.dequeue()
        addAndLoad(qe.name, qe.url, qe.doneFn)
      }
      else {
        loadProgress.hide()
      }
    }

    if (!loader.loading) {
      val resVal = loader.resources(name)
      if (resVal == ()) {
        loader.add(name, url)
        loader.load { (loader, any) =>
          doneFn(loader, any)
          checkQ()
        }
      }
      else {
        doneFn(loader, resVal)
        checkQ()
      }
    }
    else {
      queue.enqueue(QEntry(name, url, doneFn))
    }
  }
}

class LoadProgress(implicit kw: KojoWorld) {
  var loadingPic: Picture = {
    val pic = new TextPic("Loading...", 30, Color.lightBlue)
    pic.draw()
    pic
  }

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  loadingPic.ready.foreach { _ =>
    val b = loadingPic.bounds
    val bg = new RectanglePic(b.width + 10, b.height + 10)
    val bgColor = Color.rgb(20, 20, 20)
    bg.setPenColor(Color.black)
    bg.setFillColor(bgColor)
    val fg = new TextPic("Loading...", 30, Color.lightSteelBlue)
    val pics = new GPicsCentered(Seq(bg, fg))
    pics.translate(-b.width / 2 - 5, -b.height / 2 - 5)
    loadingPic.erase()
    loadingPic = pics
    pics.draw()
    pics.invisible()
  }

  def show(): Unit = {
    loadingPic.visible()
    loadingPic.moveToFront()
  }
  def hide(): Unit = {
    loadingPic.invisible()
  }
}
