package roguelike.model

import roguelike.model.level._
import roguelike.model.characters.Player
import roguelike.model.characters.Enemy
import roguelike.model.characters.ai.MeleeAI
import scalafx.scene.paint.Color
import scalafx.application.Platform
import roguelike.ui.GameScene
import scalafx.application.JFXApp3
import roguelike.model.items.Item
import roguelike.model.items.{HealthPotion, StrengthPotion, ScrollOfTeleportation}
import roguelike.model.characters.Entity
import roguelike.model.Difficulty
import scala.util.Random
import roguelike.model.items.Chest
import roguelike.model.characters.Entity.isAdjacent
import scala.collection.mutable.ListBuffer
import roguelike.model.characters.BasicEnemy
import roguelike.model.characters.Mage
import roguelike.model.characters.Spell
import roguelike.model.characters.ai.MageAI
import roguelike.model.characters.Boss
import roguelike.model.characters.ai.BossAI

class Game(stage: JFXApp3.PrimaryStage) {

  // Initialize player, current level, game log, etc.
  var player: Player = _
  var currentLevel: Level = _
  var currentLevelNumber: Int = 1
  var currentDifficulty: Difficulty = _
  var isPlayerTurn: Boolean = true
  var gameOver: Boolean = false
  var floatingTexts: List[FloatingText] = List()
  var gameScene: GameScene = null
  val hotbar: ListBuffer[Option[Item]] = ListBuffer.fill(9)(None)

  // Make random a member of the Game class
  val random = new Random()

  // Store generated levels
  private val generatedLevels: scala.collection.mutable.Map[Int, Level] =
    scala.collection.mutable.Map.empty

  // Store the position of the stairs used on the previous level
  private var previousStairsX: Int = 0
  private var previousStairsY: Int = 0

  def loadLevelForDifficulty(difficulty: Difficulty): Unit = {
    currentDifficulty = difficulty
    currentLevelNumber = 1
    generateOrLoadLevel(currentLevelNumber)
    placePlayerAtStart()
    addEnemiesAndItems(currentLevel, difficulty)
  }

  def generateNewLevel(difficulty: Difficulty): Unit = {
    val levelWidth = 80
    val levelHeight = 40

    val levelGenerator =
      new LevelGenerator(levelWidth, levelHeight, difficulty, random)

    // Generate the level and store it in the map
    val newLevel = levelGenerator.generate(currentLevelNumber)
    generatedLevels += (currentLevelNumber -> newLevel)
    currentLevel = newLevel

    if (currentLevelNumber == 1) {
      placePlayerAtStart()
    } else if (currentLevelNumber == 2) {
      placePlayerAtLevelStart()
    }
    addEnemiesAndItems(currentLevel, difficulty)
  }

  def generateOrLoadLevel(levelNumber: Int): Unit = {
    // Check if the level has already been generated
    if (generatedLevels.contains(levelNumber)) {
      currentLevel = generatedLevels(levelNumber)
    } else {
      generateNewLevel(currentDifficulty)
    }
  }

  def useStairs(x: Int, y: Int): Unit = {
    val currentStairsType = currentLevel.tiles(x)(y).terrainType
    if (
      currentLevelNumber == 2 && currentStairsType == TerrainType.StairsUp
    ) {
      currentLevelNumber += 1
      println(s"Player moved up to level ${currentLevelNumber}")
      generateOrLoadLevel(currentLevelNumber)
      placePlayerAtStairs(currentLevelNumber)
      addEnemiesAndItems(currentLevel, currentDifficulty)
      isPlayerTurn = false
      return
    }

    if (
      currentStairsType == TerrainType.StairsUp || currentStairsType == TerrainType.StairsDown
    ) {
      // Store the position of the stairs used
      previousStairsX = x
      previousStairsY = y

      // Update the current level number
      if (currentStairsType == TerrainType.StairsUp) {
        currentLevelNumber += 1
        println(s"Player moved up to level ${currentLevelNumber}")
      } else {
        currentLevelNumber -= 1
        println(s"Player moved down to level ${currentLevelNumber}")
      }

      // Generate or load the new level
      generateOrLoadLevel(currentLevelNumber)

      // Determine the type of stairs to place the player at on the new level
      val newStairsType =
        if (currentStairsType == TerrainType.StairsUp) {
          TerrainType.StairsDown
        } else {
          TerrainType.StairsUp
        }

      // Place the player at the corresponding stairs
      placePlayerAtStairs(currentLevelNumber)
    } else {
      println("No stairs here.")
    }
  }

