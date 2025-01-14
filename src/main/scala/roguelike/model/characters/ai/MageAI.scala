package roguelike.model.characters.ai

import roguelike.model.Game
import roguelike.model.characters.{Enemy, Mage, Player}
import scala.concurrent.duration.*

class MageAI extends AI {
  private val spellCooldown = 2.seconds // Cooldown for spell casting
  private var lastSpellCastTime: Long = 0
  private val spellRange = 6 // The range of the spell in tiles

  override def performAction(self: Enemy, game: Game): Unit = {
    self match {
      case mage: Mage => performMageAction(mage, game)
      case _           => 
    }
  }

  private def performMageAction(mage: Mage, game: Game): Unit = {
    val player = game.player
    val level = game.currentLevel
    val currentTime = System.currentTimeMillis()

    mage.isFacingLeft = player.x < mage.x

    // Calculate the distance to the player
    val dx = player.x - mage.x
    val dy = player.y - mage.y
    val distance = math.sqrt(dx * dx + dy * dy)

    // If next to the player, attack
    if (math.abs(dx) <= 1 && math.abs(dy) <= 1) {
      mage.isAttacking = true
      mage.attack(player, game)
      if (player.isDead()) {
        println(s"Player is dead")
      }
    } else if (
      distance <= spellRange && currentTime - lastSpellCastTime >= spellCooldown.toMillis
    ) {
      // Set the target position for the spell
      val (targetX, targetY) = predictTargetLocation(mage, player)
      mage.targetX = targetX
      mage.targetY = targetY

      mage.castSpell(game)
      lastSpellCastTime = currentTime
    } else {
      mage.isAttacking = false
      mage.isCasting = false
      val directionX = if (dx > 0) 1 else if (dx < 0) -1 else 0
      val directionY = if (dy > 0) 1 else if (dy < 0) -1 else 0
      mage.move(directionX, directionY, level)
    }
  }

  private def predictTargetLocation(mage: Mage, player: Player): (Int, Int) = {
    // Predict the player's movement
    val (predictedX, predictedY) = mage.predictPlayerMovement(player)

    // Ensure the predicted location is within the spell range
    val dx = predictedX - mage.x
    val dy = predictedY - mage.y
    val distance = math.sqrt(dx * dx + dy * dy)

    if (distance <= spellRange) {
      (predictedX, predictedY)
    } else {
      // If the predicted location is out of range, adjust it to be within range
      val scaleFactor = spellRange / distance
      val adjustedX = mage.x + (dx * scaleFactor).toInt
      val adjustedY = mage.y + (dy * scaleFactor).toInt
      (adjustedX, adjustedY)
    }
  }
}