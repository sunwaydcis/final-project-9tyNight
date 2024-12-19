package roguelike.dungeon

import scala.util.Random
import scala.collection.mutable.{ListBuffer, PriorityQueue}

object DungeonGenerator {
  def generate(width: Int, height: Int, roomCount: Int): Dungeon = {
    val random = Random()
    var rooms: List[Room] = List()

    while (rooms.size < roomCount) {
      val roomWidth = random.nextInt(10) + 5
      val roomHeight = random.nextInt(10) + 5
      val x = random.nextInt(width - roomWidth)
      val y = random.nextInt(height - roomHeight)
      val newRoom = Room(x, y, roomWidth, roomHeight)

      if (!rooms.exists(_.overlaps(newRoom))) {
        rooms = newRoom :: rooms
      }
    }

    val dungeon = Dungeon(width, height, rooms)
    
    for (i <- 0 until height) {
      for (j <- 0 until width) {
        dungeon.grid(i)(j) = '#'
      }
    }

    // Carve out the rooms
    rooms.foreach { room =>
      for (i <- room.y until room.y + room.height) {
        for (j <- room.x until room.x + room.width) {
          dungeon.grid(i)(j) = '.'
        }
      }
    }

    // Connect rooms with corridors using A*
    connectRoomsWithAStar(dungeon, rooms)

    dungeon
  }

  private def connectRoomsWithAStar(dungeon: Dungeon, rooms: List[Room]): Unit =
    for i <- 0 until rooms.length - 1 do
      val (startX, startY) = rooms(i).center
      val (endX, endY) = rooms(i + 1).center
      val startNode = Node(startX, startY)
      val endNode = Node(endX, endY)
      aStar(dungeon, startNode, endNode)

  private def aStar(dungeon: Dungeon, startNode: Node, endNode: Node): Unit = {
    val openList = PriorityQueue.empty[Node](Ordering.by(_.fCost))
    val closedList = ListBuffer.empty[Node]

    startNode.gCost = 0
    startNode.calculateHCost(endNode)
    openList.enqueue(startNode)

    while (openList.nonEmpty) {
      val currentNode = openList.dequeue()

      if (currentNode.x == endNode.x && currentNode.y == endNode.y) {
        // Reconstruct and draw the path
        reconstructPath(dungeon, currentNode)
        return
      }

      closedList += currentNode

      // Get neighbors (within the grid and not walls)
      val neighbors = getNeighbors(dungeon, currentNode)
      for (neighbor <- neighbors) {
        val isClosed = closedList.exists(n => n.x == neighbor.x && n.y == neighbor.y)
        if (!isClosed) {
          val tentativeGCost = currentNode.gCost + 1

          val inOpenList = openList.exists(n => n.x == neighbor.x && n.y == neighbor.y)
          if (tentativeGCost < neighbor.gCost || !inOpenList) {
            neighbor.gCost = tentativeGCost
            neighbor.calculateHCost(endNode)
            neighbor.parent = Some(currentNode)

            if (!inOpenList) {
              openList.enqueue(neighbor)
            }
          }
        }
      }
    }
  }
  
  private def getNeighbors(dungeon: Dungeon, node: Node): List[Node] = {
    val neighbors = ListBuffer.empty[Node]
    val directions = List((0, 1), (1, 0), (0, -1), (-1, 0), (1, 1), (-1, -1), (1, -1), (-1, 1))

    for ((dx, dy) <- directions) {
      val newX = node.x + dx
      val newY = node.y + dy

      if (newX >= 0 && newX < dungeon.width && newY >= 0 && newY < dungeon.height && dungeon.grid(newY)(newX) == '.') {
        neighbors += Node(newX, newY)
      }
    }

    neighbors.toList
  }
  
  private def reconstructPath(dungeon: Dungeon, endNode: Node): Unit = {
    var currentNode = endNode
    while (currentNode.parent.isDefined) {
      dungeon.grid(currentNode.y)(currentNode.x) = '.'
      currentNode = currentNode.parent.get
    }
  }
}