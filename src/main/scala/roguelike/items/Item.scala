package roguelike.items

import roguelike.player.Player
import scalafx.scene.paint.Color
import roguelike.MyApp

abstract class Item {
  def name: String
  def description: String
  def use(player: Player): Unit
  val symbol: Char
  val color: scalafx.scene.paint.Color
  var x: Int
  var y: Int
  val rarity: Rarity
}

// Different types of rarity for an item
sealed trait Rarity
case object Common extends Rarity
case object Uncommon extends Rarity
case object Rare extends Rarity
case object Legendary extends Rarity

// Example concrete item: HealthPotion
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
  override val rarity: Rarity = Common
}


case class StrengthPotion(var x: Int, var y: Int) extends Item {
  override def name: String = "Strength Potion"
  override def description: String = "Increases attack power by 5 for a limited time."
  override def use(player: Player): Unit = {
    player.attackPower += 5
    println(s"Player used a ${name} and gained 5 attack power!")
  }
  override val symbol: Char = 'S'
  override val color: scalafx.scene.paint.Color = scalafx.scene.paint.Color.Orange
  override val rarity: Rarity = Uncommon
}

case class ScrollOfTeleportation(var x: Int, var y: Int) extends Item {
  override def name: String = "Scroll of Teleportation"
  override def description: String = "Instantly teleports you to a random location in the dungeon."
  override def use(player: Player): Unit = {
    val randomRoom = MyApp.dungeon.rooms(scala.util.Random.nextInt(MyApp.dungeon.rooms.length))
    player.x = randomRoom.center._1
    player.y = randomRoom.center._2
    println(s"Player used a ${name} and teleported!")
  }
  override val symbol: Char = 'T'
  override val color: scalafx.scene.paint.Color = scalafx.scene.paint.Color.Cyan
  override val rarity: Rarity = Rare
}