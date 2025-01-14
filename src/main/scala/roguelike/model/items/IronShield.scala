package roguelike.model.items

import scalafx.scene.paint.Color
import roguelike.model.characters.Entity
import roguelike.model.characters.Player

case class IronShield() extends Shield(
  "Iron Shield",
  "A heavy iron shield.",
  'Θ',
  15,
  "iron_shield.png"
){
  override val imageName: String = "iron_shield.png"
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