package roguelike

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

import roguelike.dungeon.*

object MyApp extends JFXApp3:

  override def start(): Unit =
    val dungeonWidth = 50
    val dungeonHeight = 30
    val numRooms = 4

    val dungeon = DungeonGenerator.generate(dungeonWidth, dungeonHeight, numRooms)
    
    dungeon.rooms.foreach(room => println(s"Room at (${room.x}, ${room.y}), size: ${room.width}x${room.height}"))
    
    printDungeon(dungeon)

    stage = new PrimaryStage {
      title = "Roguelike Dungeon"
      scene = new Scene {
        fill = Color.Black //background colour
        content = dungeon.rooms.map { room =>
          new Rectangle {
            x = room.x * 10 
            y = room.y * 10
            width = room.width * 10
            height = room.height * 10
            fill = Color.White // Room color
          }
        } ++ createCorridorRectangles(dungeon) // Add corridors to the scene
      }
    }
  
  def printDungeon(dungeon: Dungeon): Unit =
    // Fill the grid with walls initially
    for i <- 0 until dungeon.height do
      for j <- 0 until dungeon.width do
        dungeon.grid(i)(j) = '#'
    
    dungeon.rooms.foreach { room =>
      for i <- room.y until room.y + room.height do
        for j <- room.x until room.x + room.width do
          dungeon.grid(i)(j) = '.'
    }

    // Print the grid
    for row <- dungeon.grid do
      println(row.mkString)
  
  def createCorridorRectangles(dungeon: Dungeon): Seq[Rectangle] =
    val corridorRects = for {
      row <- 0 until dungeon.height
      col <- 0 until dungeon.width
      if dungeon.grid(row)(col) == '.' && !dungeon.rooms.exists(room => isPointInRoom(col, row, room))
    } yield new Rectangle {
      x = col * 10
      y = row * 10
      width = 10
      height = 10
      fill = Color.White
    }
    corridorRects
  
  def isPointInRoom(x: Int, y: Int, room: Room): Boolean =
    x >= room.x && x < room.x + room.width && y >= room.y && y < room.y + room.height

end MyApp