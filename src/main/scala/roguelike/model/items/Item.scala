package roguelike.model.items

import roguelike.model.characters.Entity
import scalafx.scene.paint.Color

trait Item extends Serializable {
  val name: String
  val description: String
  val symbol: Char 
  var x: Int = 0 
  var y: Int = 0 
  val imageName: String

  val color: SerializableColor

  def use(target: Entity): Unit 
  
  def duration: Int
  def duration_=(newDuration: Int): Unit

  // Method to decrement the duration of the item's effect
  def tick(): Unit = {
    if (duration > 0) {
      duration -= 1
    }
  }

  // Method to check if the item's effect has expired
  def isExpired(): Boolean = {
    duration <= 0
  }

  // Method to get the slot where the item can be equipped 
  def equipmentSlot: Option[String] = None
  
  def getColor(): SerializableColor
}