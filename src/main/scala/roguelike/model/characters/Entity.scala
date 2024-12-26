package roguelike.model.characters

import roguelike.model.Stats
import roguelike.model.level.Level
import scalafx.scene.paint.Color
import roguelike.model.Game
import roguelike.model.FloatingText

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

  def move(dx: Int, dy: Int, level: Level): Unit = {
    val newX = x + dx
    val newY = y + dy
    
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
    println(s"${this.name} attacks ${target.name} for ${damage} damage!")
    target.takeDamage(damage, game)
  }

  def takeDamage(damage: Int, game: Game): Unit = {
    this.stats.health -= damage
    println(s"${this.name} takes ${damage} damage. Health: ${this.stats.health}")
    game.addFloatingText(FloatingText(s"-$damage", this.x, this.y, Color.Red, 60)) // Display damage text for 60 frames
    if (isDead()) {
      println(s"${this.name} has died!")
    }
  }

  def isDead(): Boolean = {
    stats.health <= 0
  }
}