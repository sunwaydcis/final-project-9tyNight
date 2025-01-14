package roguelike.model.items

import scalafx.scene.paint.Color
import roguelike.model.characters.Entity
import roguelike.model.characters.Player

case class SteelShield() extends Shield(
  "Steel Shield",
  "A sturdy steel shield.",
  'Θ',
  10,
  "steel_shield.png"
){
  override val imageName: String = "steel_shield.png"
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
