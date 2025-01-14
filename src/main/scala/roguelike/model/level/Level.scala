package roguelike.model.level

import roguelike.model.characters.Entity
import scala.collection.mutable.ListBuffer
import roguelike.model.items.Item
import roguelike.model.characters.Player

class Level(
             val width: Int,
             val height: Int,
             val tiles: Array[Array[Tile]],
             var entities: List[Entity],
             var items: ListBuffer[Item] = ListBuffer.empty[Item],
             val levelNumber: Int = 1
           ) {

  def isTileBlocking(x: Int, y: Int): Boolean = {
    val isBlocking =
      if (isWithinBounds(x, y)) {
        tiles(x)(y).isBlocking || entities.exists(e =>
          e.x == x && e.y == y && !e.isInstanceOf[Player]
        )
      } else {
        true
      }
    isBlocking
  }

  def isWithinBounds(x: Int, y: Int): Boolean = {
    val withinBounds = x >= 0 && x < width && y >= 0 && y < height
    withinBounds
  }

  def addEntity(entity: Entity): Unit = {
    entities = entity :: entities
  }

  def addItem(item: Item, x: Int, y: Int): Unit = {
    item.x = x
    item.y = y
    items += item
  }

  def removeItem(item: Item): Unit = {
    items -= item
  }
}