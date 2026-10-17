package kojo

import scala.collection.mutable
import scala.scalajs.js

/**
 * The player of `playNote`: a Web Audio oscillator, with a TIME CURSOR like the RealtimeNotePlayer (MIDI)
 * of the desktop.
 *
 * On the desktop playNote does not wait: it adds the note to the MIDI track at the cursor and moves the
 * cursor forward by the duration; if the sequencer is idle the note plays at once, if it is playing the
 * note plays when the earlier ones are finished. Consecutive calls thus make a MELODY. Here it is the
 * same: a note starts at `max(now, cursor)` and the cursor goes to the end of the note. (Without the
 * cursor all the notes played at the same moment, like a chord.)
 *
 * Notes that play TOGETHER (`playTogether`, `playChord`): they all start at the same `start` moment and
 * each plays for its own duration; the cursor goes to the end of the LONGEST note, so the next note starts
 * after all of them have finished. This has no counterpart on the desktop (one channel, one track); it is
 * an addition of the web Kojo. The volume is divided by the square root of the number of notes: the power
 * stays about the same, and a chord is as loud as one note. n in-phase notes add up to n * level in the
 * worst case; 0.3/√n keeps the peak below 1 only for n <= 10 (0.3·√10 ≈ 0.95), so for crowded groups the
 * level is limited to 0.95/n: the peak never goes over 0.95 in any group, no crackling. For one note the
 * divisor is 1, so `playNote` is as it was. A note of duration 0 counts in the size of the group too (it is
 * silent but it makes the others quieter).
 *
 * The instrument codes are MIDI program numbers; there are no instruments in Web Audio, so they are
 * mapped roughly to a waveform (see `waveform`). As on the desktop, changing the instrument affects only
 * the notes that come after.
 */
class NotePlayer {
  // A test hook: a fake context or an OfflineAudioContext can be put here. If it is null, the browser's
  // AudioContext is made at the first note.
  private[kojo] var audioContext: js.Dynamic = null
  // The earliest moment when the next note can start (by the clock of the context, in seconds)
  private var cursor = 0.0
  private var instrument = 0
  // (gain node, end moment): for stop(); the ones that are finished are weeded out
  private val playing = mutable.ArrayBuffer.empty[(js.Dynamic, Double)]

  /** Makes the context and resumes it if it is suspended (a context made before a user interaction starts suspended). */
  private def readyContext(): js.Dynamic = {
    val ctx = getContext()
    if (ctx != null && (ctx.state: Any) == "suspended" && js.typeOf(ctx.resume) == "function") ctx.resume()
    ctx
  }

  private def getContext(): js.Dynamic = {
    if (audioContext == null) {
      import js.Dynamic.{global => g}
      val Ctx =
        if (js.typeOf(g.AudioContext) != "undefined") g.AudioContext
        else if (js.typeOf(g.webkitAudioContext) != "undefined") g.webkitAudioContext
        else null
      // If there is no Web Audio (for example in Node tests) go on silently
      if (Ctx != null) audioContext = js.Dynamic.newInstance(Ctx)()
    }
    audioContext
  }

  def setInstrument(code: Int): Unit = {
    require(code >= 0 && code <= 127, "Instrument Code should be between 0 and 127")
    instrument = code
  }

  def playNote(pitch: Int, durationMillis: Int, volume: Int): Unit =
    playTogether(Seq((pitch, durationMillis)), volume)

  /** They all start at the same moment and last the same time; the next note comes after `duration`. */
  def playChord(pitches: collection.Seq[Int], durationMillis: Int, volume: Int): Unit =
    playTogether(pitches.map(p => (p, durationMillis)), volume)

  /**
   * They all start at the same moment and each plays for its own duration; the next note starts at the end
   * of the longest. ALL of them are checked first: if one is out of range no note enters the queue (no half
   * chord plays). An empty sequence does nothing.
   */
  def playTogether(timedNotes: collection.Seq[(Int, Int)], volume: Int): Unit = {
    timedNotes.foreach { case (pitch, _) =>
      require(pitch >= 0 && pitch <= 127, "Note pitch should be between 0 and 127")
    }
    require(volume >= 0 && volume <= 127, "Note volume should be between 0 and 127")
    if (timedNotes.nonEmpty) {
      val ctx = readyContext()
      if (ctx != null) {
        val now = ctx.currentTime.asInstanceOf[Double]
        val start = math.max(now, cursor)
        val n = timedNotes.size
        // 0.3/√n up to n <= 10; beyond that 0.95/n: the sum of n in-phase notes must not pass 0.95
        val base = math.min(0.3 / math.sqrt(n), 0.95 / n)
        // exponentialRamp cannot start from zero (undefined); a tiny floor for volume = 0
        val level = math.max(volume / 127.0 * base, 1e-4) // without blowing the speaker
        var lastEnd = start
        timedNotes.foreach { case (pitch, durationMillis) =>
          val end = start + math.max(durationMillis, 0) / 1000.0
          lastEnd = math.max(lastEnd, end)

          val oscillator = ctx.createOscillator()
          val gain = ctx.createGain()
          oscillator.`type` = NotePlayer.waveform(instrument)
          oscillator.frequency.value = NotePlayer.frequency(pitch)
          gain.gain.setValueAtTime(level, start)
          gain.gain.exponentialRampToValueAtTime(0.001, end)
          oscillator.connect(gain)
          gain.connect(ctx.destination)
          oscillator.start(start)
          oscillator.stop(end)

          playing += ((gain, end))
        }
        cursor = lastEnd
        playing.filterInPlace(_._2 > now)
      }
    }
  }

  /** A silent wait: moves the cursor forward and makes no sound (a rest in music). */
  def playRest(durationMillis: Int): Unit = {
    val ctx = readyContext()
    if (ctx != null) {
      val now = ctx.currentTime.asInstanceOf[Double]
      cursor = math.max(now, cursor) + math.max(durationMillis, 0) / 1000.0
    }
  }

  /**
   * The time (ms) until all the queued notes are finished, 0 if idle. If it is called right after queuing
   * notes in a loop it is the total length of the melody. (`now - t0` in a script does not give this:
   * playNote does not wait, it only queues.) If the context is suspended (before a user interaction) the
   * clock does not move, so the length of all the queued notes is returned.
   */
  def timeLeftMillis: Int =
    if (audioContext == null) 0
    else math.max(0.0, math.round((cursor - audioContext.currentTime.asInstanceOf[Double]) * 1000.0).toDouble).toInt

  /** Silences all the queued notes and resets the cursor (stopNotePlayer of the desktop). */
  def stop(): Unit = {
    playing.foreach { case (gain, _) => gain.disconnect() }
    playing.clear()
    cursor = 0.0
  }
}

object NotePlayer {
  /** Turns a MIDI pitch into a frequency (69 -> 440 Hz). */
  def frequency(pitch: Int): Double = 440.0 * math.pow(2.0, (pitch - 69) / 12.0)

  /** Maps a MIDI instrument code to a Web Audio waveform (a rough approximation). */
  def waveform(code: Int): String =
    if (code < 8) "triangle"           // pianos
    else if (code < 16) "sine"         // chromatic percussion (celesta, xylophone...)
    else if (code < 24) "square"       // organs
    else if (code < 32) "sawtooth"     // guitars
    else if (code < 40) "sine"         // basses
    else if (code < 56) "sawtooth"     // strings, choir
    else if (code < 72) "square"       // winds
    else if (code < 120) "triangle"    // synths, ethnic
    else "sawtooth"                    // sound effects
}
