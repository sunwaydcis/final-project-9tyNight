package roguelike.model.items

import roguelike.model.characters.Entity
import scalafx.scene.paint.Color
import roguelike.model.Game
import scala.util.Random
import roguelike.model.level.TerrainType

case class ScrollOfTeleportation() extends Item {
  override val name: String = "Scroll of Teleportation"
  override val imageName: String = "scroll_of_teleportation.png"
  override val description: String = "Instantly teleports the user to a random location on the map."
  override val symbol: Char = '?' // You can choose a different symbol if you like
  override val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.Cyan)

  private var _duration: Int = 0
  override def duration: Int = _duration
  override def duration_=(newDuration: Int): Unit = {
    _duration = newDuration
  }

  override def use(target: Entity): Unit = {
    val game = target.game
    if (game == null) {
      println("Error: Game object is not set in the player!")
      return
    }

    val level = game.currentLevel
    val random = new Random()

    var newX = 0
    var newY = 0
    var validSpot = false
    while (!validSpot) {
      newX = random.nextInt(level.width)
      newY = random.nextInt(level.height)
      if (level.isWithinBounds(newX, newY) && level.tiles(newX)(newY).terrainType == TerrainType.Floor) {
        validSpot = true
      }
    }

    target.x = newX
    target.y = newY

    println(s"${target.name} used a Scroll of Teleportation and teleported to ($newX, $newY)!")
  }

  override def getColor(): SerializableColor = color
}