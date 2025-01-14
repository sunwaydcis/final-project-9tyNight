package roguelike.model.items

import scalafx.scene.paint.Color
import roguelike.model.characters.Entity
import roguelike.model.characters.Player

case class BasicShield() extends Shield(
  "Basic Shield",
  "A basic shield.",
  'Θ',
  5,
  "basic_shield.png"
) {
  override val imageName: String = "basic_shield.png"
  override val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.LightBlue)
  override def use(target: Entity): Unit = {
    target match {
      case player: Player =>
        player.equip(this)
        println(s"${player.name} equipped $name")
      case _ =>
        println("Cannot equip this item.")
    }
  }
  override def equipmentSlot: Option[String] = Some("Shield")
  override def getColor(): SerializableColor = color
}