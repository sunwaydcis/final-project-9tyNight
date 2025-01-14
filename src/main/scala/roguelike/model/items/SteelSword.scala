package roguelike.model.items

import scalafx.scene.paint.Color
import roguelike.model.characters.Entity
import roguelike.model.characters.Player

case class SteelSword() extends Weapon("Steel Sword", "A strong steel sword.", '/', 20) {
  override val imageName: String = "steel_weapon.png"
  override val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.LightGray)
  override def use(target: Entity): Unit = {
    target match {
      case player: Player =>
        player.equip(this)
        println(s"${player.name} equipped $name")
      case _ =>
        println("Cannot equip this item.")
    }
  }
  override def equipmentSlot: Option[String] = Some("Weapon")
  override def getColor(): SerializableColor = color
}