package kojo.tr

/**
 * The Turkish names of the key codes -- for games, like `tuşBasılıMı(tuşlar.sol)`.
 *
 * THE VALUES ARE DOM `KeyboardEvent.keyCode` values -- NOT the AWT VK_* values of the desktop (and of
 * the web Kojo's own `KeyCodes` class).
 *
 * Why: `KojoWorld.isKeyPressed` compares with the `e.keyCode` that the browser gives. The arrow keys,
 * letters, digits, space and escape are the same in the two tables, but Enter (AWT 10 / DOM 13) and the
 * punctuation keys differ: with the AWT value `tuşBasılıMı(tuşlar.gir)` would NEVER be true.
 */
trait KlavyeYöntemleri extends TemelTürler {
  object tuşlar {
    val gir = 13 // DOM; 10 in AWT
    val silGeri = 8
    val sekme = 9
    val iptal = 0x03
    val temizle = 0x0c
    val kaldırma = 0x10 // shift
    val kontrol = 0x11
    val alt = 0x12
    val dur = 0x13
    val büyükHarfKilidi = 0x14
    val çık = 0x1b // escape
    val kaç = çık
    val boşluk = 0x20
    val sayfaYukarı = 0x21
    val sayfaAşağı = 0x22
    val satırSonu = 0x23
    val satırBaşı = 0x24
    val ev = satırBaşı

    val sol = 0x25
    val yukarı = 0x26
    val sağ = 0x27
    val aşağı = 0x28

    val virgül = 188 // DOM; 0x2c in AWT
    val eksi = 189
    val nokta = 190
    val bölü = 191
    val noktalıVirgül = 186
    val eşittir = 187

    val n0 = 0x30; val n1 = 0x31; val n2 = 0x32; val n3 = 0x33; val n4 = 0x34
    val n5 = 0x35; val n6 = 0x36; val n7 = 0x37; val n8 = 0x38; val n9 = 0x39

    val a = 0x41; val b = 0x42; val c = 0x43; val d = 0x44; val e = 0x45
    val f = 0x46; val g = 0x47; val h = 0x48; val i = 0x49; val j = 0x4a
    val k = 0x4b; val l = 0x4c; val m = 0x4d; val n = 0x4e; val o = 0x4f
    val p = 0x50; val q = 0x51; val r = 0x52; val s = 0x53; val t = 0x54
    val u = 0x55; val v = 0x56; val w = 0x57; val x = 0x58; val y = 0x59
    val z = 0x5a

    // --- The English aliases of the desktop ----------------------------------
    //
    // The desktop (kojo tr/klavye.scala) gives these names too, because that file was ported from AWT's
    // KeyCodes.scala; a script that ran there would fail here with "value escape is not a member of ...".
    //
    // The aliases are bound to the TURKISH names, no numbers are written: as the note at the top says, the
    // values here are DOM keyCodes and the desktop's are AWT VK_*. Copying the desktop's numbers would
    // make `tuşBasılıMı(tuşlar.enter)` never true (AWT 10 / DOM 13).
    val enter = gir
    val backSpace = silGeri
    val cancel = iptal
    val clear = temizle
    val shift = kaldırma
    val control = kontrol
    val pause = dur
    val escape = çık
    val pageUp = sayfaYukarı
    val pageDown = sayfaAşağı
    val end = satırSonu
    val home = satırBaşı

    // --- The deprecated snake_case spellings of three English names ----------
    //
    // Desktop Kojo still gives `back_space`, `page_up` and `page_down`, so a script written there uses
    // them; they keep compiling here (with a deprecation warning) and give the same key.
    @deprecated("use camelCase: backSpace", "2026")
    val back_space = backSpace
    @deprecated("use camelCase: pageUp", "2026")
    val page_up = pageUp
    @deprecated("use camelCase: pageDown", "2026")
    val page_down = pageDown
  }
}
