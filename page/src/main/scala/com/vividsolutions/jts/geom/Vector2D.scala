package com.vividsolutions.jts.geom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("jsts.geom.Vector2D")
class Vector2D extends js.Object {
  def this(x: Double, y: Double) = this()
  def getX(): Double = js.native
  def getY(): Double = js.native
  def rotate(angle: Double): Vector2D = js.native

  def add(other: Vector2D): Vector2D = js.native
  def subtract(other: Vector2D): Vector2D = js.native
  def multiply(factor: Double): Vector2D = js.native
  def divide(factor: Double): Vector2D = js.native

  def dot(v: Vector2D): Double = js.native
  def distance(v: Vector2D): Double = js.native

  def normalize(): Vector2D = js.native
  def length(): Double = js.native
  def lengthSquared(): Double = js.native

  def weightedSum(v: Vector2D, frac: Double): Vector2D = js.native
  def angle: Double = js.native
  def angle(v: Vector2D): Double = js.native
  def angleTo(v: Vector2D): Double = js.native
  def negate(): Vector2D = js.native
}
