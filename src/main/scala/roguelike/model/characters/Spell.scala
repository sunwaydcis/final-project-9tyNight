package roguelike.model.characters

import scalafx.scene.paint.Color
import roguelike.model.Game
import scalafx.scene.image.Image
import roguelike.model.characters.Entity
import roguelike.model.items.Item
import roguelike.model.Stats

class Spell(
             _targetX: Int,
             _targetY: Int,
             _damage: Double,
             _animation: Animation,
             game: Game
           ) extends Entity(_targetX, _targetY, 'S', Color.Yellow, "Spell", new Stats(0, 0, 0, 0)) {

  val targetX: Int = _targetX
  val targetY: Int = _targetY
  val damage: Double = _damage
  val animation: Animation = _animation
  var currentAnimationFrameIndex: Int = 0
  var isVisible: Boolean = true
  var framesSinceStart: Int = 0
  val totalAnimationFrames: Int = 3 * 3
  val travelFrames: Int = 1 * 3
  val impactFrame: Int = travelFrames + 1
  val animationSpeed: Double = 1
  var spellX: Int = x 
  var spellY: Int = y

  override def setGame(game: Game): Unit = {
    this.game = game
  }

  def update(): Unit = {
    framesSinceStart = (framesSinceStart + 1) % totalAnimationFrames

    if (framesSinceStart < travelFrames) {
      // Move towards target for the first third of the animation
      currentAnimationFrameIndex =
        math.min(currentAnimationFrameIndex + 1, 4)
    } else if (framesSinceStart == travelFrames) {
      currentAnimationFrameIndex = 5
    } else {
      if (currentAnimationFrameIndex < animation.frames.length - 1) {
        currentAnimationFrameIndex += 1
      } else {
        isVisible = false 
      }
    }
  }

  def checkCollision(player: Player): Unit = {
    if (
      framesSinceStart >= travelFrames && framesSinceStart <= impactFrame + 1
    ) {
      val spellRadius = 0
      
      val dx = spellX - player.x
      val dy = spellY - player.y
      val distance = math.sqrt(dx * dx + dy * dy)
      
      if (distance <= spellRadius) {
        player.takeDamage(damage.toInt, game)
        println("Player hit by spell!")
      }
    }
  }

  def isAnimationComplete: Boolean = {
    framesSinceStart >= totalAnimationFrames - 1
  }

  def getCurrentAnimationFrame(): Image = {
    animation.frames(currentAnimationFrameIndex)
  }

  override def attack(target: Entity, game: Game): Unit = {}
  override def takeDamage(damage: Int, game: Game): Unit = {}
  override def isDead(): Boolean = {
    false
  }
}