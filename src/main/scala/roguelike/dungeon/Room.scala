package roguelike.dungeon

case class Room(x: Int, y: Int, width: Int, height: Int):
  def overlaps(other: Room): Boolean =
    x < other.x + other.width &&
      x + width > other.x &&
      y < other.y + other.height &&
      y + height > other.y

  // Get the center of the room
  def center: (Int, Int) =
    (x + width / 2, y + height / 2)