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
import scalafx.application.{JFXApp3, Platform}
import scalafx.animation.AnimationTimer
import scalafx.Includes.*
import roguelike.model.characters.Entity.isAdjacent
import scalafx.Includes.*
import scalafx.event.ActionEvent
import scalafx.scene.layout.{BorderPane, VBox, HBox, GridPane}
import roguelike.model.characters.{Player, Enemy, Mage, Boss}
import roguelike.model.items.{
  HealthPotion,
  Item,
  StrengthPotion,
  ScrollOfTeleportation,
  Chest,
  Weapon,
  BasicChestplate,
  BasicSword,
  BasicHelmet,
  BasicShield,
  SteelShield,
  SteelSword,
  SteelHelmet,
  SteelChestplate,
  IronSword,
  IronHelmet,
  IronShield,
  IronChestplate,
  StrengthPendant,
  HealthRing,
  Helmet,
  Chestplate,
  Shield,
  Pendant,
  Ring
}
import scalafx.scene.shape.Rectangle
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import roguelike.model.characters.ai.MeleeAI
import scalafx.scene.image.Image
import scalafx.scene.image.ImageView
import scalafx.scene.layout.GridPane
import scalafx.scene.input.MouseEvent
import roguelike.ui.InventorySlot
import scalafx.scene.text.Font
import roguelike.model.characters.Spell

class GameScene(stage: JFXApp3.PrimaryStage, val game: Game) extends Scene {

  private val canvasWidth = 800
  private val canvasHeight = 600
  val canvas = new Canvas(canvasWidth, canvasHeight)
  val gc = canvas.graphicsContext2D

  private val healthBarWidth = 100
  private val healthBarHeight = 10
  private val healthBarX = 10
  private val healthBarY = 10
  val healthBar = new Rectangle {
    width = healthBarWidth
    height = healthBarHeight
    fill = Color.Green
    x = healthBarX
    y = healthBarY
  }

  private var centerX: Double = 0.0
  private var centerY: Double = 0.0

  val inventorySlots: Seq[InventorySlot] =
    Seq.fill(36)(new InventorySlot(this, java.util.UUID.randomUUID.toString))

  val inventoryGrid = new GridPane {
    padding = Insets(10)
    hgap = 5
    vgap = 5
    alignment = Pos.TopLeft
    visible = true
  }

  // Add the inventory slots to the grid.
  for {
    row <- 0 until 4
    col <- 0 until 9
    index = row * 9 + col
  } {
    inventoryGrid.add(inventorySlots(index), col, row)
  }

  val playerPortrait = new ImageView(
    new Image(getClass().getResourceAsStream("/ui/player/Portrait.png"))
  ) {
    fitWidth = 64
    fitHeight = 64
  }

  val portraitBox = new VBox {
    padding = Insets(10)
    children = Seq(playerPortrait)
  }

  val equipmentGrid = new GridPane {
    padding = Insets(10)
    hgap = 5
    vgap = 5
    alignment = Pos.TopLeft
    visible = true
  }

  val helmetSlot = new EquipmentSlot("Helmet", this)
  val chestplateSlot = new EquipmentSlot("Chestplate", this)
  val weaponSlot = new EquipmentSlot("Weapon", this)
  val shieldSlot = new EquipmentSlot("Shield", this)
  val pendantSlot = new EquipmentSlot("Pendant", this)
  val ringSlot = new EquipmentSlot("Ring", this)

  val equipmentSlots = Seq(
    helmetSlot,
    chestplateSlot,
    weaponSlot,
    shieldSlot,
    pendantSlot,
    ringSlot
  )

  equipmentGrid.add(helmetSlot, 1, 0)
  equipmentGrid.add(chestplateSlot, 0, 1)
  equipmentGrid.add(weaponSlot, 0, 2)
  equipmentGrid.add(shieldSlot, 2, 2)
  equipmentGrid.add(pendantSlot, 1, 1)
  equipmentGrid.add(ringSlot, 2, 1)

  val hotbarSlots: Seq[HotbarSlot] = Seq.fill(9)(new HotbarSlot(this, java.util.UUID.randomUUID.toString))

  val hotbar = new HBox {
    padding = Insets(10)
    spacing = 5
    alignment = Pos.BottomCenter
    children = hotbarSlots
  }

  val hotbarLabels = (1 to 9).map(_.toString).map(new Label(_))
  for ((label, index) <- hotbarLabels.zipWithIndex) {
    label.textFill = Color.Black
    label.font = new Font("Arial", 10)
    val slot = hotbarSlots(index)
    val vbox = new VBox(label, slot)
    vbox.setAlignment(Pos.TopCenter)
    hotbar.children.add(vbox)
  }

