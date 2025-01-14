package roguelike.model.items

import scalafx.scene.paint.Color
import roguelike.model.characters.Entity
import roguelike.model.characters.Player

case class SteelHelmet() extends Helmet(
  "Steel Helmet",
  "A sturdy steel helmet.",
  '∆',
  6,
  "steel_helmet.png"
) {
  override val imageName: String = "steel_helmet.png"
  override val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.Silver)
  override def use(target: Entity): Unit = {
    target match {
      case player: Player =>
        player.equip(this)
        println(s"${player.name} equipped $name")
      case _ =>
        println("Cannot equip this item.")
    }
  }
  override def equipmentSlot: Option[String] = Some("Helmet")
  override def getColor(): SerializableColor = color
}
