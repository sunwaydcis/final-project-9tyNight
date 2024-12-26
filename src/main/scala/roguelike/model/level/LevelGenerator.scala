package roguelike.model.level

import scala.util.Random
import roguelike.model.Game
import roguelike.model.Difficulty
import roguelike.model.items.HealthPotion

class LevelGenerator(val width: Int, val height: Int, val difficulty: Difficulty) {

  val random = new Random()

  def generate(): Level = {
    val grid = initializeGrid()
    
    val iterations = difficulty match {
      case Difficulty.Easy => 3
      case Difficulty.Medium => 5
      case Difficulty.Hard => 7
    }

    for (_ <- 0 until iterations) {
      doSimulationStep(grid)
    }

    val tiles = createTilesFromGrid(grid)
    val level = new Level(width, height, tiles, List()) 

    placeItems(level) 

    level
  }

  private def initializeGrid(): Array[Array[Boolean]] = {
    val grid = Array.ofDim[Boolean](width, height)
    for (x <- 0 until width; y <- 0 until height) {
      val density = difficulty match {
        case Difficulty.Easy => 0.50 
        case Difficulty.Medium => 0.48 
        case Difficulty.Hard => 0.45 
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
        case Difficulty.Easy => (4, 3) 
        case Difficulty.Medium => (4, 4) 
        case Difficulty.Hard => (5, 4) 
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

  private def countAliveNeighbors(grid: Array[Array[Boolean]], x: Int, y: Int): Int = {
    var count = 0
    for (i <- -1 to 1; j <- -1 to 1) {
      val neighborX = x + i
      val neighborY = y + j
      if (i != 0 || j != 0) { 
        if (neighborX >= 0 && neighborX < width && neighborY >= 0 && neighborY < height) {
          if (grid(neighborX)(neighborY)) {
            count += 1
          }
        }
      }
    }
    count
  }

  private def createTilesFromGrid(grid: Array[Array[Boolean]]): Array[Array[Tile]] = {
    Array.tabulate(width, height) { (x, y) =>
      if (grid(x)(y)) {
        new Tile(x, y, TerrainType.Floor, false) 
      } else {
        new Tile(x, y, TerrainType.Wall, true) 
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
        if (level.tiles(x)(y).terrainType == TerrainType.Floor) {
          val healthPotion = new HealthPotion()
          healthPotion.x = x
          healthPotion.y = y
          level.addItem(healthPotion, x, y) 
          println(s"Placing HealthPotion at ($x, $y)") 
          placed = true
        }
      }
    }
  }
}