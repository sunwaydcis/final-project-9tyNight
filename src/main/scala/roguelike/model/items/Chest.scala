package roguelike.model.items

import roguelike.model.characters.Entity
import roguelike.model.Game
import scala.util.Random
import scalafx.scene.paint.Color
import roguelike.model.characters.Player
import roguelike.model.level.TerrainType

class Chest(var isOpen: Boolean = false) extends Entity(0, 0, 'C', Color.Brown, "Chest", null) {

  def open(game: Game): Unit = {
    if (!isOpen) {
      isOpen = true
      // Drop items from the chest
      dropItems(game)
      println("Chest opened!")
      game.updateUI()
    } else {
      println("Chest is already open!")
    }
  }

  private def dropItems(game: Game): Unit = {
    val random = new Random()
    val numItems = 2 // Number of items to drop

    for (_ <- 0 until numItems) {
      var placed = false
      var attempts = 0 

      while (!placed && attempts < 10) {
        val item = generateRandomItem()
        println(s"Trying to drop: ${item.name}")

        // Generate random offset from the chest's position
        val offsetX = random.nextInt(3) - 1 
        val offsetY = random.nextInt(3) - 1 

        // Calculate new position based on chest's position
        val newX = x + offsetX
        val newY = y + offsetY

        println(s"  Attempting to place at: (${newX}, ${newY})")

        // Check if the new position is within bounds
        if (game.currentLevel.isWithinBounds(newX, newY)) {
          // Use the correct floor type based on the current level
          val floorType = game.currentLevel.levelNumber match {
            case 1 => TerrainType.Floor
            case 2 => TerrainType.Floor2
            case 3 => TerrainType.Floor3
            case _ => TerrainType.Floor 
          }

          val tileType = game.currentLevel.tiles(newX)(newY).terrainType
          println(s"  Tile type at (${newX}, ${newY}): ${tileType}")
          
          if (
            tileType == floorType &&
              !game.currentLevel.items.exists(i => i.x == newX && i.y == newY) &&
              !game.currentLevel.entities.exists(e =>
                e.x == newX && e.y == newY && !e.isInstanceOf[Chest]
              )
          ) {
            item.x = newX
            item.y = newY
            game.currentLevel.items += item
            println(
              s"  Dropped a ${item.name} from the chest at (${item.x}, ${item.y})."
            )
            placed = true
          } else {
            println(
              s"  Cannot place at (${newX}, ${newY}) - invalid tile or occupied."
            )
            attempts += 1
          }
        } else {
          println(s"  Cannot place at (${newX}, ${newY}) - out of bounds.")
          attempts += 1
        }
      }

      if (!placed) {
        println(s"Failed to place item after multiple attempts.")
      }
    }
  }

  import scala.util.boundary
  import scala.util.boundary.break

  private def generateRandomItem(): Item = boundary {
    val random = new Random()
    val itemWeights = Map(
      "BasicSword" -> 10,
      "SteelSword" -> 5,
      "IronSword" -> 5,
      "BasicChestplate" -> 10,
      "SteelChestplate" -> 5,
      "IronChestplate" -> 5,
      "BasicHelmet" -> 10,
      "SteelHelmet" -> 5,
      "IronHelmet" -> 5,
      "BasicShield" -> 10,
      "SteelShield" -> 5,
      "IronShield" -> 5,
      "StrengthPendant" -> 2,
      "HealthRing" -> 2
    )

    val totalWeight = itemWeights.values.sum
    val randomWeight = random.nextInt(totalWeight)

    var cumulativeWeight = 0
    for ((itemName, weight) <- itemWeights) {
      cumulativeWeight += weight
      if (randomWeight < cumulativeWeight) {
        break(itemName match { // Use break to exit the boundary
          case "BasicSword" => new BasicSword()
          case "SteelSword" => new SteelSword()
          case "IronSword" => new IronSword()
          case "BasicChestplate" => new BasicChestplate()
          case "SteelChestplate" => new SteelChestplate()
          case "IronChestplate" => new IronChestplate()
          case "BasicHelmet" => new BasicHelmet()
          case "SteelHelmet" => new SteelHelmet()
          case "IronHelmet" => new IronHelmet()
          case "BasicShield" => new BasicShield()
          case "SteelShield" => new SteelShield()
          case "IronShield" => new IronShield()
          case "StrengthPendant" => new StrengthPendant()
          case "HealthRing" => new HealthRing()
          case _ => new HealthPotion()
        })
      }
    }

    new HealthPotion() // Default in case no item is selected (shouldn't happen)
  }

  override def isDead(): Boolean = false

  override def takeDamage(damage: Int, game: Game): Unit = {
    // Chests don't take damage in this implementation
  }

  // Placeholder for attack method in Chest
  override def attack(target: Entity, game: Game): Unit = {
    // Chests do not attack, so this method is empty
  }
}