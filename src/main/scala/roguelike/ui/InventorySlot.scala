package roguelike.ui

import scalafx.scene.layout.StackPane
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color
import scalafx.geometry.Insets
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{
  ClipboardContent,
  DataFormat,
  TransferMode,
  MouseEvent,
  DragEvent
}
import roguelike.model.items.Item
import scalafx.Includes._
import InventoryData.itemDataFormat

class InventorySlot(val gameScene: GameScene, val slotId: String)
  extends StackPane
    with Slot {
  val slotSize = 32

  // Background rectangle for the slot.
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

  // Image view for displaying the item.
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
      event.gestureSource != this && event.dragboard.hasContent(
        itemDataFormat
      )
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

      val sourceSlotOpt = Seq(
        gameScene.inventorySlots.find(_.slotId == sourceSlotId),
        gameScene.equipmentSlots.find(_.slotId == sourceSlotId),
        gameScene.hotbarSlots.find(_.slotId == sourceSlotId)
      ).flatten.headOption

      sourceSlotOpt match {
        case Some(sourceSlot: Slot) =>
          sourceSlot match {
            case sourceInvSlot: InventorySlot =>
              // Swap items between inventory slots
              val targetItem = getItem()
              setItem(Some(droppedItem))
              sourceInvSlot.setItem(targetItem)
              gameScene.getGame().player.inventory.removeItem(droppedItem)
              targetItem.foreach(gameScene.getGame().player.inventory.addItem)
              gameScene.getGame().player.inventory.addItem(droppedItem)
              success = true

            case sourceHotbarSlot: HotbarSlot =>
              // Move item from hotbar to inventory
              setItem(Some(droppedItem))
              gameScene.getGame().player.inventory.addItem(droppedItem)
              sourceHotbarSlot.clearSlot()
              val hotbarIndex = gameScene.hotbarSlots.indexOf(sourceHotbarSlot)
              gameScene.getGame().hotbar(hotbarIndex) = None
              success = true

            case sourceEquipmentSlot: EquipmentSlot =>
              // Move item from equipment to inventory
              setItem(Some(droppedItem))
              gameScene.getGame().player.inventory.addItem(droppedItem)
              sourceEquipmentSlot.clearSlot()
              gameScene.getGame().player.unequipBySlot(sourceEquipmentSlot.allowedType)
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

  override def setGameScene(gameScene: GameScene): Unit = {}

  override def getItem(): Option[Item] = currentItem
}