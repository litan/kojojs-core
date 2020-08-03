package kojo

import howlerscalajs.howler.Howl

import scala.scalajs.js

class Mp3Player()(implicit kojoWorld: KojoWorld) {
  var howl: Howl = _
  var loopHowl: Howl = _
  var playId: Int = -1
  var playLoopId: Int = -1

  def playMp3(mp3FileUrl: String): Unit = {
    if (howl == null) {
      AssetLoader.showLoading()
      howl = new Howl(
        js.Dynamic.literal(
          "src" -> js.Array(mp3FileUrl),
          "onstop" -> { id: Int =>
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

  def playMp3Sound(mp3FileUrl: String) = playMp3(mp3FileUrl)

  def playMp3Loop(mp3FileUrl: String) {
    if (loopHowl == null) {
      AssetLoader.showLoading()
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
