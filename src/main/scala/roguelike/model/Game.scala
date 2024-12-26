package roguelike.model

import roguelike.model.level._
import roguelike.model.characters.Player
import roguelike.model.characters.Enemy
import roguelike.model.characters.MeleeAI
import roguelike.util.GameLog
import scalafx.scene.paint.Color
import scalafx.application.Platform
import roguelike.ui.GameScene
import roguelike.model.characters.Entity
import roguelike.model.Difficulty
import scala.util.Random

class Game() {

  var player: Player = _
  var currentLevel: Level = _
  var currentLevelNumber: Int = 1
  var currentDifficulty: Difficulty = _
  val gameLog = new GameLog()
  var isPlayerTurn: Boolean = true
  var gameOver: Boolean = false
  var floatingTexts: List[FloatingText] = List()
  var gameScene: GameScene = null

  val random = new Random()

  def loadLevelForDifficulty(difficulty: Difficulty): Unit = {
    currentDifficulty = difficulty
    generateNewLevel(difficulty)
  }

  def nextLevel(): Unit = {
    generateNewLevel(currentDifficulty)
    placePlayer()
  }

  private def placePlayerAtStart(): Unit = {
    val startPosition = currentLevel.tiles.zipWithIndex.flatMap { case (row, y) =>
      row.zipWithIndex.collectFirst { case (tile, x) if tile.terrainType == TerrainType.Floor => (x, y) }
    }.headOption

    startPosition match {
      case Some((x, y)) =>
        player.x = x
        player.y = y
      case None =>
        println("Error: No start position found in level data.")
    }
  }

  def generateNewLevel(difficulty: Difficulty): Unit = {
    val levelWidth = 80
    val levelHeight = 40

    val levelGenerator = new LevelGenerator(levelWidth, levelHeight, difficulty)
    currentLevel = levelGenerator.generate()

    placePlayer()
    addEnemiesAndItems(currentLevel, difficulty)
  }

  private def placePlayer(): Unit = {
    val startPosition = for {
      x <- 0 until currentLevel.width
      y <- 0 until currentLevel.height
      if currentLevel.tiles(x)(y).terrainType == TerrainType.Floor
    } yield (x, y)

    if (startPosition.nonEmpty) {
      val (x, y) = startPosition(random.nextInt(startPosition.length))

      if (player == null) {
        player = new Player(x, y, '@', Color.White, "Player", new Stats(100, 100, 10, 5))
        currentLevel.entities = player :: currentLevel.entities
      } else {
        player.x = x
        player.y = y

        if (!currentLevel.entities.contains(player)) {
          currentLevel.entities = player :: currentLevel.entities
        }
      }
    } else {
      println("Error: No suitable start position found for the player.")
    }
  }

  private def addEnemiesAndItems(
                                  level: Level,
                                  difficulty: Difficulty,
                                ): Unit = {
    val numberOfEnemies = difficulty match {
      case Difficulty.Easy   => 5
      case Difficulty.Medium => 10
      case Difficulty.Hard   => 20
    }

    for (_ <- 0 until numberOfEnemies) {
      var (x, y) = (0, 0)
      var validPosition = false
      while (!validPosition) {
        x = random.nextInt(level.width)
        y = random.nextInt(level.height)
        validPosition = level.tiles(x)(y).terrainType == TerrainType.Floor &&
          !level.entities.exists(e => e.x == x && e.y == y) &&
          (x != player.x && y != player.y)
      }

      val enemyStats = difficulty match {
        case Difficulty.Easy   => new Stats(10, 10, 3, 1)
        case Difficulty.Medium => new Stats(20, 20, 5, 2)
        case Difficulty.Hard   => new Stats(30, 30, 8, 3)
      }
      val enemy = new Enemy(
        x,
        y,
        'E',
        if (difficulty == Difficulty.Hard) Color.DarkRed else Color.Orange,
        "Enemy",
        enemyStats,
        new MeleeAI()
      )

      level.entities = enemy :: level.entities
    }
  }

  def movePlayer(dx: Int, dy: Int): Unit = {
    player.move(dx, dy, currentLevel)
    isPlayerTurn = false
  }

  def update(): Unit = {
    if (!isPlayerTurn) {
      // Enemy turn
      currentLevel.entities.foreach {
        case enemy: Enemy => enemy.ai.performAction(enemy, this)
        case _ =>
      }
      isPlayerTurn = true
      cleanupDeadEntities()
    }
    floatingTexts.foreach(_.update())
    floatingTexts = floatingTexts.filter(_.isVisible())
  }

  def addFloatingText(text: FloatingText): Unit = {
    floatingTexts = text :: floatingTexts
  }

  private def cleanupDeadEntities(): Unit = {
    currentLevel.entities = currentLevel.entities.filterNot {
      case p: Player if p.isDead() =>
        gameOver = true
        println("Game Over! Player has died.")
        false
      case e: Enemy if e.isDead() =>
        println(s"Enemy died at (${e.x}, ${e.y})")
        true
      case _ => false
    }
  }

  def resetGame(): Unit = {
    player = null
    currentLevel = null
    currentLevelNumber = 1
    gameOver = false
    isPlayerTurn = true
  }

  def pickUpItem(): Unit = {
    val item = currentLevel.items.find(item => item.x == player.x && item.y == player.y)
    item.foreach { it =>
      player.inventory.addItem(it)
      currentLevel.removeItem(it)
      println(s"Picked up a ${it.name}")
      Platform.runLater {
        gameScene.updateInventoryUI()
      }
    }
  }

  def removeEnemy(enemy: Enemy): Unit = {
    currentLevel.entities = currentLevel.entities.filterNot(_ == enemy)
  }

  def setGameScene(gameScene: GameScene): Unit = {
    this.gameScene = gameScene
  }
}