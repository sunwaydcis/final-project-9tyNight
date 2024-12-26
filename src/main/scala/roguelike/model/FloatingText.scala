package roguelike.model

import scalafx.scene.paint.Color

case class FloatingText(
                         var text: String,
                         var x: Int,
                         var y: Int,
                         var color: Color,
                         var duration: Int,
                         var opacity: Double = 1.0
                       ) {

  def update(): Unit = {
    y -= 1
    duration -= 1
    opacity -= 0.02
    if (opacity < 0) opacity = 0
  }

  def isVisible(): Boolean = {
    duration > 0
  }
}