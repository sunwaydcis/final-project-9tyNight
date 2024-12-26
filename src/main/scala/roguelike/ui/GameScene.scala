package roguelike.ui

import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.BorderPane
import scalafx.scene.paint.Color
import scalafx.scene.input.KeyEvent
import scalafx.scene.input.KeyCode
import roguelike.model.Game
import roguelike.model.level.Tile
import roguelike.model.level.TerrainType
import roguelike.model.characters.Entity
import scalafx.application.JFXApp3
import scalafx.animation.AnimationTimer
import scalafx.Includes.*
import roguelike.model.characters.{Player, Enemy}
import roguelike.model.items.{HealthPotion, Item}
import scalafx.scene.shape.Rectangle
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class GameScene(stage: JFXApp3.PrimaryStage, game: Game) extends Scene {
  
  val canvas = new Canvas(800, 600) 
  val gc = canvas.graphicsContext2D
  
  val healthBarWidth = 100
  val healthBarHeight = 10
  val healthBarX = 10
  val healthBarY = 10
  val healthBar = new Rectangle {
    width = healthBarWidth
    height = healthBarHeight
    fill = Color.Green
    x = healthBarX
    y = healthBarY
  }
  
  private var centerX: Double = 0.0
  private var centerY: Double = 0.0
  
  val inventoryLabel = new Label("Inventory: ") {
    padding = Insets(10)
    textFill = Color.White
  }
  
  root = new BorderPane {
    center = canvas
    top = new VBox {
      padding = Insets(10)
      alignment = Pos.TopLeft
      children = Seq(healthBar, inventoryLabel)
    }
  }
  
  val timer: AnimationTimer = AnimationTimer(_ => {
    if (game.gameOver) {
      timer.stop()
      stage.scene = new GameOverMenu(stage)
    } else if (!game.isPlayerTurn) {
      game.update() 
      render()
      game.isPlayerTurn = true 
    }
  })
  timer.start()
  
  onKeyPressed = (event: KeyEvent) => {
    if (game.isPlayerTurn) {
      event.code match {
        case KeyCode.W => game.movePlayer(0, -1) 
        case KeyCode.A => game.movePlayer(-1, 0)
        case KeyCode.S => game.movePlayer(0, 1)  
        case KeyCode.D => game.movePlayer(1, 0)  
        case KeyCode.Space => 
          val target = game.currentLevel.entities.collectFirst {
            case e: Enemy if isAdjacent(game.player, e) => e
          }
          target.foreach(game.player.attack(_, game)) 
          game.isPlayerTurn = false
        case KeyCode.U => 
          val inventory = game.player.inventory
          if (inventory.getItems().nonEmpty) {
            inventory.useItem(inventory.getItems().head, game.player)
            game.isPlayerTurn = false 
            updateInventoryUI()
          }
        case KeyCode.P => 
          game.pickUpItem()
        case _ => 
      }
      render() 
    }
  }
  
  private def isAdjacent(entity1: Entity, entity2: Entity): Boolean = {
    math.abs(entity1.x - entity2.x) <= 1 && math.abs(entity1.y - entity2.y) <= 1
  }
  
  private def render(): Unit = {
    gc.fill = Color.Black
    gc.fillRect(0, 0, canvas.width.value, canvas.height.value)
    
    centerX = canvas.width.value / 2 - game.player.x * Tile.Size
    centerY = canvas.height.value / 2 - game.player.y * Tile.Size
    
    for (x <- 0 until game.currentLevel.width; y <- 0 until game.currentLevel.height) {
      val tile = game.currentLevel.tiles(x)(y)
      tile.terrainType match {
        case TerrainType.Wall  => gc.fill = Color.Gray
        case TerrainType.Floor => gc.fill = Color.Brown
        case TerrainType.Door  => gc.fill = Color.SaddleBrown // You might want a different color
        case TerrainType.Stairs => gc.fill = Color.Yellow
        case _                 => gc.fill = Color.Black
      }
      gc.fillRect(x * Tile.Size + centerX, y * Tile.Size + centerY, Tile.Size, Tile.Size)
    }
    
    println(s"Entities: ${game.currentLevel.entities}") 
    var playerRendered = false
    game.currentLevel.entities.foreach {
      case p: Player =>
        println("Found player in entities")
        gc.fill = p.color
        gc.fillText(
          p.char.toString,
          canvas.width.value / 2, 
          canvas.height.value / 2
        )
        playerRendered = true
      case entity =>
        println(s"Drawing entity: ${entity.char} at (${entity.x}, ${entity.y})")
        gc.fill = entity.color
        gc.fillText(
          entity.char.toString,
          entity.x * Tile.Size + centerX,
          entity.y * Tile.Size + centerY
        )
    }
    updateHealthBar()
    renderFloatingTexts()
    renderEnemyHealthBars()
    renderItems()
  }

  private def updateHealthBar(): Unit = {
    val healthPercentage = game.player.stats.health.toDouble / game.player.stats.maxHealth.toDouble
    healthBar.width = healthBarWidth * healthPercentage
    healthBar.fill = if (healthPercentage > 0.6) Color.Green else if (healthPercentage > 0.3) Color.Yellow else Color.Red
  }

  private def renderFloatingTexts(): Unit = {
    for (text <- game.floatingTexts) {
      gc.fill = text.color
      gc.globalAlpha = text.opacity
      gc.fillText(text.text, text.x * Tile.Size + centerX, text.y * Tile.Size + centerY)
      gc.setGlobalAlpha(1.0) 
    }
  }

  private def renderEnemyHealthBars(): Unit = {
    game.currentLevel.entities.foreach {
      case enemy: Enemy =>
        val healthPercentage = enemy.stats.health.toDouble / enemy.stats.maxHealth.toDouble
        val healthBarWidth = Tile.Size 
        val healthBarHeight = 4 
        val healthBarX = enemy.x * Tile.Size + centerX
        val healthBarY = enemy.y * Tile.Size + centerY - healthBarHeight - 2 
        
        gc.fill = Color.Gray
        gc.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight)
        
        gc.fill = if (healthPercentage > 0.6) Color.Green else if (healthPercentage > 0.3) Color.Yellow else Color.Red
        val currentHealthWidth = healthBarWidth * healthPercentage
        gc.fillRect(healthBarX, healthBarY, currentHealthWidth, healthBarHeight)

      case _ => 
    }
  }

  private def renderItems(): Unit = {
    game.currentLevel.items.foreach { item =>
      println(s"Drawing item: ${item.name} at (${item.x}, ${item.y})")
      gc.fill = item match {
        case _: HealthPotion => Color.Blue 
        case _ => Color.Yellow 
      }
      gc.fillText(item.symbol.toString, item.x * Tile.Size + centerX, item.y * Tile.Size + centerY)
    }
  }
  def updateInventoryUI(): Unit = {
    val inventory = game.player.inventory
    val itemsText = if (inventory.getItems().nonEmpty) {
      inventory.getItems().map(_.name).mkString(", ")
    } else {
      "Empty"
    }
    inventoryLabel.text = s"Inventory: $itemsText"
  }
}