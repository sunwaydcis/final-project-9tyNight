package roguelike.model.items

import roguelike.model.characters.Entity
import scalafx.scene.paint.Color

case class StrengthPotion() extends Item {
  override val name: String = "Strength Potion"
  override val imageName: String = "strength_potion.png"
  override val description: String = "Temporarily increases strength."
  override val symbol: Char = 'S'
  override val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.Orange)
  private var _duration: Int = 3600

  override def duration: Int = _duration

  override def duration_=(newDuration: Int): Unit = {
    _duration = newDuration
  }

  override def use(target: Entity): Unit = {
    target.stats.attack += 30
  }

  override def getColor(): SerializableColor = color
}
