package roguelike.model.items

import roguelike.model.characters.Entity
import scalafx.scene.paint.Color

abstract class Ring(
                     val name: String,
                     val description: String,
                     val symbol: Char,
                     val health: Int
                   ) extends Item {
  override val duration: Int = 0
  override val imageName: String = ""
  override def use(target: Entity): Unit = {
    // Apply the permanent health boost
    target.stats.health += health
    println(
      s"${target.name} equipped ${this.name} and gained $health health permanently!"
    )
  }

  override def duration_=(newDuration: Int): Unit = {}
  
  override def equipmentSlot: Option[String] = Some("Ring")

  val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.Green)
  override def getColor(): SerializableColor = color
}