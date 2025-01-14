package roguelike.model.items

import scalafx.scene.paint.Color
import roguelike.model.characters.Entity
import roguelike.model.characters.Player

case class BasicChestplate() extends Chestplate(
  "Basic Chestplate",
  "A sturdy steel chestplate.",
  '[',
  5,
  "basic_chestplate.png"
) {
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

  private var _duration: Int = 0
  override def duration: Int = _duration

  override def duration_=(newDuration: Int): Unit = {
    _duration = newDuration
  }
}