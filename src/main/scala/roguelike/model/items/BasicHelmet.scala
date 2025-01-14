package roguelike.model.items

import scalafx.scene.paint.Color
import roguelike.model.characters.Entity
import roguelike.model.characters.Player

case class BasicHelmet() extends Helmet(
  "Basic Helmet",
  "A basic helmet.",
  '∆',
  3,
  "basic_helmet.png"
) {
  override val imageName: String = "basic_helmet.png"
  override val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.Silver)
  override def use(target: Entity): Unit = {
    target match {
      case player: Player =>
        player.equip(this) // Call equip to handle equipment logic
        println(s"${player.name} equipped $name")
      case _ =>
        println("Cannot equip this item.")
    }
  }
  override def equipmentSlot: Option[String] = Some("Helmet")
  override def getColor(): SerializableColor = color
}