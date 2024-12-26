package roguelike.model.items

import roguelike.model.characters.Entity
import scalafx.scene.paint.Color

class HealthPotion extends Item {
  override val name: String = "Health Potion"
  override val description: String = "Restores 20 health points."
  override val symbol: Char = '+'
  val color: Color = Color.Red
  override def use(target: Entity): Unit = {
    val amountHealed = 20
    target.stats.health += amountHealed
    if (target.stats.health > target.stats.maxHealth) {
      target.stats.health = target.stats.maxHealth
    }
    println(s"${target.name} used a Health Potion and restored ${amountHealed} health!")
  }
}