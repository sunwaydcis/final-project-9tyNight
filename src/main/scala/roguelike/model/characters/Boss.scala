package roguelike.model.characters

import roguelike.model.level.Level
import roguelike.model.Stats
import scalafx.scene.paint.Color
import roguelike.model.Game
import scalafx.scene.image.Image
import roguelike.model.FloatingText
import roguelike.model.items.{Item, SerializableColor, ColorHelper}
import roguelike.model.characters.ai.AI

class Boss(
            _x: Int,
            _y: Int,
            char: Char,
            _color: Color,
            name: String,
            stats: Stats,
            ai: AI
          ) extends Enemy(_x, _y, char, _color, name, stats, ai) {

  override def loadAnimations(): Unit = {
    idleAnimation = Animation(
      (1 to 9).map(i =>
        new Image(
          getClass.getResource(s"/ui/boss/Idle/Idle_$i.png").toExternalForm
        )
      ).toList
    )

    idleAnimationLeft = Animation(
      (1 to 9).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/boss/Idle/Idle_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    runAnimation = Animation(
      (1 to 6).map(i =>
        new Image(
          getClass.getResource(s"/ui/boss/Run/Run_$i.png").toExternalForm
        )
      ).toList
    )

    runAnimationLeft = Animation(
      (1 to 6).map(i =>
        new Image(
          getClass.getResource(s"/ui/boss/Run/Run_left_$i.png").toExternalForm
        )
      ).toList
    )

    attackAnimation = Animation(
      (1 to 12).map(i =>
        new Image(
          getClass.getResource(s"/ui/boss/Attack/Attack_$i.png").toExternalForm
        )
      ).toList
    )

    attackAnimationLeft = Animation(
      (1 to 12).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/boss/Attack/Attack_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    deathAnimation = Animation(
      (1 to 23).map(i =>
        new Image(
          getClass.getResource(s"/ui/boss/Death/Death_$i.png").toExternalForm
        )
      ).toList
    )

    deathAnimationLeft = Animation(
      (1 to 23).map(i =>
        new Image(
          getClass
            .getResource(s"/ui/boss/Death/Death_left_$i.png")
            .toExternalForm
        )
      ).toList
    )

    currentAnimation = idleAnimation
  }
  loadAnimations()
  
  override def takeDamage(damage: Int, game: Game): Unit = {
    super.takeDamage(damage, game)
  }
}