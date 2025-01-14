package roguelike.model.level

import scala.util.Random
import roguelike.model.Game
import roguelike.model.Difficulty
import roguelike.model.items.HealthPotion
import roguelike.model.items.StrengthPotion
import roguelike.model.items.ScrollOfTeleportation
import roguelike.model.items.Chest

class LevelGenerator(
                      val width: Int,
                      val height: Int,
                      val difficulty: Difficulty,
                      val random: Random
                    ) {

  def generate(levelNumber: Int): Level = {
    val grid = initializeGrid()

    val iterations = difficulty match {
      case Difficulty.Easy   => 3
      case Difficulty.Medium => 5
      case Difficulty.Hard   => 7
    }

    for (_ <- 0 until iterations) {
      doSimulationStep(grid)
    }

    val tiles = levelNumber match {
      case 1 => createTilesFromGrid(grid)
      case 2 => createTilesFromGrid2(grid)
      case 3 => createTilesFromGrid3(grid)
      case _ => createTilesFromGrid(grid) 
    }

    val level = new Level(width, height, tiles, List(), levelNumber = levelNumber)

    placeItems(level)
    placeStairs(level)
    level
  }

  private def initializeGrid(): Array[Array[Boolean]] = {
    val grid = Array.ofDim[Boolean](width, height)
    for (x <- 0 until width; y <- 0 until height) {
      val density = difficulty match {
        case Difficulty.Easy   => 0.50
        case Difficulty.Medium => 0.48
        case Difficulty.Hard   => 0.45
      }
      grid(x)(y) = random.nextFloat() < density
    }
    grid
  }

  private def doSimulationStep(grid: Array[Array[Boolean]]): Unit = {
    val newGrid = Array.ofDim[Boolean](width, height)

    for (x <- 0 until width; y <- 0 until height) {
      val aliveNeighbors = countAliveNeighbors(grid, x, y)

      val (birthLimit, deathLimit) = difficulty match {
        case Difficulty.Easy   => (4, 3)
        case Difficulty.Medium => (4, 4)
        case Difficulty.Hard   => (5, 4)
      }

      if (grid(x)(y)) {
        newGrid(x)(y) = aliveNeighbors >= deathLimit
      } else {
        newGrid(x)(y) = aliveNeighbors > birthLimit
      }
    }

    for (x <- 0 until width; y <- 0 until height) {
      grid(x)(y) = newGrid(x)(y)
    }
  }

  private def countAliveNeighbors(
                                   grid: Array[Array[Boolean]],
                                   x: Int,
                                   y: Int
                                 ): Int = {
    var count = 0
    for (i <- -1 to 1; j <- -1 to 1) {
      val neighborX = x + i
      val neighborY = y + j
      if (i != 0 || j != 0) {
        if (
          neighborX >= 0 && neighborX < width && neighborY >= 0 && neighborY < height
        ) {
          if (grid(neighborX)(neighborY)) {
            count += 1
          }
        }
      }
    }
    count
  }

  private def createTilesFromGrid(
                                   grid: Array[Array[Boolean]]
                                 ): Array[Array[Tile]] = {
    Array.tabulate(width, height) { (x, y) =>
      if (grid(x)(y)) {
        new Tile(x, y, TerrainType.Floor, false)
      } else {
        new Tile(x, y, TerrainType.Wall, true)
      }
    }
  }

  private def createTilesFromGrid2(
                                    grid: Array[Array[Boolean]]
                                  ): Array[Array[Tile]] = {
    Array.tabulate(width, height) { (x, y) =>
      if (grid(x)(y)) {
        new Tile(x, y, TerrainType.Floor2, false) // Use Floor2 for the second floor
      } else {
        new Tile(x, y, TerrainType.Wall2, true) // Use Wall2 for the second floor
      }
    }
  }

  private def createTilesFromGrid3(
                                    grid: Array[Array[Boolean]]
                                  ): Array[Array[Tile]] = {
    Array.tabulate(width, height) { (x, y) =>
      if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
        // Skull wall for the outer perimeter
        new Tile(x, y, TerrainType.Wall3, true)
      } else if (random.nextFloat() < 0.1) {
        // 10% chance
        random.nextInt(4) match { 
          case 0 => new Tile(x, y, TerrainType.CorpseBones1, false)
          case 1 => new Tile(x, y, TerrainType.CorpseBones2, false)
          case 2 => new Tile(x, y, TerrainType.BloodSpatter1, false)
          case 3 => new Tile(x, y, TerrainType.BloodSpatter2, false)
        }
      } else {
        new Tile(x, y, TerrainType.Floor3, false)
      }
    }
  }

  private def placeItems(level: Level): Unit = {
    val random = new Random()

    for (i <- 0 until 10) { 
      var placed = false
      while (!placed) {
        val x = random.nextInt(level.width)
        val y = random.nextInt(level.height)
        val tileType = level.levelNumber match {
          case 1 => TerrainType.Floor
          case 2 => TerrainType.Floor2
          case 3 => TerrainType.Floor3
          case _ => TerrainType.Floor 
        }
        if (level.tiles(x)(y).terrainType == tileType) {
          val item = random.nextInt(3) match { 
            case 0 => new HealthPotion()
            case 1 => new StrengthPotion()
            case 2 => new ScrollOfTeleportation()
          }
          item.x = x
          item.y = y
          level.addItem(item, x, y)
          placed = true
        }
      }
    }
    for (_ <- 0 until 5) { 
      var placed = false
      while (!placed) {
        val x = random.nextInt(level.width)
        val y = random.nextInt(level.height)
        val tileType = level.levelNumber match {
          case 1 => TerrainType.Floor
          case 2 => TerrainType.Floor2
          case 3 => TerrainType.Floor3
          case _ => TerrainType.Floor 
        }
        if (level.tiles(x)(y).terrainType == tileType) {
          val chest = new Chest()
          chest.x = x
          chest.y = y
          level.entities = chest :: level.entities 
          placed = true
        }
      }
    }
  }

  private def placeStairs(level: Level): Unit = {
    val random = new Random()

    // Place stairs up on level 1 and level 2
    if (level.levelNumber == 1 || level.levelNumber == 2) {
      var placed = false
      while (!placed) {
        val x = random.nextInt(level.width)
        val y = random.nextInt(level.height)
        val floorType =
          if (level.levelNumber == 1) TerrainType.Floor else TerrainType.Floor2
        if (level.tiles(x)(y).terrainType == floorType) {
          level.tiles(x)(y) = Tile(x, y, TerrainType.StairsUp, false) // Stairs up
          placed = true
        }
      }
    }

    // Place stairs down on level 2 and level 3
    if (level.levelNumber == 2 || level.levelNumber == 3) {
      var placed = false
      while (!placed) {
        val x = random.nextInt(level.width)
        val y = random.nextInt(level.height)
        val floorType =
          if (level.levelNumber == 2) TerrainType.Floor2 else TerrainType.Floor3
        if (level.tiles(x)(y).terrainType == floorType) {
          level.tiles(x)(y) = Tile(x, y, TerrainType.StairsDown, false) // Stairs down
          placed = true
        }
      }
    }
  }
}