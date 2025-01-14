package roguelike.model.characters.ai

import roguelike.model.Game
import roguelike.model.characters.{Enemy, Boss}

class BossAI extends AI {
  override def performAction(self: Enemy, game: Game): Unit = {
    self match {
      case boss: Boss =>
        val player = game.player
        val level = game.currentLevel

        boss.isFacingLeft = player.x < boss.x
        
        val dx = player.x - boss.x
        val dy = player.y - boss.y
        
        if (math.abs(dx) <= 1 && math.abs(dy) <= 1) {
          boss.isAttacking = true
          boss.attack(player, game)
          if (player.isDead()) {
            println(s"Player is dead")
          }
        } else {
          boss.isAttacking = false
          val directionX = if (dx > 0) 1 else if (dx < 0) -1 else 0
          val directionY = if (dy > 0) 1 else if (dy < 0) -1 else 0
          boss.move(directionX, directionY, level)
        }

      case _ =>
        println("Error: Trying to perform BossAI action on non-Boss entity.")
    }
  }
}