package roguelike

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.input.KeyEvent
import scalafx.scene.input.KeyCode
import scalafx.Includes.*
import scalafx.animation.{AnimationTimer, FadeTransition}
import scalafx.scene.layout.HBox
import scalafx.scene.control.Label
import scalafx.geometry.Insets
import scalafx.scene.text.Font
import scalafx.scene.layout.VBox
import scalafx.scene.control.Button
import roguelike.dungeon.*
import roguelike.player.*
import roguelike.monster.*
import roguelike.items.*
import scalafx.beans.property.ObjectProperty
import scala.collection.mutable.ListBuffer
import scalafx.scene.Node
import scalafx.util.Duration

object MyApp extends JFXApp3:

  var player: Player = _
  var dungeon: Dungeon = _
  var monsters: List[Monster] = List()
  var items: List[Item] = List()
  var isPlayerTurn: Boolean = true
  val inventoryUI: ObjectProperty[Option[VBox]] = ObjectProperty(None)
  val damageLabels: ListBuffer[Label] = ListBuffer()
  var monsterVBoxes: Map[Monster, VBox] = Map.empty

  override def start(): Unit = {
    val dungeonWidth = 50
    val dungeonHeight = 30
    val numRooms = 4

    dungeon = DungeonGenerator.generate(dungeonWidth, dungeonHeight, numRooms)

    dungeon.rooms.foreach(room => println(s"Room at (${room.x}, ${room.y}), size: ${room.width}x${room.height}"))

    printDungeon(dungeon)

    // Create the player and place them in the first room
    player = Player(dungeon.rooms.head.center._1, dungeon.rooms.head.center._2)

    // Create monsters
    monsters = Monster.placeMonsters(dungeon, numMonsters = 5) // Place 5 monsters

    // Create items and place them in the dungeon
    items = placeItemsInDungeon(dungeon, 3) // Example: Place 3 health potions

    val testLabel = new Label("TEST") {
      style = "-fx-text-fill: yellow; -fx-font-weight: bold; -fx-font-size: 20;"
      layoutX = 200 // Example position
      layoutY = 200
    }

    stage = new PrimaryStage {
      title = "Roguelike Dungeon"
      scene = new Scene {
        fill = Color.Black
        content = Seq(testLabel) ++ createDungeonContent() ++ createMonsterRectangles() ++ createItemRectangles() ++ Seq(createPlayerRectangle())


        onKeyPressed = (event: KeyEvent) => {
          if (isPlayerTurn) {
            val originalX = player.x
            val originalY = player.y
            event.code match {
              case KeyCode.W    => player.move(0, -1, dungeon)
              case KeyCode.S  => player.move(0, 1, dungeon)
              case KeyCode.A => player.move(-1, 0, dungeon)
              case KeyCode.D => player.move(1, 0, dungeon)
              case KeyCode.Space => handleCombat()
              case KeyCode.I => toggleInventoryUI()
              case _             =>
            }
            if (player.x != originalX || player.y != originalY) {
              handleItemPickup()
            }
            updateSceneContent()
            isPlayerTurn = false // End player's turn
            println(s"isPlayerTurn set to: ${isPlayerTurn}")
          }
        }
      }
    }

    // Start the game loop
    var lastUpdateTime = 0L
    val timer: AnimationTimer = AnimationTimer { currentNanoTime =>
      if (lastUpdateTime > 0) {
        val deltaTime = (currentNanoTime - lastUpdateTime) / 1e9
        if (!isPlayerTurn) {
          println("Monsters' turn")
          monsters.foreach(_.update(player, dungeon, monsterVBoxes))
          handleCombat()
          updateSceneContent()
          isPlayerTurn = true
          println(s"isPlayerTurn set to: ${isPlayerTurn}")
        }
      }
      lastUpdateTime = currentNanoTime
    }
    timer.start()
  }

  // Helper function to create scene content for the dungeon, player, and monsters
  def createDungeonContent(): Seq[scalafx.scene.Node] = {
    dungeon.rooms.map { room =>
      new Rectangle {
        x = room.x * 10
        y = room.y * 10
        width = room.width * 10
        height = room.height * 10
        fill = Color.White
      }
    } ++ createCorridorRectangles(dungeon) ++ Seq(createPlayerRectangle())
  }

  // Helper function to create Rectangles for monsters
  def createMonsterRectangles(): Seq[Node] =
    monsters.map { monster =>
      val monsterRect = new Rectangle {
        width = 10
        height = 10
        fill = monster.color
      }

      monster.healthBar.x = monster.x * 10
      monster.healthBar.y = monster.y * 10 - 5

      val vbox = new VBox {
        translateX = monster.x * 10
        translateY = monster.y * 10 - 5
        children = Seq(monster.healthBar, monsterRect)
      }

      monsterVBoxes += (monster -> vbox)

      vbox
    }

  // Helper function to create Rectangles for items
  def createItemRectangles(): Seq[Rectangle] =
    items.map { item =>
      new Rectangle {
        x = item.x * 10
        y = item.y * 10
        width = 10
        height = 10
        fill = item.color
      }
    }

  // Helper function to update the scene content after player or monster movement
  def updateSceneContent(): Unit = {
    stage.scene().content = createDungeonContent() ++ createMonsterRectangles() ++ createItemRectangles() ++ Seq(createPlayerRectangle()) ++ inventoryUI.value.toSeq ++ damageLabels
  }

  // Helper function to print the dungeon to the console (for testing)
  def printDungeon(dungeon: Dungeon): Unit =
    // Fill the grid with walls initially
    for i <- 0 until dungeon.height do
      for j <- 0 until dungeon.width do
        dungeon.grid(i)(j) = '#'

    dungeon.rooms.foreach { room =>
      for i <- room.y until room.y + room.height do
        for j <- room.x until room.x + room.width do
          dungeon.grid(i)(j) = '.'
    }


    for row <- dungeon.grid do
      println(row.mkString)

  // Helper function to create Rectangles for corridors
  def createCorridorRectangles(dungeon: Dungeon): Seq[Rectangle] =
    val corridorRects = for {
      row <- 0 until dungeon.height
      col <- 0 until dungeon.width
      if dungeon.grid(row)(col) == '.' && !dungeon.rooms.exists(room => isPointInRoom(col, row, room))
    } yield new Rectangle {
      x = col * 10
      y = row * 10
      width = 10
      height = 10
      fill = Color.White
    }
    corridorRects

  // Helper function to check if a point is inside a room
  def isPointInRoom(x: Int, y: Int, room: Room): Boolean =
    x >= room.x && x < room.x + room.width && y >= room.y && y < room.y + room.height

  // Helper function to create a Rectangle for the player
  def createPlayerRectangle(): Rectangle =
    new Rectangle {
      x = player.x * 10
      y = player.y * 10
      width = 10
      height = 10
      fill = player.color
    }

  // Combat handling
  def handleCombat(): Unit = {
    // Check for adjacent monsters
    val adjacentMonsters = monsters.filter(m => isAdjacent(player.x, player.y, m.x, m.y))

    if (adjacentMonsters.nonEmpty) {
      val targetMonster = adjacentMonsters.head
      val damage = player.attackPower

      targetMonster.takeDamage(damage)
      displayDamageNumber(damage, targetMonster.x, targetMonster.y)

      targetMonster.updateHealthBar()

      if (targetMonster.health <= 0) {
        monsters = monsters.filterNot(_ == targetMonster)
        monsterVBoxes -= targetMonster
      }
    }

    monsters.filter(m => isAdjacent(player.x, player.y, m.x, m.y)).foreach { monster =>
      val damage = monster.attackPower
      player.takeDamage(damage)
      displayDamageNumber(damage, player.x, player.y)
    }

    updateSceneContent()
  }

  // Helper function to check if two entities are adjacent
  def isAdjacent(x1: Int, y1: Int, x2: Int, y2: Int): Boolean = {
    math.abs(x1 - x2) <= 1 && math.abs(y1 - y2) <= 1
  }

  // Helper function to handle item pickup
  def handleItemPickup(): Unit = {
    val itemToRemove = items.find(item => item.x == player.x && item.y == player.y)
    itemToRemove.foreach { item =>
      player.pickupItem(item)
      items = items.filterNot(_ == item)
      updateSceneContent()
      println(s"Player picked up a ${item.name}, quantity: ${item.quantity}")
    }
  }

  // Helper function to display the player's inventory (for testing)
  def displayInventory(): Unit = {
    println("Inventory:")
    if (player.inventory.isEmpty) {
      println("  (Empty)")
    } else {
      player.inventory.foreach(item => println(s"  - ${item.name}"))
    }
  }

  // Helper function to place items in the dungeon
  def placeItemsInDungeon(dungeon: Dungeon, numItems: Int): List[Item] = {
    val random = new scala.util.Random()
    val items = scala.collection.mutable.ListBuffer.empty[Item]

    for (_ <- 0 until numItems) {
      var placed = false
      while (!placed) {
        val room = dungeon.rooms(random.nextInt(dungeon.rooms.length))
        val x = room.x + random.nextInt(room.width)
        val y = room.y + random.nextInt(room.height)

        if (dungeon.grid(y)(x) == '.' && !items.exists(i => i.x == x && i.y == y)) {
          val item: Item = random.nextInt(100) match {
            case n if n < 50 => HealthPotion(x, y)
            case n if n < 80 => StrengthPotion(x, y)
            case n if n < 95 => ScrollOfTeleportation(x, y)
            case _ => HealthPotion(x, y)
          }
          item.quantity = 1
          items += item
          placed = true
        }
      }
    }

    items.toList
  }

  def toggleInventoryUI(): Unit = {
    inventoryUI.value = inventoryUI.value match {
      case Some(_) => None
      case None => Some(createInventoryUI())
    }
    updateSceneContent()
  }

  private def createInventoryUI(): VBox = {
    val inventoryDisplay = new VBox {
      padding = Insets(10)
      spacing = 10
      style = "-fx-background-color: rgba(0, 0, 0, 0.8);"
      children = new Label {
        text = "Inventory"
        font = Font("Arial", 16)
        style = "-fx-text-fill: white;"
      } +: player.inventory.groupBy(item => (item.name, item.rarity)).map { case ((name, rarity), items) =>
        val item = items.head
        new HBox {
          spacing = 10
          children = Seq(
            new Label {
              text = s"$name ($rarity) - ${item.description}, Quantity: ${item.quantity}"
              font = Font("Arial", 14)
              style = "-fx-text-fill: white;"
            },
            new Button {
              text = "Use 1"
              onAction = _ => {
                item.use(player, dungeon)
                item.quantity -= 1
                if (item.quantity == 0) {
                  player.inventory = player.inventory.filterNot(_ == item)
                }
                toggleInventoryUI()
                isPlayerTurn = false
              }
            },
            new Button {
              text = "Use All"
              onAction = _ => {
                for (_ <- 0 until item.quantity) {
                  item.use(player, dungeon)
                }
                player.inventory = player.inventory.filterNot(_ == item)
                toggleInventoryUI()
                isPlayerTurn = false
              }
            }
          )
        }
      }.toSeq :+ new Button {
        text = "Close"
        onAction = _ => toggleInventoryUI()
      }
    }
    inventoryDisplay
  }

  // Helper function to display a damage number that fades out
  def displayDamageNumber(damage: Int, x: Int, y: Int): Unit = {
    val damageLabel = new Label(s"-$damage") {
      style = "-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 16;"
      layoutX = x * 10
      layoutY = y * 10 - 20 // Position above the entity
    }

    stage.scene().content += damageLabel
    println(s"Damage label added to scene at: (${damageLabel.layoutX.value}, ${damageLabel.layoutY.value})")
    
    val fadeOut = new FadeTransition(Duration(1000), damageLabel) {
      fromValue = 1.0
      toValue = 0.0
      onFinished = () => {
        stage.scene().content -= damageLabel // Remove after animation
        damageLabels -= damageLabel
        println("Damage label removed from scene")
      }
    }
    damageLabels += damageLabel
    fadeOut.play()
  }

end MyApp