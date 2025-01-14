package roguelike.model.items

import scalafx.scene.paint.Color

case class StrengthPendant() extends Pendant("Strength Pendant", "A magical pendant that permanently increases attack.", 'P', 10) {
  override val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.Red)
  override val imageName: String = "strength_pendant.png"
  override def equipmentSlot: Option[String] = Some("Pendant")
  override def getColor(): SerializableColor = color
}

