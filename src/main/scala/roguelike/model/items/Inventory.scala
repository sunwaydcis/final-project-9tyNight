package roguelike.model.items

import scala.collection.mutable.ListBuffer
import roguelike.model.characters.Entity

class Inventory {
  private val items: ListBuffer[Item] = ListBuffer.empty[Item]

  def addItem(item: Item): Unit = {
    items += item
    println(s"Added ${item.name} to inventory.")
  }

  def removeItem(item: Item): Unit = {
    items -= item
  }

  def getItems(): List[Item] = items.toList

  // Use an item on the target
  def useItem(item: Item, target: Entity): Unit = {
    if (items.contains(item)) {
      item.use(target)
      removeItem(item)
      println(s"Used ${item.name} on ${target.name}")
    }
  }
}