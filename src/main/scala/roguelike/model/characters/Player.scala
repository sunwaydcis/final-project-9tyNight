package roguelike.model.characters

import roguelike.model.Stats
import roguelike.model.level.Level
import scalafx.scene.paint.Color
import roguelike.model.Game
import roguelike.model.items.Inventory
import roguelike.model.FloatingText

class Player(
              _x: Int,
              _y: Int,
              char: Char,
              color: Color,
              name: String,
              stats: Stats
            ) extends Entity(_x, _y, char, color, name, stats) {
  val inventory: Inventory = new Inventory()

  override def attack(target: Entity, game: Game): Unit = {
    val damage = math.max(0, this.stats.attack - target.stats.defense)
    println(s"${this.name} attacks ${target.name} for ${damage} damage!")
    target.takeDamage(damage, game)
  }

  override def takeDamage(damage: Int, game: Game): Unit = {
    this.stats.health -= damage
    println(s"${this.name} takes ${damage} damage. Health: ${this.stats.health}")
    game.addFloatingText(FloatingText(s"-$damage", this.x, this.y, Color.Red, 60))
    if (isDead()) {
      println(s"${this.name} has died!")
    }
  }

  override def isDead(): Boolean = {
    val isDead = this.stats.health <= 0
    if (isDead) {
      println("Player has died!")
    }
    isDead
  }

  override def move(dx: Int, dy: Int, level: Level): Unit = {
    var newX = x + dx
    var newY = y + dy

    println(s"Attempting to move to: ($newX, $newY)")
    println(s"Is within bounds: ${level.isWithinBounds(newX, newY)}")
    println(s"Is tile blocking: ${level.isTileBlocking(newX, newY)}")

    if (level.isWithinBounds(newX, newY) && !level.isTileBlocking(newX, newY)) {
      x = newX
      y = newY
    }
  }
}