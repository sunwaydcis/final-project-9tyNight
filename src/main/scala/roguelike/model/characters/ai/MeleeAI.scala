package roguelike.model.characters.ai

import roguelike.model.Game
import roguelike.model.characters.Enemy
import roguelike.model.characters.ai.AI

class MeleeAI extends AI {
  override def performAction(self: Enemy, game: Game): Unit = {
    val player = game.player
    val level = game.currentLevel

    self.isFacingLeft = player.x < self.x

    // Calculate the distance to the player
    val dx = player.x - self.x
    val dy = player.y - self.y

    // If next to the player, attack
    if (math.abs(dx) <= 1 && math.abs(dy) <= 1) {
      self.isAttacking = true
      self.attack(player, game)
      if (player.isDead()) {
        println(s"Player is dead")
      }
    }
    else {
      self.isAttacking = false
      val directionX = if (dx > 0) 1 else if (dx < 0) -1 else 0
      val directionY = if (dy > 0) 1 else if (dy < 0) -1 else 0
      self.move(directionX, directionY, level)
    }
  }
}