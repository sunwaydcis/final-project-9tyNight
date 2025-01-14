package roguelike.model.items

import roguelike.model.characters.Entity
import scalafx.scene.paint.Color

abstract class Pendant(
                        val name: String,
                        val description: String,
                        val symbol: Char,
                        val attack: Int
                      ) extends Item {
  override val duration: Int = 0
  override val imageName: String = ""
  override def use(target: Entity): Unit = {
    target.stats.attack += attack
    println(
      s"${target.name} equipped ${this.name} and gained $attack attack permanently!"
    )
  }

  override def duration_=(newDuration: Int): Unit = {}
  
  override def equipmentSlot: Option[String] = Some("Pendant")

  val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.Red)
  override def getColor(): SerializableColor = color
}