  root = new BorderPane {
    center = canvas
    top = new VBox {
      padding = Insets(10)
      alignment = Pos.TopLeft
      children = Seq(healthBar)
    }
    left = new VBox {
      children = Seq(portraitBox, equipmentGrid, inventoryGrid, hotbar)
    }
  }

  val timer: AnimationTimer = AnimationTimer(_ => {
    if (game.gameOver) {
      timer.stop()
      stage.scene = new GameOverMenu(stage)
    } else {
      if (!game.isPlayerTurn) {
        game.update()
        game.isPlayerTurn = true
      }
      game.player.updateAnimation(game)
      game.currentLevel.entities.foreach {
        case enemy: Enemy => enemy.updateAnimation(game)
        case _           =>
      }
      render()
    }
  })
  timer.start()

  onKeyPressed = (event: KeyEvent) => {
    if (game.isPlayerTurn) {
      event.code match {
        case KeyCode.W => game.movePlayer(0, -1) // Move up
        case KeyCode.A => game.movePlayer(-1, 0) // Move left
        case KeyCode.S => game.movePlayer(0, 1)  // Move down
        case KeyCode.D => game.movePlayer(1, 0)  // Move right
        case KeyCode.Space => // Attack
          val target = game.currentLevel.entities.collectFirst {
            case e: Enemy if isAdjacent(game.player, e) => e
          }
          target.foreach(game.player.attack(_, game))
          game.player.isAttacking = true
          game.player.currentAnimation = game.player.attackAnimation
          game.player.currentAnimation.reset()
          game.isPlayerTurn = false
        case KeyCode.P => // Pickup item
          game.pickUpItem(this)
        case KeyCode.C => // Open chest
          game.openChest(game.player)
          game.isPlayerTurn = false
        case KeyCode.Comma =>
          game.useStairs(game.player.x, game.player.y)
        case KeyCode.Period =>
          game.useStairs(game.player.x, game.player.y)
        case KeyCode.Digit1 => handleHotbarKeyPress(0)
        case KeyCode.Digit2 => handleHotbarKeyPress(1)
        case KeyCode.Digit3 => handleHotbarKeyPress(2)
        case KeyCode.Digit4 => handleHotbarKeyPress(3)
        case KeyCode.Digit5 => handleHotbarKeyPress(4)
        case KeyCode.Digit6 => handleHotbarKeyPress(5)
        case KeyCode.Digit7 => handleHotbarKeyPress(6)
        case KeyCode.Digit8 => handleHotbarKeyPress(7)
        case KeyCode.Digit9 => handleHotbarKeyPress(8)
        case _            =>
      }
      render()
    }
  }

  def getGame(): Game = {
    return game
  }

  def removeItemFromPlayerInventory(item: Item): Unit = {
    game.player.inventory.removeItem(item)
    updateInventoryUI()
  }

  def setGameSceneReference(gameScene: GameScene): Unit = {
    inventorySlots.foreach(_.setGameScene(gameScene))
    hotbarSlots.foreach(_.setGameScene(gameScene))
    equipmentSlots.foreach(_.setGameScene(gameScene))
  }

  setGameSceneReference(this)

  private val tileImages: Map[TerrainType, Image] = loadTileImages()

  private def loadTileImages(): Map[TerrainType, Image] = {
    Map(
      TerrainType.Wall -> new Image(
        getClass.getResource("/ui/tiles/dirt_wall_top.png").toString
      ),
      TerrainType.Floor -> new Image(
        getClass.getResource("/ui/tiles/floor_stone_1.png").toString
      ),
      TerrainType.StairsUp -> new Image(
        getClass.getResource("/ui/tiles/staircase_up.png").toString
      ),
      TerrainType.StairsDown -> new Image(
        getClass.getResource("/ui/tiles/staircase_down.png").toString
      ),
      TerrainType.Floor2 -> new Image(getClass.getResource("/ui/tiles/blue_stone_floor_1_blue_bg.png").toString),
      TerrainType.Wall2 -> new Image(getClass.getResource("/ui/tiles/rough_stone_wall_top.png").toString),
      TerrainType.Floor3 -> new Image(
        getClass.getResource("/ui/tiles/blank_red_floor.png").toString
      ),
      TerrainType.Wall3 -> new Image(
        getClass.getResource("/ui/tiles/skull_wall_top.png").toString
      ),
      TerrainType.CorpseBones1 -> new Image(
        getClass.getResource("/ui/tiles/corpse_bones_1.png").toString
      ),
      TerrainType.CorpseBones2 -> new Image(
        getClass.getResource("/ui/tiles/corpse_bones_2.png").toString
      ),
      TerrainType.BloodSpatter1 -> new Image(
        getClass.getResource("/ui/tiles/blood_spatter_1.png").toString
      ),
      TerrainType.BloodSpatter2 -> new Image(
        getClass.getResource("/ui/tiles/blood_spatter_2.png").toString
      )
    )
  }
  val defaultTileImage: Image = new Image(
    getClass.getResource("/ui/tiles/blank_floor_dark_grey.png").toString
  )

