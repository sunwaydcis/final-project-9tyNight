package roguelike.monster

import roguelike.dungeon.Dungeon
import roguelike.player.Player
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

case class Monster(
                    var x: Int,
                    var y: Int,
                    var health: Int = 50,
                    val maxHealth: Int = 50,
                    var attackPower: Int = 5,
                    var symbol: Char = 'M',
                    var color: scalafx.scene.paint.Color = Color.Red,
                    var moveCooldown: Int = 0,
                    val healthBar: Rectangle = new Rectangle {
                      width = 10
                      height = 3
                      fill = Color.Red
                      visible = false
                    }
                  ) {

  val maxMoveCooldown = 10

  def takeDamage(damage: Int): Unit = {
    health -= damage
    if (health < 0) health = 0
    updateHealthBar()
  }

  def attack(player: Player): Unit = {
    println(s"Monster attacks Player!")
    player.takeDamage(attackPower)
  }

  // Enhanced AI - move towards the player if within a certain range, otherwise move randomly
  def update(player: Player, dungeon: Dungeon, monsterVBoxes: Map[Monster, VBox]): Unit = {
    println(s"Monster at (${x}, ${y}) updating. moveCooldown: ${moveCooldown}")
    if (moveCooldown > 0) {
      moveCooldown -= 1
      println(s"  Cooling down. Cooldown: ${moveCooldown}")
      return
    }

    val distanceToPlayer = math.sqrt(math.pow(player.x - x, 2) + math.pow(player.y - y, 2))
    println(s"  Distance to player: ${distanceToPlayer}")

    if (distanceToPlayer <= 5) {
      val dx = (player.x - x).sign
      val dy = (player.y - y).sign

      val newX = x + dx
      val newY = y + dy

      println(s"  Trying to move towards player to (${newX}, ${newY})")

      if (isValidMove(newX, newY, dungeon)) {
        x = newX
        y = newY
        println(s"  Moved to (${x}, ${y})")
        moveCooldown = maxMoveCooldown
        monsterVBoxes.get(this).foreach { vbox =>
          vbox.translateX = x * 10
          vbox.translateY = y * 10 - 5
        }
      } else {
        println(s"  Move blocked by obstacle.")
      }
    } else {
      val randomMove = scala.util.Random.nextInt(4)
      val (dx, dy) = randomMove match {
        case 0 => (1, 0)
        case 1 => (-1, 0)
        case 2 => (0, 1)
        case 3 => (0, -1)
        case _ => (0, 0)
      }

      val newX = x + dx
      val newY = y + dy

      println(s"  Random move: dx = ${dx}, dy = ${dy}, newX = ${newX}, newY = ${newY}")

      if (isValidMove(newX, newY, dungeon)) {
        x = newX
        y = newY
        println(s"  Moved to (${x}, ${y})")
        moveCooldown = maxMoveCooldown

        monsterVBoxes.get(this).foreach { vbox =>
          vbox.translateX = x * 10
          vbox.translateY = y * 10 - 5
        }
      } else {
        println(s"  Random move blocked.")
      }
    }
  }

  // Check if the new position is within the dungeon and not a wall
  private def isValidMove(newX: Int, newY: Int, dungeon: Dungeon): Boolean = {
    val isValid = newX >= 0 && newX < dungeon.width && newY >= 0 && newY < dungeon.height && dungeon.grid(newY)(newX) != '#'
    println(s"    isValidMove(${newX}, ${newY}) = ${isValid}")
    isValid
  }
  // Method to update the health bar's visibility and width
  def updateHealthBar(): Unit = {
    if (health < maxHealth) {
      healthBar.visible = true
      healthBar.width = 10 * (health.toDouble / maxHealth.toDouble)
    } else {
      healthBar.visible = false
    }
  }
}

object Monster {
  def placeMonsters(dungeon: Dungeon, numMonsters: Int): List[Monster] = {
    val random = new scala.util.Random()
    val monsters = scala.collection.mutable.ListBuffer.empty[Monster]

    for (_ <- 0 until numMonsters) {
      var placed = false
      while (!placed) {
        val room = dungeon.rooms(random.nextInt(dungeon.rooms.length))
        val x = room.x + random.nextInt(room.width)
        val y = room.y + random.nextInt(room.height)

        if (dungeon.grid(y)(x) == '.' && !monsters.exists(m => m.x == x && m.y == y)) {
          monsters += Monster(x, y) 
          placed = true
        }
      }
    }

    monsters.toList
  }
}