package kojo

import pixiscalajs.PIXI.{Graphics, Point}

import scala.collection.mutable.ArrayBuffer
import scala.language.implicitConversions

object VertexShapeSupport {
  val curveTightness = 0f
  val s = curveTightness

  object curveToBezierMatrix {
    val m00 = 0.0f; val m01 = 1.0f; val m02 = 0.0f; val m03 = 0.0f
    val m10 = -1.0f / 6; val m11 = 1.0f; val m12 = 1.0f / 6; val m13 = 0.0f
    val m20 = 0.0f; val m21 = 1.0f / 6; val m22 = 1.0f; val m23 = -1.0f / 6
    val m30 = 0.0f; val m31 = 0.0f; val m32 = 1.0f; val m33 = 0.0f

    def mult(source: Array[Float], target: Array[Float]): Unit = {
      target(0) = m00 * source(0) + m01 * source(1) + m02 * source(2) + m03 * source(3)
      target(1) = m10 * source(0) + m11 * source(1) + m12 * source(2) + m13 * source(3)
      target(2) = m20 * source(0) + m21 * source(1) + m22 * source(2) + m23 * source(3)
      target(3) = m30 * source(0) + m31 * source(1) + m32 * source(2) + m33 * source(3)
    }
  }
}

trait VertexShapeSupport {
  def shapeDone(path: Graphics): Unit
  def shapePath = new Graphics

  private sealed trait ShapeVertex
  private case class Vertex(x: Double, y: Double) extends ShapeVertex
  private case class CurveVertex(x: Double, y: Double) extends ShapeVertex
  private case class QuadVertex(x1: Double, y1: Double, x2: Double, y2: Double) extends ShapeVertex
  private case class BezierVertex(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double) extends ShapeVertex

  private var shapeVertices: collection.mutable.ArrayBuffer[ShapeVertex] = _
  private var curveCoordX: Array[Float] = _
  private var curveCoordY: Array[Float] = _
  private var curveDrawX: Array[Float] = _
  private var curveDrawY: Array[Float] = _
  private var firstVertex = true

  private lazy val curveInit = {
    curveCoordX = new Array[Float](4)
    curveCoordY = new Array[Float](4)
    curveDrawX = new Array[Float](4)
    curveDrawY = new Array[Float](4)
  }

  private def checkBegin(): Unit = {
    require(shapeVertices != null, "Do a beginShape() before adding vertices to a shape")
  }

  def beginShape(): Unit = {
    shapeVertices = ArrayBuffer.empty[ShapeVertex]
    firstVertex = true
  }

  def vertex(x: Double, y: Double): Unit = {
    checkBegin()
    shapeVertices.append(Vertex(x, y))
  }

  def quadraticVertex(cx: Double, cy: Double, x2: Double, y2: Double): Unit = {
    checkBegin()
    shapeVertices.append(QuadVertex(cx, cy, x2, y2))
  }

  def bezierVertex(cx1: Double, cy1: Double, cx2: Double, cy2: Double, x2: Double, y2: Double): Unit = {
    checkBegin()
    shapeVertices.append(BezierVertex(cx1, cy1, cx2, cy2, x2, y2))
  }

  def curveVertex(x: Double, y: Double): Unit = {
    checkBegin()
    shapeVertices.append(CurveVertex(x, y))
  }

  private def rtToXy(r: Double, theta: Double): Point = {
    val t = theta.toRadians
    val x = r * math.cos(t)
    val y = r * math.sin(t)
    Point(x, y)
  }

  def vertexRt(r: Double, theta: Double): Unit = {
    val p = rtToXy(r, theta)
    vertex(p.x, p.y)
  }

  def curveVertexRt(r: Double, theta: Double): Unit = {
    val p = rtToXy(r, theta)
    curveVertex(p.x, p.y)
  }

  private def curveVertexSegment(gpath: Graphics, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float): Unit = {
    curveInit

    curveCoordX(0) = x1
    curveCoordY(0) = y1
    curveCoordX(1) = x2
    curveCoordY(1) = y2
    curveCoordX(2) = x3
    curveCoordY(2) = y3
    curveCoordX(3) = x4
    curveCoordY(3) = y4

    import kojo.VertexShapeSupport.curveToBezierMatrix
    curveToBezierMatrix.mult(curveCoordX, curveDrawX)
    curveToBezierMatrix.mult(curveCoordY, curveDrawY)
    if (firstVertex) {
      gpath.moveTo(curveDrawX(0), curveDrawY(0))
      firstVertex = false
    }
    gpath.bezierCurveTo(curveDrawX(1), curveDrawY(1), curveDrawX(2), curveDrawY(2), curveDrawX(3), curveDrawY(3))
  }

  def endShape() = {
    require(shapeVertices.length > 0, "A shape should have at least one vertex")

    def checkCurveFirstVertex(): Unit = {
      if (firstVertex) {
        throw new RuntimeException("A curved shape should start with a vertex")
      }
    }

    val tempPath = shapePath
    val curveVertices = ArrayBuffer.empty[CurveVertex]
    shapeVertices.foreach {
      case Vertex(x, y) =>
        if (firstVertex) {
          tempPath.moveTo(x, y)
          firstVertex = false
        }
        else {
          tempPath.lineTo(x, y)
        }
      case cv @ CurveVertex(_, _) =>
        curveVertices.append(cv)
        val cvlen = curveVertices.length
        if (cvlen > 3) {
          curveVertexSegment(
            tempPath,
            curveVertices(cvlen - 4).x.toFloat, curveVertices(cvlen - 4).y.toFloat,
            curveVertices(cvlen - 3).x.toFloat, curveVertices(cvlen - 3).y.toFloat,
            curveVertices(cvlen - 2).x.toFloat, curveVertices(cvlen - 2).y.toFloat,
            curveVertices(cvlen - 1).x.toFloat, curveVertices(cvlen - 1).y.toFloat
          )
        }
      case QuadVertex(x1, y1, x2, y2) =>
        checkCurveFirstVertex(); tempPath.quadraticCurveTo(x1, y1, x2, y2)
      case BezierVertex(x1, y1, x2, y2, x3, y3) =>
        checkCurveFirstVertex(); tempPath.bezierCurveTo(x1, y1, x2, y2, x3, y3)
    }
    shapeVertices = null
    shapeDone(tempPath)
  }
}

class VertexShape(val path: Graphics) extends VertexShapeSupport {
  def shapeDone(path2: Graphics): Unit = {}
  override def shapePath: Graphics = path
}
