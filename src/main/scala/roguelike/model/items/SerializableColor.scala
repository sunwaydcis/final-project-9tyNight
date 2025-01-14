package roguelike.model.items

import scalafx.scene.paint.Color
import java.io.Serializable

case class SerializableColor(red: Int, green: Int, blue: Int, alpha: Double) extends Serializable

object ColorHelper {
  def toScalaFXColor(sc: SerializableColor): Color = {
    Color.rgb(sc.red, sc.green, sc.blue, sc.alpha)
  }

  def fromScalaFXColor(c: Color): SerializableColor = {
    SerializableColor((c.red * 255).toInt, (c.green * 255).toInt, (c.blue * 255).toInt, c.opacity)
  }
}