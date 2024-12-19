package roguelike.dungeon

import scala.math.sqrt

case class Node(x: Int, y: Int, var gCost: Int = 0, var hCost: Int = 0, var parent: Option[Node] = None):
  def fCost: Int = gCost + hCost
  
  def calculateHCost(endNode: Node): Unit =
    hCost = (Math.abs(x - endNode.x) + Math.abs(y - endNode.y))