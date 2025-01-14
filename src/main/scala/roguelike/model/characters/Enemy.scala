package roguelike.model.characters

import roguelike.model.level.Level
import roguelike.model.Stats
import scalafx.scene.paint.Color
import roguelike.model.Game
import scalafx.scene.image.Image
import roguelike.model.FloatingText
import roguelike.model.items.{Item, SerializableColor, ColorHelper}
import roguelike.model.characters.ai.AI

abstract class Enemy(
                      _x: Int,
                      _y: Int,
                      char: Char,
                      _color: Color,
                      name: String,
                      stats: Stats,
                      val ai: AI
                    ) extends Entity(_x, _y, char, _color, name, stats) {

  var currentAnimation: Animation = _
  var idleAnimation: Animation = _
  var runAnimation: Animation = _
  var attackAnimation: Animation = _
  var deathAnimation: Animation = _
  var idleAnimationLeft: Animation = _
  var runAnimationLeft: Animation = _
  var attackAnimationLeft: Animation = _
  var deathAnimationLeft: Animation = _

  var isRunning: Boolean = false
  var isAttacking: Boolean = false
  var isFacingLeft: Boolean = false
  var deathAnimationFinished: Boolean = false

  // Load animations for the basic enemy 
  protected def loadAnimations(): Unit = {
    idleAnimation = Animation(
      (1 to 15).map(i =>
        new Image(
          getClass.getResource(s"/ui/enemy_1/Idle/Idle_$i.png").toExternalForm
        )
      ).toList
    )

    idleAnimationLeft = Animation(
      (1 to 15).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_1/Idle/Idle_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    runAnimation = Animation(
      (1 to 12).map(i =>
        new Image(
          getClass.getResource(s"/ui/enemy_1/Walk/Walk_$i.png").toExternalForm
        )
      ).toList
    )

    runAnimationLeft = Animation(
      (1 to 12).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_1/Walk/Walk_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    attackAnimation = Animation(
      (1 to 7).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_1/Attack/Attack_$i.png")
            .toExternalForm
        )
      ).toList
    )

    attackAnimationLeft = Animation(
      (1 to 7).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_1/Attack/Attack_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    deathAnimation = Animation(
      (1 to 11).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_1/Death/Death_$i.png")
            .toExternalForm
        )
      ).toList
    )

    deathAnimationLeft = Animation(
      (1 to 11).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/enemy_1/Death/Death_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    currentAnimation = idleAnimation
  }

  // Call loadAnimations in the constructor
  loadAnimations()

  override def setGame(game: Game): Unit = {
    this.game = game
  }

  override def attack(target: Entity, game: Game): Unit = {
    val damage = this.stats.attack
    target.takeDamage(damage, game)
  }

  override def takeDamage(damage: Int, game: Game): Unit = {
    this.stats.health -= damage
    game.addFloatingText(
      FloatingText(s"-$damage", this.x, this.y, Color.Red, 60)
    )
    if (isDead()) {
      isAttacking = false
      isRunning = false
      currentAnimation =
        if (isFacingLeft) deathAnimationLeft else deathAnimation
      currentAnimation.reset()
    }
  }

  override def isDead(): Boolean = {
    this.stats.health <= 0
  }

  def performAction(game: Game): Unit = {
    ai.performAction(this, game)
  }

  // Update animation
  def updateAnimation(game: Game): Unit = {
    if (isDead()) {
      currentAnimation =
        if (isFacingLeft) deathAnimationLeft else deathAnimation
      if (currentAnimation.currentFrame == currentAnimation.frames.length - 1) {
        deathAnimationFinished = true
        game.removeEnemy(this)
      }
    } else if (isAttacking) {
      currentAnimation =
        if (isFacingLeft) attackAnimationLeft else attackAnimation
      if (currentAnimation.currentFrame == currentAnimation.frames.length - 1) {
        isAttacking = false
      }
    } else if (isRunning) {
      currentAnimation =
        if (isFacingLeft) runAnimationLeft else runAnimation
    } else {
      currentAnimation =
        if (isFacingLeft) idleAnimationLeft else idleAnimation
    }
    currentAnimation.update()
  }

  def getCurrentAnimationFrame(): Image = {
    currentAnimation.getCurrentFrame()
  }
}