package kojo

import com.vividsolutions.jts.geom.Geometry
import kojo.Utils.newCoordinate
import kojo.doodle.Color
import org.scalajs.dom
import pixiscalajs.PIXI

import scala.scalajs.js

/**
 * A sketch picture: a single HTML `canvas` and a single PIXI `Sprite` (the counterpart of desktop
 * Kojo's Java2DPic).
 *
 * What is drawn is PIXELS, and the surface is NOT cleared between frames (if `background` is not called,
 * the drawings pile up; that is how you leave a trail). There is always a single node on the stage, however
 * long the drawing goes on: no Picture objects pile up, so `PicCache` and the stage do not grow.
 *
 * TIMING (the same as on the desktop): `setup` runs once, when the picture is drawn for the FIRST time,
 * and `drawLoop` runs on every frame. `noLoop()` stops only the loop of THIS sketch; the animation of the
 * world goes on.
 *
 * TEXTURE: `PIXI.Texture.from` is NOT used (it enters the texture cache and never leaves). On every frame
 * the surface is uploaded again with `baseTexture.update()`.
 *
 * @param scaleOutFactor the surface is `width × f` by `height × f` pixels and the sprite is scaled by 1/f
 *                       (for thinner lines). The user's coordinates are surface pixels.
 *                       `0` = AUTOMATIC: the screen density (`devicePixelRatio`), at most 2, and small
 *                       enough for the texture limit of the device (see `SketchPic.autoFactor`). Why 2:
 *                       measured on two phones, x2 is smooth on both, x3 is over the frame budget
 *                       (16.7 ms) on a mid-range device. The default stays 1, because an explicit f changes
 *                       the coordinate space (`canvasBounds` becomes surface pixels). In the automatic
 *                       mode `scale(f)` is applied to the context once: the coordinates stay stage units
 *                       (the same script is at the same place and has the same size on every device) and
 *                       only the sharpness grows. `cd.width` and `cd.height` are stage units too.
 */
