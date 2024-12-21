package roguelike.items

import roguelike.player.Player
import scalafx.scene.paint.Color
import roguelike.dungeon.Dungeon

abstract class Item {
  def name: String
  def description: String
  def use(player: Player, dungeon: Dungeon): Unit
  val symbol: Char
  val color: scalafx.scene.paint.Color
  var x: Int
  var y: Int
  val rarity: Rarity
  var quantity: Int = 1
}

// Different types of rarity for an item
sealed trait Rarity
case object Common extends Rarity
case object Uncommon extends Rarity
case object Rare extends Rarity
case object Legendary extends Rarity

// HealthPotion
case class HealthPotion(var x: Int, var y: Int) extends Item {
  override def name: String = "Health Potion"
  override def description: String = "Restores 50 health points."
  override def use(player: Player, dungeon: Dungeon): Unit = {
    if (player.health < player.maxHealth) {
      player.health += 50
      if (player.health > player.maxHealth) player.health = player.maxHealth
      println(s"Player used a ${name} and restored 50 health!")

      quantity -= 1
      if (quantity <= 0) {
        player.inventory = player.inventory.filterNot(_ == this)
      }
    } else {
      println("Health is already full!")
    }
  }
  override val symbol: Char = '!'
  override val color: scalafx.scene.paint.Color = Color.Pink
  override val rarity: Rarity = Common
}

//Strength Potion
case class StrengthPotion(var x: Int, var y: Int) extends Item {
  override def name: String = "Strength Potion"
  override def description: String = "Increases attack power by 5 for a limited time."
  override def use(player: Player, dungeon: Dungeon): Unit = {
    player.attackPower += 5
    println(s"Player used a ${name} and gained 5 attack power!")

    quantity -= 1
    if (quantity <= 0) {
      player.inventory = player.inventory.filterNot(_ == this)
    }
  }
  override val symbol: Char = 'S'
  override val color: scalafx.scene.paint.Color = Color.Orange
  override val rarity: Rarity = Uncommon
}

// Scroll of Teleportation
case class ScrollOfTeleportation(var x: Int, var y: Int) extends Item {
  override def name: String = "Scroll of Teleportation"
  override def description: String = "Instantly teleports you to a random location in the dungeon."
  override def use(player: Player, dungeon: Dungeon): Unit = {
    val randomRoom = dungeon.rooms(scala.util.Random.nextInt(dungeon.rooms.length))
    player.x = randomRoom.center._1
    player.y = randomRoom.center._2
    println(s"Player used a ${name} and teleported!")

    quantity -= 1
    if (quantity <= 0) {
      player.inventory = player.inventory.filterNot(_ == this)
    }
  }
  override val symbol: Char = 'T'
  override val color: scalafx.scene.paint.Color = Color.Cyan
  override val rarity: Rarity = Rare
}