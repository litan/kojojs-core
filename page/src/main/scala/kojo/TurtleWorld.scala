package kojo

import org.scalajs.dom.{document, html, window}
import pixiscalajs.PIXI
import pixiscalajs.PIXI.{CanvasRenderer, RendererOptions}

import scala.scalajs.js

class TurtleWorld {
  val fiddleContainer =
    document.getElementById("fiddle-container").asInstanceOf[html.Div]
  val canvas_holder =
    document.getElementById("canvas-holder").asInstanceOf[html.Div]
  val (width, height) =
    (fiddleContainer.clientWidth, fiddleContainer.clientHeight)
  val renderer = PIXI.Pixi.autoDetectRenderer(width, height, rendererOptions())
//  val renderer = new CanvasRenderer(width, height, rendererOptions())
  val stage = new PIXI.Container()
  init()

  def init() {
    render()
    canvas_holder.appendChild(renderer.view)
    stage.name = "Stage"
    stage.width = width
    stage.height = height
    stage.interactive = true
    stage.setTransform(width / 2, height / 2, 1, -1, 0, 0, 0, 0, 0)
  }

  def addTurtleLayer(layer: PIXI.Container): Unit = {
    stage.addChild(layer)
  }

  def scheduleLater(fn: () => Unit): Unit = {
    window.requestAnimationFrame { t =>
      fn()
    }
    //      window.setTimeout(fn, 10)
  }

  def addSprite(sprite: PIXI.Sprite): Unit = {
    def endFrame(): Unit = {
      stage.addChild(sprite)
      render()
    }

    def frame(): Unit = {
      scheduleLater(endFrame)
      render()
    }

    scheduleLater(frame)
  }

  def render(): Unit = {
    renderer.render(stage)
  }

  def rendererOptions(antialias: Boolean = true,
                      resolution: Double = 1,
                      backgroundColor: Int = 0xFFFFFF,
                      clearBeforeRender: Boolean = true): RendererOptions = {
    js.Dynamic
      .literal(antialias = antialias,
               resolution = resolution,
               backgroundColor = backgroundColor,
               clearBeforeRender = clearBeforeRender)
      .asInstanceOf[RendererOptions]
  }
}
