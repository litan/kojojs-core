package kojo.tr

/**
 * The Turkish names of the note player (`playNote` and friends of `kojo.Builtins`; the desktop's
 * `notaÇal` of `trInit`).
 *
 * `nota` is a MIDI pitch (0-127; 69 = A at 440 Hz), `ses` a volume (0-127). The calls do not wait: as on the
 * desktop, consecutive calls are queued and make a melody. The `Çalgı` codes are MIDI program numbers (the
 * English `Instrument`); in the browser they are mapped roughly to a waveform.
 *
 * The player itself is the one of `Builtins`, so that `notaÇal` and `playNote` share one time cursor and a
 * script that mixes the two plays its notes in order.
 */
trait SesYöntemleri extends TemelTürler {
  object Çalgı {
    val Piyano = 0
    val AkustikKuyruklu = 0
    val ParlakAkustik = 1
    val ElektroKuyruklu = 2
    val HonkyTonkPiyano = 3
    val ElektroPiyano = 4
    val AkustikBas = 32
    val Kuş = 123
    val Telefon = 124
    val Helikopter = 125
    val Alkış = 126
    val Tabanca = 127
  }

  protected def notaÇalar: kojo.NotePlayer

  /** Turns a MIDI pitch into a frequency (69 → 440 Hz). */
  def notaFrekansı(nota: Sayı): Kesir = kojo.NotePlayer.frequency(nota)

  /** Maps a MIDI instrument code to a Web Audio waveform (a rough approximation). */
  def çalgıDalgası(kod: Sayı): Yazı = kojo.NotePlayer.waveform(kod)

  def notaÇalgısınıKur(çalgı: Sayı): Birim = {
    require(çalgı >= 0 && çalgı <= 127, "çalgı 0 ile 127 arasında olmalı")
    notaÇalar.setInstrument(çalgı)
  }

  /** Does not wait: the note plays when the one before it has ended (at once if nothing is playing). */
  def notaÇal(nota: Sayı, süreMiliSaniye: Sayı, ses: Sayı = 80): Birim = {
    require(nota >= 0 && nota <= 127, "nota 0 ile 127 arasında olmalı")
    require(ses >= 0 && ses <= 127, "ses 0 ile 127 arasında olmalı")
    notaÇalar.playNote(nota, süreMiliSaniye, ses)
  }

  /**
   * All the notes start at the same moment and last `süreMiliSaniye`; the next note starts after that. The
   * volume is divided by the square root of the number of notes, so a chord is as loud as one note.
   */
  def akorÇal(notalar: Diz[Sayı], süreMiliSaniye: Sayı, ses: Sayı = 80): Birim = {
    notalar.foreach(nota => require(nota >= 0 && nota <= 127, "nota 0 ile 127 arasında olmalı"))
    require(ses >= 0 && ses <= 127, "ses 0 ile 127 arasında olmalı")
    notaÇalar.playChord(notalar, süreMiliSaniye, ses)
  }

  /**
   * The `(nota, süreMiliSaniye)` pairs all start at the same moment and each plays for its own duration;
   * the next note starts when the longest has ended. For a melody together with a bass:
   * `beraberÇal(Dizi((67, 500), (43, 1000)))`.
   */
  def beraberÇal(süreliNotalar: Diz[(Sayı, Sayı)], ses: Sayı = 80): Birim = {
    süreliNotalar.foreach { case (nota, _) => require(nota >= 0 && nota <= 127, "nota 0 ile 127 arasında olmalı") }
    require(ses >= 0 && ses <= 127, "ses 0 ile 127 arasında olmalı")
    notaÇalar.playTogether(süreliNotalar, ses)
  }

  /** A silent wait (a rest): the next note starts `süreMiliSaniye` later. */
  def notaSus(süreMiliSaniye: Sayı): Birim = notaÇalar.playRest(süreMiliSaniye)

  /** Silences all the queued and playing notes and resets the cursor: the next `notaÇal` plays at once. */
  def notaÇalarıKapat(): Birim = notaÇalar.stop()

  /** The same as `notaÇalarıKapat`. */
  def notaÇalarıDurdur(): Birim = notaÇalarıKapat()

  /** The time (ms) until the queued notes have ended, 0 if nothing is playing. */
  def kalanNotaSüresi: Sayı = notaÇalar.timeLeftMillis
}
