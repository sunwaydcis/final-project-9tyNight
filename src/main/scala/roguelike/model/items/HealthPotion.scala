package roguelike.model.items

import roguelike.model.characters.Entity
import scalafx.scene.paint.Color

case class HealthPotion() extends Item {
  override val name: String = "Health Potion"
  override val imageName: String = "health_potion.png"
  override val description: String = "Restores 50 health points."
  override val symbol: Char = '+'
  override val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.Blue)

  // Health potions are used immediately and don't have a duration
  private var _duration: Int = 0
  override def duration: Int = _duration
  override def duration_=(newDuration: Int): Unit = {
    _duration = newDuration
  }

  override def use(target: Entity): Unit = {
    val amountHealed = 50
    target.stats.health += amountHealed
    if (target.stats.health > target.stats.maxHealth) {
      target.stats.health = target.stats.maxHealth
    }
    println(s"${target.name} used a Health Potion and restored $amountHealed health!")
  }

  override def getColor(): SerializableColor = color
}