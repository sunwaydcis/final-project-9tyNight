package roguelike.model.items

import scala.collection.mutable.ListBuffer
import roguelike.model.characters.Entity
import roguelike.model.Game

case class Inventory(
                      private val items: ListBuffer[Item] = ListBuffer.empty[Item],
                      capacity: Int = 36
                    ) {

  def getItems(): List[Item] = items.toList

  def addItem(item: Item): Boolean = {
    if (items.size < capacity) {
      items += item
      println(s"Added ${item.name} to inventory.")
      true
    } else {
      println("Inventory is full!")
      false
    }
  }

  def removeItem(item: Item): Unit = {
    items -= item
  }

  def useItem(item: Item, user: Entity, game: Game): Unit = {
    item.use(user)
    println(s"${user.name} used ${item.name}.")
    
    if (!(item.isInstanceOf[Weapon] ||
      item.isInstanceOf[Pendant] ||
      item.isInstanceOf[Ring] ||
      item.isInstanceOf[Helmet] ||
      item.isInstanceOf[Chestplate] ||
      item.isInstanceOf[Shield])) {
      removeItem(item)
    }
  }

  def insertItemAt(item: Item, index: Int): Unit = {
    println(
      s"Attempting to insert ${item.name} at index $index. Inventory size: ${items.size}, capacity: $capacity"
    )
    if (index >= 0 && index <= items.size && items.size < capacity) {
      items -= item
      items.insert(index, item)
      println(
        s"Successfully added ${item.name} to inventory at index $index. New inventory size: ${items.size}"
      )
    } else {
      println(
        s"Cannot insert item at the specified index. Index: $index, Size: ${items.size}, Capacity: $capacity"
      )
    }
  }
}