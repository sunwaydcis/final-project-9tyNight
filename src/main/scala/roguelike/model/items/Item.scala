package roguelike.model.items

import roguelike.model.characters.Entity

trait Item {
  val name: String
  val description: String
  val symbol: Char
  var x: Int = 0
  var y: Int = 0

  def use(target: Entity): Unit
}