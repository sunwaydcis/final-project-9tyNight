package roguelike.model.items

import roguelike.model.characters.Entity
import scalafx.scene.paint.Color
import roguelike.model.characters.Player

abstract class Weapon(
                       val name: String,
                       val description: String,
                       val symbol: Char,
                       val attack: Int
                     ) extends Item {
  override val duration: Int = 0
  override val imageName: String = ""
  override def use(target: Entity): Unit = {
    target match {
      case player: Player =>
        player.equip(this) 
        println(s"${player.name} equipped $name")
      case _ =>
        println("Cannot equip this item.")
    }
  }

  override def duration_=(newDuration: Int): Unit = {}
  

  override def equipmentSlot: Option[String] = Some("Weapon")
  val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.LightGray)
  override def getColor(): SerializableColor = color
}