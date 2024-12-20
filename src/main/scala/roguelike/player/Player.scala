package roguelike.player

import roguelike.dungeon.Dungeon
import roguelike.items.Item
import roguelike.monster.Monster
import scalafx.scene.paint.Color

case class Player(
                   var x: Int,
                   var y: Int,
                   var health: Int = 100,
                   var attackPower: Int = 10,
                   var inventory: List[Item] = List(),
                   var color: Color = Color.Blue,
                   var symbol: Char = '@'
                 ) {

  def move(dx: Int, dy: Int, dungeon: Dungeon): Unit = {
    val newX = x + dx
    val newY = y + dy

    // Check for wall collisions
    if (isValidMove(newX, newY, dungeon)) {
      x = newX
      y = newY
    }
  }

  // Check if the new position is within the dungeon and not a wall
  private def isValidMove(newX: Int, newY: Int, dungeon: Dungeon): Boolean = {
    newX >= 0 && newX < dungeon.width && newY >= 0 && newY < dungeon.height && dungeon.grid(newY)(newX) != '#'
  }

  def pickupItem(item: Item): Unit = {
    inventory = item :: inventory
    println(s"Player picked up a ${item.name}!")
  }

  def attack(monster: Monster): Unit = {
    println(s"Player attacks monster!")
    monster.takeDamage(attackPower)
  }

  def takeDamage(damage: Int): Unit = {
    health -= damage
    if (health < 0) health = 0
  }
}