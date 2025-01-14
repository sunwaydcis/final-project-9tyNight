package roguelike.ui

import scalafx.scene.layout.StackPane
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color
import scalafx.geometry.Insets
import roguelike.model.items.{HealthPotion, StrengthPotion, ScrollOfTeleportation}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{
  ClipboardContent,
  DataFormat,
  TransferMode,
  MouseEvent,
  DragEvent
}
import roguelike.model.items.{
  Item,
  Weapon,
  Pendant,
  Ring,
  Chestplate,
  Helmet,
  Shield
}
import scalafx.Includes._
import InventoryData.itemDataFormat
import scalafx.application.Platform
import roguelike.model.characters.Player

class HotbarSlot(val gameScene: GameScene, val slotId: String)
  extends StackPane
    with Slot {
  val slotSize = 32

  private val backgroundRect = new Rectangle {
    width = slotSize
    height = slotSize
    fill = Color.LightGray
  }

  backgroundRect.width <== width - 4
  backgroundRect.height <== height - 4

  children = Seq(backgroundRect)
  padding = Insets(2)

  private var currentItem: Option[Item] = None
  val itemImageView: ImageView = new ImageView() {
    fitWidth = slotSize - 4
    fitHeight = slotSize - 4
  }
  children.add(itemImageView)

  // Handles drag detection on the slot.
  this.onDragDetected = (event: MouseEvent) => {
    getItem() match {
      case Some(item) =>
        val db = startDragAndDrop(TransferMode.Copy)
        val content = new ClipboardContent()
        content.put(itemDataFormat, item)

        content.putString(slotId)

        db.setContent(content)

        db.dragView = itemImageView.image.value
        db.dragViewOffsetX = event.x
        db.dragViewOffsetY = event.y

        event.consume()
      case None => 
    }
  }

  // Handles drag over events.
  this.onDragOver = (event: DragEvent) => {
    if (
      event.gestureSource != this && event.dragboard.hasContent(itemDataFormat)
    ) {
      event.acceptTransferModes(TransferMode.Copy)
    }
    event.consume()
  }

  // Handles drag dropped events.
  this.onDragDropped = (event: DragEvent) => {
    val db = event.dragboard
    var success = false
    if (db.hasContent(itemDataFormat)) {
      val droppedItem = db.getContent(itemDataFormat).asInstanceOf[Item]
      val sourceSlotId = db.getString
      val sourceSlotOpt = gameScene.inventorySlots
        .find(_.slotId == sourceSlotId)
        .orElse(gameScene.equipmentSlots.find(_.slotId == sourceSlotId))
        .orElse(gameScene.hotbarSlots.find(_.slotId == sourceSlotId))

      sourceSlotOpt match {
        case Some(sourceSlot: Slot) =>
          val targetItem = getItem()

          sourceSlot match {
            case sourceInventorySlot: InventorySlot =>
              // Move item from inventory to hotbar
              gameScene.getGame().player.inventory.removeItem(droppedItem)

              val targetIndex = gameScene.hotbarSlots.indexOf(this)
              gameScene.getGame().hotbar(targetIndex) = Some(droppedItem)
              setItem(Some(droppedItem))

              sourceSlot.setItem(targetItem)
              success = true

            case sourceHotbarSlot: HotbarSlot =>
              // Swap items within hotbar
              val sourceIndex = gameScene.hotbarSlots.indexOf(sourceHotbarSlot)
              val targetIndex = gameScene.hotbarSlots.indexOf(this)

              gameScene.getGame().hotbar(sourceIndex) = targetItem
              gameScene.getGame().hotbar(targetIndex) = Some(droppedItem)

              sourceHotbarSlot.setItem(targetItem)
              setItem(Some(droppedItem))
              success = true

            case sourceEquipmentSlot: EquipmentSlot =>
              // Move item from equipment to hotbar
              val targetIndex = gameScene.hotbarSlots.indexOf(this)

              if (
                !gameScene
                  .getGame()
                  .player
                  .inventory
                  .getItems()
                  .contains(droppedItem) &&
                  !gameScene.getGame().hotbar.contains(Some(droppedItem))
              ) {
                gameScene.getGame().player.inventory.addItem(droppedItem)
              }

              gameScene.getGame().hotbar(targetIndex) = Some(droppedItem)

              setItem(Some(droppedItem))

              sourceSlot.setItem(None)
              gameScene
                .getGame()
                .player
                .unequipBySlot(sourceEquipmentSlot.allowedType)

              success = true
            case _ =>
          }

        case None =>
          println("Error: Source slot not found.")
      }
    }
    event.setDropCompleted(success)
    event.consume()
  }

  // Sets the item in the slot.
  def setItem(item: Option[Item]): Unit = {
    currentItem = item
    item match {
      case Some(i) =>
        val imagePath = s"/ui/items/${i.imageName}"
        val imageStream = getClass.getResourceAsStream(imagePath)
        if (imageStream != null) {
          itemImageView.image = new Image(imageStream)
        } else {
          println(s"ERROR: Could not find resource: $imagePath")
          itemImageView.image = null 
        }
      case None =>
        itemImageView.image = null
    }
  }

  // Clears the slot.
  def clearSlot(): Unit = {
    setItem(None)
  }
  
  def useItem(player: Player): Unit = {
    currentItem.foreach { item =>
      item.use(player)

      // Remove consumable items from the hotbar after use.
      if (
        item.isInstanceOf[HealthPotion] || item.isInstanceOf[StrengthPotion] ||
          item.isInstanceOf[ScrollOfTeleportation]
      ) {
        clearSlot()
        val hotbarIndex = gameScene.hotbarSlots.indexOf(this)
        if (hotbarIndex != -1) {
          gameScene.getGame().hotbar(hotbarIndex) = None
        }
      }

      gameScene.getGame().updateUI()
      gameScene.getGame().isPlayerTurn = false
    }
  }

  override def getItem(): Option[Item] = currentItem

  override def setGameScene(gameScene: GameScene): Unit = {}
}