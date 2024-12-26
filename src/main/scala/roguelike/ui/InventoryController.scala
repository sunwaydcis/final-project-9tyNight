package roguelike.ui

import javafx.fxml.FXML
import scalafx.scene.layout.GridPane
import scalafx.scene.control.Label
import roguelike.model.items.Item
import scalafx.scene.image.{ImageView, Image}
import scalafx.scene.input.MouseEvent
import roguelike.model.characters.Player
import roguelike.model.items.Inventory
import scalafx.Includes._

class InventoryController {

  @FXML
  private var inventoryGrid: GridPane = _ 

  @FXML
  private var itemDescriptionLabel: Label = _

  private var playerInventory: Option[Inventory] = None

  def setPlayer(player: Player): Unit = {
    this.playerInventory = Some(player.inventory)
    updateInventory()
  }

  def updateInventory(): Unit = {
    playerInventory.foreach { inventory =>
      inventoryGrid.children.clear()
      
      for ((item, index) <- inventory.getItems().zipWithIndex) {
        val row = index / 5 
        val col = index % 5
        
        val itemImageView = new ImageView(new Image(getClass.getResourceAsStream(s"/ui/${item.name}.png"))) {
          fitWidth = 32 
          fitHeight = 32
          onMouseClicked = (event: MouseEvent) => {
            handleItemClick(item)
          }
        }
        
        inventoryGrid.add(itemImageView, col, row)
      }
    }
  }

  private def handleItemClick(item: Item): Unit = {
    itemDescriptionLabel.text = item.description
  }
}