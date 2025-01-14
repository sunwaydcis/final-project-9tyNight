package roguelike.model.characters

import roguelike.model.Stats
import roguelike.model.level.Level
import scalafx.scene.paint.Color
import roguelike.model.Game
import roguelike.model.items.Inventory
import scalafx.scene.image.Image
import roguelike.model.FloatingText
import roguelike.model.items.Item

abstract class Entity(
                       _x: Int,
                       _y: Int,
                       val char: Char,
                       val color: Color,
                       val name: String,
                       val stats: Stats
                     ) {
  var x: Int = _x
  var y: Int = _y
  var game: Game = _

  def setGame(game: Game): Unit = {
    this.game = game
  }

  def move(dx: Int, dy: Int, level: Level): Unit = {
    val newX = x + dx
    val newY = y + dy

    // Check if the new position is within the level bounds
    if (
      newX >= 0 && newX < level.width && newY >= 0 && newY < level.height && !level
        .isTileBlocking(newX, newY)
    ) {
      x = newX
      y = newY
    }
  }

  def attack(target: Entity, game: Game): Unit = {
    val damage = math.max(0, this.stats.attack - target.stats.defense)
    target.takeDamage(damage, game)
  }

  def takeDamage(damage: Int, game: Game): Unit = {
    this.stats.health -= damage
    game.addFloatingText(FloatingText(s"-$damage", this.x, this.y, Color.Red, 60))
    if (isDead()) {
    }
  }

  def isDead(): Boolean = {
    stats.health <= 0
  }

  def equip(item: Item): Option[Item] = None

  def unequip(slot: String): Unit = {}
}

object Entity {
  def isAdjacent(entity1: Entity, entity2: Entity): Boolean = {
    math.abs(entity1.x - entity2.x) <= 1 && math.abs(entity1.y - entity2.y) <= 1
  }
}