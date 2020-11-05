package kojo

import howlerscalajs.howler.Howl

import scala.collection.mutable
import scala.scalajs.js

object Mp3Player {
  val seen = mutable.HashSet.empty[String]
}

class Mp3Player()(implicit kojoWorld: KojoWorld) {
  import Mp3Player.seen

  var howl: Howl = _
  var loopHowl: Howl = _
  var playId: Int = -1
  var playLoopId: Int = -1

  def preloadMp3(mp3FileUrl: String): Unit = {
    if (!seen.contains(mp3FileUrl)) {
      seen.add(mp3FileUrl)
      AssetLoader.showLoading()
      howl = new Howl(
        js.Dynamic.literal(
          "src" -> js.Array(mp3FileUrl),
          "onload" -> { () =>
            AssetLoader.hideLoading()
            howl = null
          }
        )
      )
      // creating the Howl loads it ('preload' is true by default)
      // explicit load right after 'new Howl' results in two net requests for the file
      // howl.load()
    }
  }

  def playMp3(mp3FileUrl: String): Unit = {
    // As per current design:
    // A play does not work immediately after a preload. Only after the preload is done will a play work.
    if (howl == null) {
      if (!seen.contains(mp3FileUrl)) {
        seen.add(mp3FileUrl)
        AssetLoader.showLoading()
      }
      howl = new Howl(
        js.Dynamic.literal(
          "src" -> js.Array(mp3FileUrl),
          "onstop" -> { id: Int =>
            mp3Done()
          },
          "onend" -> { id: Int =>
            mp3Done()
          },
          "onplay" -> { id: Int =>
            AssetLoader.hideLoading()
          }
        )
      )
      playId = howl.play()
    }
  }

  def playMp3Sound(mp3FileUrl: String) = {
    if (!seen.contains(mp3FileUrl)) {
      seen.add(mp3FileUrl)
      AssetLoader.showLoading()
    }
    val transientHowl = new Howl(
      js.Dynamic.literal(
        "src" -> js.Array(mp3FileUrl),
        "onplay" -> { id: Int =>
          AssetLoader.hideLoading()
        }
      )
    )
    transientHowl.play()
  }

  def playMp3Loop(mp3FileUrl: String) {
    if (loopHowl == null) {
      if (!seen.contains(mp3FileUrl)) {
        seen.add(mp3FileUrl)
        AssetLoader.showLoading()
      }
      loopHowl = new Howl(
        js.Dynamic.literal(
          "src" -> js.Array(mp3FileUrl),
          "loop" -> true,
          "onstop" -> { id: Int =>
            mp3LoopDone()
          },
          "onplay" -> { id: Int =>
            AssetLoader.hideLoading()
          }
        )
      )
      playLoopId = loopHowl.play()
    }
  }

  def isMp3Playing = howl != null

  private def mp3Done(): Unit = {
    howl = null
    playId = -1
  }

  private def mp3LoopDone(): Unit = {
    loopHowl = null
    playLoopId = -1
  }

  def stopMp3(): Unit = {
    if (howl != null) {
      howl.stop(playId)
    }
  }

  def stopMp3Loop(): Unit = {
    if (loopHowl != null) {
      loopHowl.stop(playLoopId)
    }
  }
}