  private def placePlayerAtStairs(levelNumber: Int): Unit = {
    if (player == null) {
      placePlayer()
    }

    // On level 3, place the player at the StairsDown tile
    if (levelNumber == 3) {
      placePlayerOnStairsDown()
    } else if (levelNumber == 2) {
      placePlayerOnStairsDown()
    } else {
      val newStairsType =
        if (currentLevelNumber > levelNumber) TerrainType.StairsDown
        else TerrainType.StairsUp
      placePlayerAtCorrespondingStairs(newStairsType)
    }
    if (!currentLevel.entities.contains(player)) {
      currentLevel.entities = player :: currentLevel.entities
    }
  }

  private def placePlayerOnStairsDown(): Unit = {
    val stairsDownPos = for {
      x <- 0 until currentLevel.width
      y <- 0 until currentLevel.height
      if currentLevel.tiles(x)(y).terrainType == TerrainType.StairsDown
    } yield (x, y)

    if (stairsDownPos.nonEmpty) {
      val (x, y) = stairsDownPos.head
      player.x = x
      player.y = y
    } else {
      println("Error: No StairsDown tile found on level 3.") //line by chatgpt
    }
  }

  private def placePlayerAtCorrespondingStairs(stairType: TerrainType): Unit = {
    val correspondingStairsPositions = for {
      x <- 0 until currentLevel.width
      y <- 0 until currentLevel.height
      if currentLevel.tiles(x)(y).terrainType == stairType
    } yield (x, y)

    if (correspondingStairsPositions.nonEmpty) {
      val closestStairs = correspondingStairsPositions.minBy { case (x, y) =>
        val dx = x - previousStairsX
        val dy = y - previousStairsY
        dx * dx + dy * dy
      }

      player.x = closestStairs._1
      player.y = closestStairs._2
    } else {
      println(s"Error: No $stairType found in level data.") //line by chatgpt
    }
  }

  // Helper method to place the player at the start of the level
  private def placePlayerAtStart(): Unit = {
    // Find all valid floor positions on the first level
    val validFloorPositions = for {
      x <- 0 until currentLevel.width
      y <- 0 until currentLevel.height
      if currentLevel.tiles(x)(y).terrainType == TerrainType.Floor
    } yield (x, y)

    if (validFloorPositions.nonEmpty) {
      val (x, y) = validFloorPositions(random.nextInt(validFloorPositions.length))

      if (player == null) {
        player = new Player(x, y, '@', Color.White, "Player", new Stats(100, 100, 10, 5))
        player.loadAnimations()
        player.setGame(this)
        currentLevel.entities = player :: currentLevel.entities
      } else {
        player.x = x
        player.y = y

        if (!currentLevel.entities.contains(player)) {
          currentLevel.entities = player :: currentLevel.entities
        }
      }
    } else {
      // Handle the case where no valid floor position is found (line by chatgpt)
      println("Error: No valid floor position found for the player on level 1.")
    }
  }

  private def placePlayerAtLevelStart(): Unit = {
    val stairsUpPosition = for {
      x <- 0 until currentLevel.width
      y <- 0 until currentLevel.height
      if currentLevel.tiles(x)(y).terrainType == TerrainType.StairsUp
    } yield (x, y)

    if (stairsUpPosition.nonEmpty) {
      val (x, y) = stairsUpPosition.head
      if (player == null) {
        player = new Player(x, y, '@', Color.White, "Player", new Stats(100, 100, 10, 5))
        player.loadAnimations()
        player.setGame(this)
        currentLevel.entities = player :: currentLevel.entities
      } else {
        player.x = x
        player.y = y

        if (!currentLevel.entities.contains(player)) {
          currentLevel.entities = player :: currentLevel.entities
        }
      }
    } else {
      println("Error: No StairsUp position found for player.") //line by chatgpt
    }
  }

  private def placePlayer(): Unit = {
    if (player == null) {
      player = new Player(
        0,
        0,
        '@',
        Color.White,
        "Player",
        new Stats(100, 100, 10, 5)
      )
      player.loadAnimations()
      player.setGame(this)
      currentLevel.entities = player :: currentLevel.entities
    }
  }

