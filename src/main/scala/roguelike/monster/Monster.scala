package roguelike.monster

import roguelike.dungeon.Dungeon
import roguelike.player.Player

case class Monster(
                    var x: Int,
                    var y: Int,
                    var health: Int = 50,
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

  // Enhanced AI - move towards the player if within a certain range, otherwise move randomly
  def update(player: Player, dungeon: Dungeon): Unit = {
    if (moveCooldown > 0) {
      moveCooldown -= 1
      return
    }

    val distanceToPlayer = math.sqrt(math.pow(player.x - x, 2) + math.pow(player.y - y, 2))

    if (distanceToPlayer <= 5) {
      val dx = (player.x - x).sign
      val dy = (player.y - y).sign

      val newX = x + dx
      val newY = y + dy

      if (isValidMove(newX, newY, dungeon)) {
        x = newX
        y = newY
        moveCooldown = maxMoveCooldown
      }
    } else {
      val randomMove = scala.util.Random.nextInt(4)
      randomMove match {
        case 0 => if (isValidMove(x + 1, y, dungeon)) x += 1 // Move right
        case 1 => if (isValidMove(x - 1, y, dungeon)) x -= 1 // Move left
        case 2 => if (isValidMove(x, y + 1, dungeon)) y += 1 // Move down
        case 3 => if (isValidMove(x, y - 1, dungeon)) y -= 1 // Move up
        case _ =>
      }
      moveCooldown = maxMoveCooldown
    }
  }

  private def isValidMove(newX: Int, newY: Int, dungeon: Dungeon): Boolean = {
    newX >= 0 && newX < dungeon.width && newY >= 0 && newY < dungeon.height && dungeon.grid(newY)(newX) != '#'
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