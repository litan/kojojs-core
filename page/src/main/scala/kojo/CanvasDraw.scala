package kojo

import kojo.doodle.Color
import org.scalajs.dom

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js

/**
 * The surface of a sketch: the browser's counterpart of desktop Kojo's `CanvasDraw` (lite/CanvasDraw.scala).
 * It draws on the 2D context of an HTML `canvas` instead of a Java2D `Graphics2D`. A sketch is a single
 * picture and what it draws is PIXELS: objects do not pile up, so it can draw as much as it likes
 * (see SketchPic).
 *
 * COORDINATES -- the same as on the desktop, measured: `Graphics2D` works in raw image coordinates (y
 * down); the image is put on the stage, where y is UP, with `drawImage(img, 0, 0)`, so row i of the image
 * is at y = i on the stage. The result: in a sketch (x, y) is the point (x, y) of the stage, but the image
 * is upside down on the stage. We do the same: the context is raw and the sprite is not transformed
 * (see SketchPic). So that text does not look upside down it is drawn with `scale(1, -1)`, as on the desktop.
 *
 * Behaviors that stay the same as on the desktop (read from CanvasDraw.scala, pinned down by tests):
 *  - `rect(x1, y1, x2, y2)`: the 3rd and 4th values are NOT a corner but the width and the height
 *    (Rectangle2D.setRect).
 *  - `ellipse(cx, cy, w, h)`: w and h are the FULL size (Canvas wants radii, so half of them is passed).
 *  - `arc`: angles are in RADIANS; the meaning is that of Java's `Arc2D.OPEN` on raw (y down)
 *    coordinates: 0 is at 3 o'clock and a growing angle goes COUNTER-clockwise on the screen (on the
 *    canvas the angle is -a and `anticlockwise` is extent > 0).
 *  - the constants of the pen cap and join: `ROUND`, `PROJECT` (= square cap), `SQUARE` (= butt cap!) --
 *    the names of the desktop are kept on purpose (the CAP_SQUARE/CAP_BUTT mix-up).
 *  - `pushMatrix`/`popMatrix` save only the TRANSFORM (`save`/`restore` would save the paint and pen
 *    too; the desktop does not).
 *  - `background` moves the corners (0,0) and (w,h) by the inverse of the transform and fills the
 *    rectangle between them.
 *
 * Differences: there is no `drawShape(java.awt.Shape)` (no AWT); `blendMode` takes the name of the
 * canvas's `globalCompositeOperation` (`"lighter"`, `"multiply"`, ...) instead of a `java.awt.Composite`;
 * `image` does not exist yet.
 */
