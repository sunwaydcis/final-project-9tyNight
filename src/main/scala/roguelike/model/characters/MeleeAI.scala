package roguelike.model.characters

import roguelike.model.Game

class MeleeAI extends AI {
  override def performAction(self: Enemy, game: Game): Unit = {
    val player = game.player
    val level = game.currentLevel

    val dx = player.x - self.x
    val dy = player.y - self.y

    if (math.abs(dx) <= 1 && math.abs(dy) <= 1) {
      self.attack(player, game)
      if (player.isDead()) {
        println(s"Player is dead")
      }
    }
    else {
      val directionX = if (dx > 0) 1 else if (dx < 0) -1 else 0
      val directionY = if (dy > 0) 1 else if (dy < 0) -1 else 0
      self.move(directionX, directionY, level)
    }
  }
}