class SketchPic(
    setup: CanvasDraw => Unit,
    drawLoop: CanvasDraw => Unit,
    scaleOutFactor: Double = 1,
    seedFn: Long => Unit = _ => ()
)(implicit val kojoWorld: KojoWorld)
    extends Picture
    with ReadyPromise {

  private val f =
    if (scaleOutFactor > 0) scaleOutFactor
    else if (scaleOutFactor == 0)
      SketchPic.autoFactor(
        dom.window.devicePixelRatio,
        kojoWorld.canvasWidth,
        kojoWorld.canvasHeight,
        SketchPic.maxTextureSize
      )
    else 1.0
  private val surfaceWidth = math.max(1, math.round(kojoWorld.canvasWidth * f).toInt)
  private val surfaceHeight = math.max(1, math.round(kojoWorld.canvasHeight * f).toInt)

  /** The chosen factor (for the tests and for the explanation of `canvasBounds`). */
  private[kojo] def factor: Double = f

  private val canvas: dom.html.Canvas = {
    val c = dom.document.createElement("canvas").asInstanceOf[dom.html.Canvas]
    c.width = surfaceWidth
    c.height = surfaceHeight
    c
  }

  /** The tests reach the raw canvas here, to read pixels. */
  private[kojo] def canvasElement: dom.html.Canvas = canvas

  /** The surface that is given to the user. */
  val cd = new CanvasDraw(canvas, surfaceWidth, surfaceHeight, seedFn, if (scaleOutFactor == 0) f else 1.0)

  private var baseTexture: js.Dynamic = _

  private val sprite: PIXI.Sprite = {
    val P = js.Dynamic.global.PIXI
    // No mipmaps: PIXI 5 takes an option (MIPMAP_MODES.OFF), PIXI 4 a boolean property
    val base =
      if (js.isUndefined(P.MIPMAP_MODES))
        js.Dynamic.newInstance(P.BaseTexture)(canvas.asInstanceOf[js.Any])
      else
        js.Dynamic.newInstance(P.BaseTexture)(canvas.asInstanceOf[js.Any], js.Dynamic.literal(mipmap = P.MIPMAP_MODES.OFF))
    if (js.isUndefined(P.MIPMAP_MODES)) base.mipmap = false
    baseTexture = base
    val texture = js.Dynamic.newInstance(P.Texture)(base)
    val s = js.Dynamic.newInstance(P.Sprite)(texture).asInstanceOf[PIXI.Sprite]
    // On the stage, where y is up, row i of the raw canvas is at y = i on the stage -- see CanvasDraw (as in Java2DPic)
    s.setTransform(0, 0, 1 / f, 1 / f, 0, 0, 0, 0, 0)
    s
  }
  private val layer = new PIXI.Container
  layer.addChild(sprite)
  val tnode = layer

  private var isSetUp = false
  // On every (re)draw a new loop is set up; the old ones become ineffective by comparing `generation`.
  // Why: KojoWorld.animate has no handle to stop a loop; a sketch that was erased and drawn again after
  // `stop()` did not come back to life, because the loop had been set up once. The old chains (dead or alive)
  // return empty on every frame and go on until the world is closed or the animation stops -- this is the
  // general behavior of animate; the memory of the surface is held until then too.
  private var generation = 0
  private var erased = false

  makeDone()

  private def upload(): Unit = baseTexture.update()

  /** One frame: `drawLoop` plus the texture upload. The tests call it to drive frames without an animation. */
  private[kojo] def step(): Unit = {
    if (!erased && cd.loop) {
      drawLoop(cd)
      upload()
      kojoWorld.render()
    }
  }

  override def realDraw(): Unit = {
    erased = false
    if (!isSetUp) {
      isSetUp = true
      setup(cd)
      upload()
    }
    kojoWorld.addLayer(layer)
    generation += 1
    val current = generation
    kojoWorld.animate { if (current == generation) step() }
  }

  def erase(): Unit = {
    erased = true
    kojoWorld.removeLayer(layer)
    // Let go of the GL side: if the surface is drawn again it loads itself again
    if (js.typeOf(baseTexture.dispose) == "function") baseTexture.dispose()
  }

  override def setFillColor(c: Color): Unit = {}
  override def setPenColor(c: Color): Unit = {}
  override def setPenThickness(t: Double): Unit = {}

  override def initGeom(): Geometry = {
    import scala.scalajs.js.JSConverters._
    val w = surfaceWidth / f
    val h = surfaceHeight / f
    val outline = Seq(
      newCoordinate(0, 0), newCoordinate(0, h), newCoordinate(w, h), newCoordinate(w, 0), newCoordinate(0, 0)
    )
    Utils.Gf.createLineString(outline.toJSArray)
  }

  def copy: Picture = new SketchPic(setup, drawLoop, scaleOutFactor, seedFn)
}

object SketchPic {
  /** The upper limit of the automatic factor. x2 was smooth on two phones, x3 not on a mid-range one. */
  val MaxAutoFactor = 2.0

  /**
   * The factor for `scaleOutFactor = 0`: `min(dpr, 2)`, small enough that the longest side of the surface
   * does not pass the texture limit of the device; never below 1. A pure function: the DPR and the limit
   * are parameters, for the tests.
   */
  def autoFactor(dpr: Double, width: Double, height: Double, textureLimit: Double): Double = {
    val density = if (dpr >= 1) dpr else 1.0
    val edge = math.max(1.0, math.max(width, height))
    val fitting = if (textureLimit > 0) textureLimit / edge else Double.PositiveInfinity
    math.max(1.0, math.min(math.min(density, MaxAutoFactor), fitting))
  }

  /** The largest texture size of the device; asked once. Without WebGL, 4096 is assumed (the smallest common limit). */
  lazy val maxTextureSize: Double =
    try {
      val c = dom.document.createElement("canvas").asInstanceOf[dom.html.Canvas]
      val gl = c.getContext("webgl").asInstanceOf[js.Dynamic]
      if (js.isUndefined(gl) || gl == null) 4096.0
      else {
        val n = gl.getParameter(gl.MAX_TEXTURE_SIZE).asInstanceOf[Double]
        val loseExt = gl.getExtension("WEBGL_lose_context")
        if (!js.isUndefined(loseExt) && loseExt != null) loseExt.loseContext()
        if (n > 0) n else 4096.0
      }
    } catch { case _: Throwable => 4096.0 }
}
