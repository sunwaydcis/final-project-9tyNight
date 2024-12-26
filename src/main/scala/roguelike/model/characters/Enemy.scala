package roguelike.model.characters

import roguelike.model.level.Level
import roguelike.model.Stats
import scalafx.scene.paint.Color
import roguelike.model.Game
import roguelike.model.FloatingText

class Enemy(
             _x: Int,
             _y: Int,
             char: Char,
             color: Color,
             name: String,
             stats: Stats,
             val ai: AI
           ) extends Entity(_x, _y, char, color, name, stats) {

  override def attack(target: Entity, game: Game): Unit = {
    val damage = this.stats.attack
    target.takeDamage(damage, game)
  }

  override def takeDamage(damage: Int, game: Game): Unit = {
    this.stats.health -= damage
    println(s"${this.name} takes ${damage} damage. Health: ${this.stats.health}")
    game.addFloatingText(FloatingText(s"-$damage", this.x, this.y, Color.Red, 60))
    if (isDead()) {
      println(s"${this.name} has died!")
      game.removeEnemy(this)
    }
  }

  override def isDead(): Boolean = {
    this.stats.health <= 0
  }

  def performAction(game: Game): Unit = {
    ai.performAction(this, game)
  }
}