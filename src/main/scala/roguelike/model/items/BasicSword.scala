package roguelike.model.items

import scalafx.scene.paint.Color
import roguelike.model.characters.Entity
import roguelike.model.characters.Player

case class BasicSword() extends Weapon("Basic Sword", "A simple sword.", '/', 10) {
  override val imageName: String = "basic_weapon.png"
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
  override def getColor(): SerializableColor = ColorHelper.fromScalaFXColor(Color.LightGray)
}