  // Render the game state.
  def render(): Unit = {
    gc.fill = Color.Black
    gc.fillRect(0, 0, canvas.width.value, canvas.height.value)

    centerX = canvas.width.value / 2 - game.player.x * Tile.Size
    centerY = canvas.height.value / 2 - game.player.y * Tile.Size

    for (x <- 0 until game.currentLevel.width;
         y <- 0 until game.currentLevel.height) {
      val tile = game.currentLevel.tiles(x)(y)
      val image = tileImages.getOrElse(tile.terrainType, defaultTileImage)
      gc.drawImage(
        image,
        x * Tile.Size + centerX,
        y * Tile.Size + centerY,
        Tile.Size,
        Tile.Size
      )
    }

    game.currentLevel.entities.foreach {
      case p: Player =>
        gc.fill = game.player.color
        gc.drawImage(
          game.player.getCurrentAnimationFrame(),
          game.player.x * Tile.Size + centerX,
          game.player.y * Tile.Size + centerY,
          Tile.Size,
          Tile.Size

        )
      case mage: Mage =>
        gc.drawImage(
          mage.getCurrentAnimationFrame(),
          mage.x * Tile.Size + centerX,
          mage.y * Tile.Size + centerY,
          Tile.Size,
          Tile.Size
        )
      case enemy: Enemy =>
        gc.drawImage(
          enemy.getCurrentAnimationFrame(),
          enemy.x * Tile.Size + centerX,
          enemy.y * Tile.Size + centerY,
          Tile.Size,
          Tile.Size
        )
      case c: Chest =>
        val chestImage = if (!c.isOpen) {
          new Image(getClass.getResourceAsStream("/ui/items/chest_closed.png"))
        } else {
          new Image(getClass.getResourceAsStream("/ui/items/chest_open.png"))
        }
        gc.drawImage(
          chestImage,
          c.x * Tile.Size + centerX,
          c.y * Tile.Size + centerY,
          Tile.Size,
          Tile.Size
        )
      case spell: Spell =>
        if (spell.isVisible)
          gc.drawImage(
            spell.getCurrentAnimationFrame(),
            spell.x * Tile.Size + centerX,
            spell.y * Tile.Size + centerY,
            Tile.Size,
            Tile.Size
          )
      case boss: Boss =>
        gc.drawImage(
          boss.getCurrentAnimationFrame(),
          boss.x * Tile.Size + centerX,
          boss.y * Tile.Size + centerY,
          Tile.Size,
          Tile.Size
        )
      case entity =>
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

  private def updateEquipmentSlot(slotType: String, item: Option[Item]): Unit = {
    slotType match {
      case "Weapon" => equipmentSlots.find(_.slotId.startsWith("equipment-Weapon")).foreach(_.setItem(item))
      case "Chestplate" => equipmentSlots.find(_.slotId.startsWith("equipment-Chestplate")).foreach(_.setItem(item))
      case "Helmet" => equipmentSlots.find(_.slotId.startsWith("equipment-Helmet")).foreach(_.setItem(item))
      case "Shield" => equipmentSlots.find(_.slotId.startsWith("equipment-Shield")).foreach(_.setItem(item))
      case "Pendant" => equipmentSlots.find(_.slotId.startsWith("equipment-Pendant")).foreach(_.setItem(item))
      case "Ring" => equipmentSlots.find(_.slotId.startsWith("equipment-Ring")).foreach(_.setItem(item))
      case _ => println(s"Unknown equipment slot type: $slotType")
    }
  }

  private def updateHealthBar(): Unit = {
    val healthPercentage =
      game.player.stats.health.toDouble / game.player.stats.maxHealth.toDouble
    healthBar.width = healthBarWidth * healthPercentage
    healthBar.fill = if (healthPercentage > 0.6) Color.Green
    else if (healthPercentage > 0.3) Color.Yellow
    else Color.Red
  }

  private def renderFloatingTexts(): Unit = {
    for (text <- game.floatingTexts) {
      gc.fill = text.color
      gc.globalAlpha = text.opacity
      gc.fillText(
        text.text,
        text.x * Tile.Size + centerX,
        text.y * Tile.Size + centerY
      )
      gc.setGlobalAlpha(1.0)
    }
  }

  private def renderEnemyHealthBars(): Unit = {
    game.currentLevel.entities.foreach {
      case enemy: Enemy =>
        val healthPercentage =
          enemy.stats.health.toDouble / enemy.stats.maxHealth.toDouble
        val healthBarWidth = Tile.Size
        val healthBarHeight = 4
        val healthBarX = enemy.x * Tile.Size + centerX
        val healthBarY =
          enemy.y * Tile.Size + centerY - healthBarHeight - 2

        gc.fill = Color.Gray
        gc.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight)

        gc.fill =
          if (healthPercentage > 0.6) Color.Green
          else if (healthPercentage > 0.3) Color.Yellow
          else Color.Red
        val currentHealthWidth = healthBarWidth * healthPercentage
        gc.fillRect(healthBarX, healthBarY, currentHealthWidth, healthBarHeight)

      case _ =>
    }
  }

  private def renderItems(): Unit = {
    game.currentLevel.items.foreach { item =>
      val imagePath = item match {
        case _: HealthPotion            => "/ui/items/health_potion.png"
        case _: StrengthPotion          => "/ui/items/strength_potion.png"
        case _: ScrollOfTeleportation  => "/ui/items/scroll_of_teleportation.png"
        case _: BasicChestplate         => "/ui/items/basic_chestplate.png"
        case _: SteelChestplate         => "/ui/items/steel_chestplate.png"
        case _: IronChestplate          => "/ui/items/iron_chestplate.png"
        case _: BasicHelmet             => "/ui/items/basic_helmet.png"
        case _: SteelHelmet             => "/ui/items/steel_helmet.png"
        case _: IronHelmet              => "/ui/items/iron_helmet.png"
        case _: BasicShield             => "/ui/items/basic_shield.png"
        case _: SteelShield             => "/ui/items/steel_shield.png"
        case _: IronShield              => "/ui/items/iron_shield.png"
        case _: BasicSword              => "/ui/items/Basic_weapon.png"
        case _: SteelSword              => "/ui/items/steel_weapon.png"
        case _: IronSword               => "/ui/items/iron_weapon.png"
        case _: StrengthPendant         => "/ui/items/strength_pendant.png"
        case _: HealthRing              => "/ui/items/health_ring.png"
        case chest: Chest =>
          if (!chest.isOpen) {
            "/ui/items/chest_closed.png"
          } else {
            "/ui/items/chest_open.png"
          }
        case _ => "/ui/tiles/blank_floor_dark_grey.png"
      }
      val imageStream = getClass.getResourceAsStream(imagePath)
      if (imageStream != null) {
        val image = new Image(imageStream)
        gc.drawImage(
          image,
          item.x * Tile.Size + centerX,
          item.y * Tile.Size + centerY,
          Tile.Size,
          Tile.Size
        )
      } else {
        gc.drawImage(
          defaultTileImage,
          item.x * Tile.Size + centerX,
          item.y * Tile.Size + centerY,
          Tile.Size,
          Tile.Size
        )
      }
    }

  }

   //Handles item click events.
  def handleItemClick(item: Item, player: Player, game: Game): Unit = {
    item.use(player)
    if (!item.isInstanceOf[Weapon] &&
      !item.isInstanceOf[Helmet] &&
      !item.isInstanceOf[Chestplate] &&
      !item.isInstanceOf[Shield] &&
      !item.isInstanceOf[Pendant] &&
      !item.isInstanceOf[Ring]) {
      player.inventory.removeItem(item)
    }
    updateInventoryUI()
  }

  private def handleHotbarKeyPress(index: Int): Unit = {
    if (index >= 0 && index < hotbarSlots.length) {
      hotbarSlots(index).useItem(game.player)
    }
  }

  //Updates the inventory UI elements.

  def updateInventoryUI(): Unit = {
    val inventory = game.player.inventory
    val items = inventory.getItems()

    for (index <- 0 until inventorySlots.size) {
      if (index < items.length) {
        inventorySlots(index).setItem(Some(items(index)))
      } else {
        inventorySlots(index).setItem(None)
      }
    }

    for (index <- 0 until hotbarSlots.size) {
      game.hotbar(index) match {
        case Some(item) => hotbarSlots(index).setItem(Some(item))
        case None       => hotbarSlots(index).setItem(None)
      }
    }
  }

  onKeyReleased = (event: KeyEvent) => {
    event.code match {
      case KeyCode.W | KeyCode.A | KeyCode.S | KeyCode.D =>
        // Reset the movement flag when the key is released
        game.player.isRunning = false
        game.player.currentAnimation = game.player.idleAnimation
      case _ =>
    }
  }
}