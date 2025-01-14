package roguelike.model.characters

import roguelike.model.level.Level
import roguelike.model.Stats
import scalafx.scene.paint.Color
import roguelike.model.Game
import scalafx.scene.image.Image
import roguelike.model.FloatingText
import roguelike.model.items.{Item, SerializableColor, ColorHelper}
import roguelike.model.characters.ai.AI

class Mage(
            _x: Int,
            _y: Int,
            char: Char,
            _color: Color,
            name: String,
            stats: Stats,
            ai: AI
          ) extends Enemy(_x, _y, char, _color, name, stats, ai) {

  var isCasting: Boolean = false
  var castAnimation: Animation = _
  var castAnimationLeft: Animation = _
  var spellAnimation: Animation = _

  // Target position for the spell
  var targetX: Int = _
  var targetY: Int = _

  // Override loadAnimations to load Mage-specific animations
  override protected def loadAnimations(): Unit = {
    idleAnimation = Animation(
      (1 to 8).map(i =>
        new Image(
          getClass.getResource(s"/ui/enemy_2/Idle/Idle_$i.png").toExternalForm
        )
      ).toList
    )

    idleAnimationLeft = Animation(
      (1 to 8).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_2/Idle/Idle_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    runAnimation = Animation(
      (1 to 8).map(i =>
        new Image(
          getClass.getResource(s"/ui/enemy_2/Walk/Walk_$i.png").toExternalForm
        )
      ).toList
    )

    runAnimationLeft = Animation(
      (1 to 8).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_2/Walk/Walk_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    attackAnimation = Animation(
      (1 to 10).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_2/Attack/Attack_$i.png")
            .toExternalForm
        )
      ).toList
    )

    attackAnimationLeft = Animation(
      (1 to 10).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_2/Attack/Attack_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    deathAnimation = Animation(
      (1 to 10).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_2/Death/Death_$i.png")
            .toExternalForm
        )
      ).toList
    )

    deathAnimationLeft = Animation(
      (1 to 10).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_2/Death/Death_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    castAnimation = Animation(
      (1 to 9).map(i =>
        new Image(
          getClass.getResource(s"/ui/enemy_2/Cast/Cast_$i.png").toExternalForm
        )
      ).toList
    )

    castAnimationLeft = Animation(
      (1 to 9).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_2/Cast/Cast_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    spellAnimation = Animation(
      (1 to 16).map(i =>
        new Image(
          getClass.getResource(s"/ui/enemy_2/Spell/Spell_$i.png").toExternalForm
        )
      ).toList
    )

    currentAnimation = idleAnimation
  }
  
  loadAnimations()

  override def attack(target: Entity, game: Game): Unit = {
    val damage = this.stats.attack * 4
    target.takeDamage(damage, game)
    isAttacking = true
    currentAnimation =
      if (isFacingLeft) attackAnimationLeft else attackAnimation
  }

  def castSpell(game: Game): Unit = {
    isCasting = true
    currentAnimation = if (isFacingLeft) castAnimationLeft else castAnimation
    
    val spell = new Spell(
      targetX,
      targetY,
      this.stats.attack,
      spellAnimation,
      game
    )
    game.currentLevel.entities = spell :: game.currentLevel.entities
  }

  override def takeDamage(damage: Int, game: Game): Unit = {
    this.stats.health -= damage
    game.addFloatingText(
      FloatingText(s"-$damage", this.x, this.y, Color.Red, 60)
    )
    if (isDead()) {
      isAttacking = false
      isRunning = false
      isCasting = false
      currentAnimation =
        if (isFacingLeft) deathAnimationLeft else deathAnimation
      currentAnimation.reset()
    }
  }

  // Method to predict where the player might move next
  def predictPlayerMovement(player: Player): (Int, Int) = {
    (player.x, player.y)
  }

  // Override updateAnimation to handle Mage-specific animations
  override def updateAnimation(game: Game): Unit = {
    if (isDead()) {
      currentAnimation = if (isFacingLeft) deathAnimationLeft else deathAnimation
      if (currentAnimation.currentFrame == currentAnimation.frames.length - 1) {
        deathAnimationFinished = true
        game.removeEnemy(this)
      }
    } else if (isAttacking) {
      currentAnimation = if (isFacingLeft) attackAnimationLeft else attackAnimation
      if (currentAnimation.currentFrame == currentAnimation.frames.length - 1) {
        isAttacking = false
      }
    } else if (isCasting) {
      currentAnimation = if (isFacingLeft) castAnimationLeft else castAnimation
      if (currentAnimation.currentFrame == currentAnimation.frames.length - 1) {
        isCasting = false
      }
    } else if (isRunning) {
      currentAnimation = if (isFacingLeft) runAnimationLeft else runAnimation
    } else {
      currentAnimation = if (isFacingLeft) idleAnimationLeft else idleAnimation
    }
    currentAnimation.update()
  }
}