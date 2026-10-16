package kojo

/**
 * A font: the browser's counterpart of desktop Kojo's `Font(name, size)`.
 *
 * There is no AWT here, so the font maps to a PIXI/canvas text style (font family and size). `style` is
 * the bit mask of java.awt.Font (0 plain, 1 bold, 2 italic), which CanvasDraw applies to the text it draws.
 */
final case class Font(name: String, size: Int, style: Int = 0)
