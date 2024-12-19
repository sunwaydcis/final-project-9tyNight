package roguelike.items

import roguelike.player.Player

abstract class Item {
  def name: String
  def description: String
  def use(player: Player): Unit
  val symbol: Char
  val color: scalafx.scene.paint.Color
  var x: Int
  var y: Int
}

// HealthPotion
case class HealthPotion(var x: Int, var y: Int) extends Item {
  override def name: String = "Health Potion"
  override def description: String = "Restores 50 health points."
  override def use(player: Player): Unit = {
    player.health += 50
    if (player.health > 100) player.health = 100 
    println(s"Player used a ${name} and restored 50 health!")
  }
  override val symbol: Char = '!'
  override val color: scalafx.scene.paint.Color = scalafx.scene.paint.Color.Pink
}