package roguelike.monster

import roguelike.dungeon.Dungeon
import roguelike.player.Player

case class Monster(
                    var x: Int,
                    var y: Int,
                    var health: Int = 50,
                    val maxHealth: Int = 50,
                    var attackPower: Int = 5,
                    var symbol: Char = 'M',
                    var color: scalafx.scene.paint.Color = scalafx.scene.paint.Color.Red,
                    var moveCooldown: Int = 0
                  ) {

  val maxMoveCooldown = 10 

  def takeDamage(damage: Int): Unit = {
    health -= damage
    if (health < 0) health = 0
  }

  def attack(player: Player): Unit = {
    println(s"Monster attacks Player!")
    player.takeDamage(attackPower)
  }


  def update(player: Player, dungeon: Dungeon): Unit = {
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

        // Check if the position is valid for a monster
        if (dungeon.grid(y)(x) == '.' && !monsters.exists(m => m.x == x && m.y == y)) {
          monsters += Monster(x, y) // Use default values for health, attackPower, etc.
          placed = true
        }
      }
    }

    monsters.toList
  }
}