class CanvasDraw private[kojo] (
    canvas: dom.html.Canvas,
    surfaceWidth: Double,
    surfaceHeight: Double,
    seedFn: Long => Unit,
    // `scaleFactor` != 1: scale(scaleFactor) is applied to the context ONCE, so the user's coordinates are
    // stage units, not surface pixels (the automatic mode of SketchPic, `scaleOutFactor = 0`).
    // `width`/`height` are then in stage units; `background` paints the real surface.
    scaleFactor: Double = 1.0
) {
  private val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private val c = ctx.asInstanceOf[js.Dynamic]

  /** The width and height of the canvas that the user sees (surface pixels if `scaleFactor` is 1, stage units if not). */
  val width: Double = surfaceWidth / scaleFactor
  val height: Double = surfaceHeight / scaleFactor

  if (scaleFactor != 1.0) ctx.scale(scaleFactor, scaleFactor)

  // desktop: CapJoin.CAP_ROUND / CAP_SQUARE / CAP_BUTT, JOIN_MITER / JOIN_BEVEL
  val ROUND = 1
  val PROJECT = 2
  val SQUARE = 0
  val MITER = 0
  val BEVEL = 2

  private var fillCss: String = null
  private var strokeCss: String = Color.red.toCanvas
  private var strokeThickness = 2.0
  private var textFontValue: Font = Font("Sans Serif", 15)
  private var matrices = List.empty[js.Array[Double]]
  private var _loop = true

  // the defaults of the desktop: ROUND cap, MITER join, thickness 2
  c.lineCap = "round"
  c.lineJoin = "miter"
  c.lineWidth = strokeThickness

  def loop: Boolean = _loop
  def noLoop(): Unit = { _loop = false }

  def randomSeed(s: Long): Unit = seedFn(s)

  // --- paint and pen ---------------------------------------------------------
  private def rgbaCss(r: Int, g: Int, b: Int, a: Int): String = s"rgba($r, $g, $b, ${a / 255.0})"

  def noStroke(): Unit = { strokeCss = null }
  def noFill(): Unit = { fillCss = null }
  def fill(r: Int, g: Int, b: Int, a: Int = 255): Unit = { fillCss = rgbaCss(r, g, b, a) }
  def fill(col: Color): Unit = { fillCss = col.toCanvas }
  def stroke(r: Int, g: Int, b: Int, a: Int = 255): Unit = { strokeCss = rgbaCss(r, g, b, a) }
  def stroke(col: Color): Unit = { strokeCss = col.toCanvas }

  def strokeWeight(n: Double): Unit = {
    strokeThickness = n
    c.lineWidth = n
  }

  def strokeCap(n: Int): Unit = {
    c.lineCap = n match {
      case 0 => "butt"
      case 1 => "round"
      case 2 => "square"
      case _ => throw new IllegalArgumentException(s"unknown stroke cap: $n")
    }
  }

  def strokeJoin(n: Int): Unit = {
    c.lineJoin = n match {
      case 0 => "miter"
      case 1 => "round"
      case 2 => "bevel"
      case _ => throw new IllegalArgumentException(s"unknown stroke join: $n")
    }
  }

  /** `mode`: a name of the canvas `globalCompositeOperation` ("source-over", "lighter", "multiply", ...). */
  def blendMode(mode: String): Unit = { c.globalCompositeOperation = mode }

  // --- background ------------------------------------------------------------
  private def withCurrentPaint(css: String)(f: => Unit): Unit = {
    c.fillStyle = css
    f
  }

  def background(r: Int, g: Int, b: Int, a: Int = 255): Unit = backgroundCss(rgbaCss(r, g, b, a))
  def background(col: Color): Unit = backgroundCss(col.toCanvas)

  // desktop bgRectCoords: move the corners (0,0) and (w,h) by the INVERSE of the transform and fill the
  // rectangle between them. If the transform is rotated only these two corners are used (as on the desktop).
  private def backgroundCss(css: String): Unit = {
    val m = c.getTransform()
    val inv = m.inverse()
    val p0 = inv.transformPoint(js.Dynamic.literal(x = 0.0, y = 0.0))
    val p1 = inv.transformPoint(js.Dynamic.literal(x = surfaceWidth, y = surfaceHeight))
    val x0 = p0.x.asInstanceOf[Double]; val y0 = p0.y.asInstanceOf[Double]
    val x1 = p1.x.asInstanceOf[Double]; val y1 = p1.y.asInstanceOf[Double]
    withCurrentPaint(css) { ctx.fillRect(x0, y0, x1 - x0, y1 - y0) }
  }

  // --- shapes ----------------------------------------------------------------
  /** After the path is built: the fill (if any), then the pen (if any) -- the order of the desktop drawShape. */
  private def paint(): Unit = {
    if (fillCss != null) {
      c.fillStyle = fillCss
      ctx.fill()
    }
    if (strokeCss != null) {
      c.strokeStyle = strokeCss
      ctx.stroke()
    }
  }

  def ellipse(cx: Double, cy: Double, w: Double, h: Double): Unit = {
    ctx.beginPath()
    c.ellipse(cx, cy, w / 2, h / 2, 0.0, 0.0, 2 * math.Pi)
    paint()
  }

  /** `start` and `extent` are in radians; the frame is the same as in ellipse() (center, FULL width, FULL height). */
  def arc(cx: Double, cy: Double, w: Double, h: Double, start: Double, extent: Double): Unit = {
    ctx.beginPath()
    c.ellipse(cx, cy, w / 2, h / 2, 0.0, -start, -(start + extent), extent > 0)
    paint()
  }

  def line(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    ctx.beginPath()
    ctx.moveTo(x1, y1)
    ctx.lineTo(x2, y2)
    paint()
  }

  /** Careful: the 3rd and 4th values are the width and the height (not a corner), as on the desktop. */
  def rect(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    ctx.beginPath()
    ctx.rect(x1, y1, x2, y2)
    paint()
  }

  def point(x: Double, y: Double): Unit = line(x - 0.01 / 2, y, x + 0.01 / 2, y)

  // --- text ------------------------------------------------------------------
  def textFont(f: Font): Unit = { textFontValue = f }

  private def fontCss(f: Font): String = {
    val family = f.name.replace(" ", "").toLowerCase match {
      case "sansserif" | "dialog" | "dialoginput" => "sans-serif"
      case "serif"                                => "serif"
      case "monospaced"                           => "monospace"
      case _                                      => "\"" + f.name + "\""
    }
    val bold = if ((f.style & 1) != 0) "bold " else ""
    val italic = if ((f.style & 2) != 0) "italic " else ""
    s"$italic$bold${f.size}px $family"
  }

  // desktop drawText: the text is drawn in the PEN color, with y flipped (the stage is upside down).
  def text(s: String, x: Double, y: Double): Unit = {
    if (strokeCss != null) c.fillStyle = strokeCss
    c.font = fontCss(textFontValue)
    pushMatrix()
    scale(1, -1)
    ctx.fillText(s, x, -y)
    popMatrix()
  }

  // --- transforms ------------------------------------------------------------
  def translate(x: Double, y: Double): Unit = ctx.translate(x, y)
  def rotate(angle: Double): Unit = ctx.rotate(angle)
  def rotateDegrees(angle: Double): Unit = rotate(angle.toRadians)
  def scale(f: Double): Unit = ctx.scale(f, f)
  def scale(fx: Double, fy: Double): Unit = ctx.scale(fx, fy)

  def pushMatrix(): Unit = {
    val m = c.getTransform()
    matrices = js.Array[Double](
      m.a.asInstanceOf[Double], m.b.asInstanceOf[Double], m.c.asInstanceOf[Double],
      m.d.asInstanceOf[Double], m.e.asInstanceOf[Double], m.f.asInstanceOf[Double]
    ) :: matrices
  }

  def popMatrix(): Unit = {
    val m = matrices.head
    ctx.setTransform(m(0), m(1), m(2), m(3), m(4), m(5))
    matrices = matrices.tail
  }

  // --- shapes made of vertices (the canvas counterpart of vertexShapeSupport.scala) --
  private sealed trait ShapeVertex
  private case class Vertex(x: Double, y: Double) extends ShapeVertex
  private case class CurveVertex(x: Double, y: Double) extends ShapeVertex
  private case class QuadVertex(x1: Double, y1: Double, x2: Double, y2: Double) extends ShapeVertex
  private case class BezierVertex(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double)
      extends ShapeVertex
  private case class ArcVertex(x: Double, y: Double, angle: Double) extends ShapeVertex

  private var shapeVertices: ArrayBuffer[ShapeVertex] = _

  private def checkBegin(): Unit =
    require(shapeVertices != null, "Do a beginShape() before adding vertices to a shape")

  def beginShape(): Unit = { shapeVertices = ArrayBuffer.empty[ShapeVertex] }
  def vertex(x: Double, y: Double): Unit = { checkBegin(); shapeVertices.append(Vertex(x, y)) }
  def quadraticVertex(cx: Double, cy: Double, x2: Double, y2: Double): Unit = {
    checkBegin(); shapeVertices.append(QuadVertex(cx, cy, x2, y2))
  }
  def bezierVertex(cx1: Double, cy1: Double, cx2: Double, cy2: Double, x2: Double, y2: Double): Unit = {
    checkBegin(); shapeVertices.append(BezierVertex(cx1, cy1, cx2, cy2, x2, y2))
  }
  def curveVertex(x: Double, y: Double): Unit = { checkBegin(); shapeVertices.append(CurveVertex(x, y)) }
  def arcVertex(x: Double, y: Double, angle: Double): Unit = {
    checkBegin(); shapeVertices.append(ArcVertex(x, y, angle))
  }
  def vertexRt(r: Double, theta: Double): Unit = {
    val t = theta.toRadians
    vertex(r * math.cos(t), r * math.sin(t))
  }
  def curveVertexRt(r: Double, theta: Double): Unit = {
    val t = theta.toRadians
    curveVertex(r * math.cos(t), r * math.sin(t))
  }

  def endShape(): Unit = {
    require(shapeVertices != null && shapeVertices.nonEmpty, "A shape should have at least one vertex")
    var firstVertex = true
    var curX = 0.0
    var curY = 0.0
    def checkCurveFirstVertex(): Unit =
      if (firstVertex) throw new RuntimeException("A curved shape should start with a vertex")

    val coordX = new Array[Float](4)
    val coordY = new Array[Float](4)
    val drawX = new Array[Float](4)
    val drawY = new Array[Float](4)
    val curveVertices = ArrayBuffer.empty[CurveVertex]

    ctx.beginPath()
    shapeVertices.foreach {
      case Vertex(x, y) =>
        if (firstVertex) { ctx.moveTo(x, y); firstVertex = false }
        else ctx.lineTo(x, y)
        curX = x; curY = y
      case cv @ CurveVertex(_, _) =>
        curveVertices.append(cv)
        val n = curveVertices.length
        if (n > 3) {
          var i = 0
          while (i < 4) {
            coordX(i) = curveVertices(n - 4 + i).x.toFloat
            coordY(i) = curveVertices(n - 4 + i).y.toFloat
            i += 1
          }
          VertexShapeSupport.curveToBezierMatrix.mult(coordX, drawX)
          VertexShapeSupport.curveToBezierMatrix.mult(coordY, drawY)
          if (firstVertex) { ctx.moveTo(drawX(0), drawY(0)); firstVertex = false }
          ctx.bezierCurveTo(drawX(1), drawY(1), drawX(2), drawY(2), drawX(3), drawY(3))
          curX = drawX(3); curY = drawY(3)
        }
      case QuadVertex(x1, y1, x2, y2) =>
        checkCurveFirstVertex(); ctx.quadraticCurveTo(x1, y1, x2, y2); curX = x2; curY = y2
      case BezierVertex(x1, y1, x2, y2, x3, y3) =>
        checkCurveFirstVertex(); ctx.bezierCurveTo(x1, y1, x2, y2, x3, y3); curX = x3; curY = y3
      case ArcVertex(x, y, angle) =>
        checkCurveFirstVertex()
        arcTo(curX, curY, x, y, angle)
        curX = x; curY = y
    }
    shapeVertices = null
    paint()
  }

  // desktop Rich2DPath.arc: a circular arc of `angleOfArc` degrees from (curX,curY) to (x,y).
  private def arcTo(x1: Double, y1: Double, x2: Double, y2: Double, angleOfArc: Double): Unit = {
    val arcAngle = -(angleOfArc % 360)
    val mx = (x1 + x2) / 2
    val my = (y1 + y2) / 2
    val dx = (x2 - x1) / 2
    val dy = (y2 - y1) / 2
    val len = math.hypot(dx, dy)
    val ux = dx / len
    val uy = dy / len
    val lpx = uy
    val lpy = -ux
    val t =
      if (math.abs(arcAngle) == 0.0) len
      else if (math.abs(arcAngle) == 180.0) 0.0
      else len / math.tan(math.toRadians(arcAngle) / 2.0)
    val cx = mx + lpx * t
    val cy = my + lpy * t
    val radius = math.hypot(cx - x1, cy - y1)
    // Java: the start angle is in degrees, counter-clockwise, on raw coordinates = -atan2; on the canvas the angle = atan2
    val startAngle = math.atan2(y1 - cy, x1 - cx)
    c.ellipse(cx, cy, radius, radius, 0.0, startAngle, startAngle - math.toRadians(arcAngle), arcAngle > 0)
  }
}
