package roguelike.model.items

import scalafx.scene.paint.Color
import roguelike.model.characters.Entity
import roguelike.model.characters.Player

case class IronChestplate() extends Chestplate(
  "Iron Chestplate",
  "A heavy iron chestplate.",
  '[',
  20,
  "iron_chestplate.png"
) {
  override val imageName: String = "iron_chestplate.png"
  override val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.LightSteelBlue)
  override def use(target: Entity): Unit = {
    target match {
      case player: Player =>
        player.equip(this)
        println(s"${player.name} equipped $name")
      case _ =>
        println("Cannot equip this item.")
    }
  }

  override def equipmentSlot: Option[String] = Some("Chestplate")
  override def getColor(): SerializableColor = color
}