  private def addEnemiesAndItems(
                                  level: Level,
                                  difficulty: Difficulty
                                ): Unit = {
    if (level.levelNumber == 3) {
      // Place only the boss on level 3
      var bossPlaced = false
      while (!bossPlaced) {
        val x = random.nextInt(level.width)
        val y = random.nextInt(level.height)
        if (
          level.tiles(x)(y).terrainType == TerrainType.Floor3 && !level.entities
            .exists(e => e.x == x && e.y == y)
        ) {
          val bossStats = new Stats(250, 250, 100, 20)
          val boss = new Boss(x, y, 'B', Color.DarkRed, "Boss", bossStats, new BossAI())
          boss.setGame(this)
          level.entities = boss :: level.entities
          bossPlaced = true
        }
      }
    } else {
      val numberOfEnemies = difficulty match {
        case Difficulty.Easy => 5
        case Difficulty.Medium => 10
        case Difficulty.Hard => 1
      }

      for (_ <- 0 until numberOfEnemies) {
        var (x, y) = (0, 0)
        var validPosition = false
        while (!validPosition) {
          x = random.nextInt(level.width)
          y = random.nextInt(level.height)
          val tileType =
            if (level.levelNumber == 1) TerrainType.Floor
            else TerrainType.Floor2 // Level 2

          validPosition =
            level.tiles(x)(y).terrainType == tileType &&
              !level.entities.exists(e => e.x == x && e.y == y)
        }

        val enemyStats = difficulty match {
          case Difficulty.Easy => new Stats(20, 20, 15, 1)
        }

        val enemy =
          if (level.levelNumber == 2) {
            new Mage(
              x,
              y,
              'M',
              Color.Purple,
              "Mage",
              new Stats(
                enemyStats.maxHealth * 2,
                enemyStats.maxHealth * 2,
                enemyStats.attack * 2,
                enemyStats.defense
              ),
              new MageAI()
            )
          } else {
            new BasicEnemy(
              x,
              y,
              if (difficulty == Difficulty.Hard) Color.DarkRed else Color.Orange,
              "Basic Enemy",
              enemyStats,
              new MeleeAI()
            )
          }

        enemy.setGame(this)
        level.entities = enemy :: level.entities
      }
    }
  }

  def movePlayer(dx: Int, dy: Int): Unit = {
    val newX = player.x + dx
    val newY = player.y + dy

    // Check if the new position is within bounds and not blocking
    if (currentLevel.isWithinBounds(newX, newY)) {
      val newTile = currentLevel.tiles(newX)(newY)

      // Check if the new position is a staircase
      if (
        newTile.terrainType == TerrainType.StairsUp || newTile.terrainType == TerrainType.StairsDown
      ) {
        useStairs(newX, newY)
        isPlayerTurn = false
        return
      }
      if (!newTile.isBlocking) {
        player.move(dx, dy, currentLevel)
        isPlayerTurn = false
      }
    }
  }

  def useHotbarItem(index: Int): Unit = {
    if (index >= 0 && index < hotbar.length) {
      hotbar(index) match {
        case Some(item) =>
          // Use the item on the player
          item.use(player)

          if (item.isInstanceOf[HealthPotion] || item.isInstanceOf[StrengthPotion]) {
            hotbar(index) = None
            println(s"Removed ${item.name} from hotbar")
          } else if (item.isInstanceOf[ScrollOfTeleportation]) {
            hotbar(index) = None
            println(s"Removed ${item.name} from hotbar")
          }

          // Update the UI to see the changes
          updateUI()

          isPlayerTurn = false
        case None =>
          println(s"No item in hotbar slot ${index + 1}")
      }
    }
  }

  def update(): Unit = {
    if (!isPlayerTurn) {
      currentLevel.entities = currentLevel.entities.flatMap {
        case spell: Spell =>
          spell.update()
          if (spell.isVisible) {
            spell.checkCollision(player)
            Some(spell)
          } else None
        case enemy: Enemy =>
          enemy.ai.performAction(enemy, this)
          enemy.updateAnimation(this)
          Some(enemy)
        case other => Some(other)
      }

      isPlayerTurn = true
      cleanupDeadEntities()
    }
    floatingTexts.foreach(_.update())
    floatingTexts = floatingTexts.filter(_.isVisible())
    if (player.isAttacking) {
      player.currentAnimation = player.attackAnimation
      if (
        player.currentAnimation.currentFrame == player.currentAnimation.frames.length - 1
      ) {
        player.isAttacking = false
      }
    } else if (player.isRunning) {
      player.currentAnimation = player.runAnimation
    } else {
      player.currentAnimation = player.idleAnimation
    }
    player.updateAnimation(this)
    player.updateEffects()
    if (gameScene != null) {
      Platform.runLater {
        gameScene.updateInventoryUI()
      }
    }
  }

