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
import roguelike.model.items.{Item, Weapon, Pendant, Ring, Chestplate, Helmet, Shield}
import scalafx.Includes._
import InventoryData.itemDataFormat
import scalafx.scene.input.Dragboard
import scalafx.application.Platform

 //Represents an equipment slot in the game UI.
class EquipmentSlot(val allowedType: String, val gameScene: GameScene)
  extends StackPane
    with Slot {
  private val slotSize = 32

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

  val slotId: String = s"equipment-$allowedType-${hashCode()}"

  // Handles drag detection.
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
    if (event.gestureSource != this && event.dragboard.hasContent(itemDataFormat)) {
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

      if (isItemAllowed(droppedItem)) {
        val sourceSlotId = db.getString
        val sourceSlotOpt = gameScene.inventorySlots.find(_.slotId == sourceSlotId)
          .orElse(gameScene.equipmentSlots.find(_.slotId == sourceSlotId))
          .orElse(gameScene.hotbarSlots.find(_.slotId == sourceSlotId))

        sourceSlotOpt match {
          case Some(sourceSlot: Slot) =>
            val targetItem = getItem()

            gameScene.getGame().player.equip(droppedItem)

            sourceSlot match {
              case sourceInvSlot: InventorySlot =>
                gameScene.getGame().player.inventory.removeItem(droppedItem)
              case sourceHotbarSlot: HotbarSlot =>
                val hotbarIndex = gameScene.hotbarSlots.indexOf(sourceHotbarSlot)
                gameScene.getGame().hotbar(hotbarIndex) = None
              case _: EquipmentSlot =>
              case _ =>
            }

            setItem(Some(droppedItem))
            sourceSlot.setItem(targetItem)

            success = true

          case None =>
            println("Error: Source slot not found.")
        }
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

  // Checks if the item type is allowed in this slot.
  private def isItemAllowed(item: Item): Boolean = {
    allowedType match {
      case "Weapon"     => item.isInstanceOf[Weapon]
      case "Helmet"     => item.isInstanceOf[Helmet]
      case "Chestplate" => item.isInstanceOf[Chestplate]
      case "Shield"     => item.isInstanceOf[Shield]
      case "Pendant"    => item.isInstanceOf[Pendant]
      case "Ring"       => item.isInstanceOf[Ring]
      case _            => false
    }
  }

  override def setGameScene(gameScene: GameScene): Unit = {
  }

  override def getItem(): Option[Item] = currentItem
}