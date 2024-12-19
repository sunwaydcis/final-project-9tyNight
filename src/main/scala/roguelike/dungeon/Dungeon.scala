package roguelike.dungeon

case class Dungeon(width: Int, height: Int, rooms: List[Room]):
  val grid: Array[Array[Char]] = Array.fill(height, width)(' ')