  def addFloatingText(text: FloatingText): Unit = {
    floatingTexts = text :: floatingTexts
  }

  private def cleanupDeadEntities(): Unit = {
    currentLevel.entities = currentLevel.entities.filterNot {
      case p: Player if p.isDead() =>
        println("Game Over! Player has died.")
        false
      case e: Enemy => e.isDead() && e.deathAnimationFinished
      case _ => false
    }
  }

  // Add a method to reset the game
  def resetGame(): Unit = {
    player = null
    currentLevel = null
    currentLevelNumber = 1
    gameOver = false
    isPlayerTurn = true
  }

  def pickUpItem(gameScene: GameScene): Unit = {
    val pickupRange = 1

    // Pick up items at the player's current position
    val itemsAtPlayer =
      currentLevel.items.filter(item => item.x == player.x && item.y == player.y)
    itemsAtPlayer.foreach { it =>
      if (!addItemToHotbar(it)) {
        player.inventory.addItem(it)
        println(s"Added ${it.name} to inventory")
      } else {
        println(s"Added ${it.name} to hotbar")
      }
      currentLevel.items =
        currentLevel.items.filterNot(i => i.x == it.x && i.y == it.y)
      println(s"Picked up a ${it.name} from player's position")
    }

    // Check for items within the pickup range (excluding the player's position)
    for {
      dx <- -pickupRange to pickupRange
      dy <- -pickupRange to pickupRange
      if dx != 0 || dy != 0
      newX = player.x + dx
      newY = player.y + dy
      if currentLevel.isWithinBounds(newX, newY)
    } {
      // Use the correct floor type based on the current level
      val floorType = currentLevel.levelNumber match {
        case 1 => TerrainType.Floor
        case 2 => TerrainType.Floor2
        case 3 => TerrainType.Floor3
        case _ => TerrainType.Floor
      }

      // Check if there's an item of the correct floor type at the new position
      val itemOpt = currentLevel.items.find(item =>
        item.x == newX && item.y == newY && currentLevel.tiles(newX)(newY).terrainType == floorType
      )

      itemOpt.foreach { it =>
        if (!addItemToHotbar(it)) {
          player.inventory.addItem(it)
          println(s"Added ${it.name} to inventory")
        } else {
          println(s"Added ${it.name} to hotbar")
        }
        currentLevel.items =
          currentLevel.items.filterNot(i => i.x == it.x && i.y == it.y)
        println(s"Picked up a ${it.name} at offset ($dx, $dy)")
      }
    }

    if (gameScene != null) then
      Platform.runLater {
        gameScene.updateInventoryUI()
      }
  }

  def addItemToHotbar(item: Item): Boolean = {
    if (player.inventory.getItems().contains(item)) {
      return false
    }

    val hotbarStart = 0
    val hotbarSize = 9

    // Check if there's an empty slot in the hotbar
    for (i <- hotbarStart until hotbarSize) {
      if (hotbar(i).isEmpty) {
        hotbar(i) = Some(item)
        return true
      }
    }
    false
  }

  def removeItemFromInventory(item: Item): Unit = {
    player.inventory.removeItem(item)
    for (i <- 0 until hotbar.length) {
      if (hotbar(i).contains(item)) {
        hotbar(i) = None
      }
    }
  }

  // Add a method to remove an item from the player's inventory
  def dropItem(item: Item): Unit = {
    player.inventory.removeItem(item)
  }

  def removeEnemy(enemy: Enemy): Unit = {
    currentLevel.entities = currentLevel.entities.filterNot(_ == enemy)
  }

  def setGameScene(gameScene: GameScene): Unit = {
    this.gameScene = gameScene
  }

  def updateUI(): Unit = {
    if (gameScene != null) {
      Platform.runLater {
        gameScene.updateInventoryUI()
        gameScene.render()
      }
    }
  }

  def openChest(player: Player): Unit = {
    println("openChest() called") // Debugging print(line by chatgpt) 

    currentLevel.entities.foreach {
      case c: Chest =>
        println(
          s"Found chest at: (${c.x}, ${c.y}), isOpen: ${c.isOpen}"
        ) // Debugging print(line by chatgpt)
        if (isAdjacent(player, c) && !c.isOpen) {
          println(s"Chest is adjacent and closed. Trying to open.") // Debugging print(line by chatgpt)
          c.open(this)
          println(s"Player opened a chest at (${c.x}, ${c.y})")
          updateUI()
          return
        }
      case _ =>
    }
  }
}
