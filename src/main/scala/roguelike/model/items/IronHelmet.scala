package roguelike.model.items

import scalafx.scene.paint.Color
import roguelike.model.characters.Entity
import roguelike.model.characters.Player

case class IronHelmet() extends Helmet(
  "Iron Helmet",
  "A heavy iron helmet.",
  '∆',
  12,
  "iron_helmet.png"
) {
  override val imageName: String = "iron_helmet.png"
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