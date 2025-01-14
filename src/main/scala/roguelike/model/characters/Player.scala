package roguelike.model.characters

import roguelike.model.Stats
import roguelike.model.level.Level
import scalafx.scene.paint.Color
import roguelike.model.Game
import roguelike.model.items.{Inventory, Item, Equipment, Weapon, Shield, Chestplate, Pendant, Ring, Helmet}
import scalafx.scene.image.Image
import roguelike.model.FloatingText
import scala.collection.mutable.ListBuffer

class Player(
              _x: Int,
              _y: Int,
              char: Char,
              _color: Color,
              name: String,
              stats: Stats
            ) extends Entity(_x, _y, char, _color, name, stats) {
  val inventory: Inventory = new Inventory()
  val equipment: Equipment = new Equipment()
  val activeEffects: ListBuffer[Item] = ListBuffer.empty[Item]

  // Animation states
  var currentAnimation: Animation = _
  var idleAnimation: Animation = _
  var idleAnimationLeft: Animation = _
  var runAnimation: Animation = _
  var runAnimationLeft: Animation = _
  var attackAnimation: Animation = _
  var attackAnimationLeft: Animation = _
  var deathAnimation: Animation = _
  var deathAnimationLeft: Animation = _

  var isRunning: Boolean = false
  var isAttacking: Boolean = false
  var facingDirection: (Int, Int) = (0, 1)
  var isFacingLeft = false
  var deathAnimationFinished: Boolean = false

  override def setGame(game: Game): Unit = {
    this.game = game
  }

  override def attack(target: Entity, game: Game): Unit = {
    // Trigger the attack animation
    isAttacking = true
    currentAnimation = if (isFacingLeft) attackAnimationLeft else attackAnimation
    currentAnimation.reset()

    // Calculate damage based on equipped items
    val weaponAttack = equipment.weapon.map(_.attack).getOrElse(0)
    val pendantAttack = equipment.pendant.map(_.attack).getOrElse(0)
    val totalAttack = this.stats.attack + weaponAttack + pendantAttack

    // Calculate the damage based on the total attack and the target's defense.
    val damage = math.max(0, totalAttack - target.stats.defense)

    target.takeDamage(damage, game)
    game.isPlayerTurn = false
  }

  def updateEffects(): Unit = {
    activeEffects.foreach(_.tick())

    val expiredEffects = activeEffects.filter(_.isExpired())
    expiredEffects.foreach { item =>
      println(s"${item.name} effect has expired on ${this.name}")
      if (item.name == "Strength Potion") {
        this.stats.attack -= 30
      }
    }
    activeEffects --= expiredEffects
  }

  override def takeDamage(damage: Int, game: Game): Unit = {
    // Calculate defense based on equipped armor and shield
    val armorDefense = equipment.chestplate.map(_.defense).getOrElse(0) +
      equipment.helmet.map(_.defense).getOrElse(0) +
      equipment.shield.map(_.defense).getOrElse(0)
    val totalDefense = this.stats.defense + armorDefense

    // Reduce damage based on total defense, with a minimum damage of 0
    val actualDamage = math.max(0, damage - totalDefense)

    // Apply the damage to the player's health
    this.stats.health -= actualDamage
    println(s"${this.name} takes ${actualDamage} damage. Health: ${this.stats.health}")
    game.addFloatingText(FloatingText(s"-$actualDamage", this.x, this.y, Color.Red, 60))

    if (isDead()) {
      println(s"${this.name} has died!")
    }
  }

  override def isDead(): Boolean = {
    val isDead = this.stats.health <= 0
    if (isDead) {
      println("Player has died!")
    }
    isDead
  }

  override def move(dx: Int, dy: Int, level: Level): Unit = {
    var newX = x + dx
    var newY = y + dy

    // Update facing direction based on movement
    if (dx < 0) {
      isFacingLeft = true
    } else if (dx > 0) {
      isFacingLeft = false
    }

    // Check for collisions with walls or other entities
    if (level.isWithinBounds(newX, newY) && !level.isTileBlocking(newX, newY) && !level.entities.exists(e => e.x == newX && e.y == newY && !e.isInstanceOf[Player])) {
      x = newX
      y = newY
      isRunning = true
      currentAnimation = if (isFacingLeft) runAnimationLeft else runAnimation
    } else {
      isRunning = false
      currentAnimation = if (isFacingLeft) idleAnimationLeft else idleAnimation
    }
  }

  def loadAnimations() = {
    idleAnimation = Animation(
      (1 to 6).map(i => new Image(getClass.getResource(s"/ui/player/Idle/Idle$i.png").toExternalForm)).toList
    )

    idleAnimationLeft = Animation(
      (1 to 6).map(i => new Image(getClass.getResource(s"/ui/player/Idle/Idle_left_$i.png").toExternalForm)).toList
    )

    runAnimation = Animation(
      (1 to 8).map(i => new Image(getClass.getResource(s"/ui/player/Run/Run$i.png").toExternalForm)).toList
    )

    runAnimationLeft = Animation(
      (1 to 8).map(i => new Image(getClass.getResource(s"/ui/player/Run/Run_left_$i.png").toExternalForm)).toList
    )

    attackAnimation = Animation(
      (1 to 13).map(i => new Image(getClass.getResource(s"/ui/player/Attack/Attack$i.png").toExternalForm)).toList
    )

    attackAnimationLeft = Animation(
      (1 to 13).map(i => new Image(getClass.getResource(s"/ui/player/Attack/Attack_left_$i.png").toExternalForm)).toList
    )

    deathAnimation = Animation(
      (1 to 12).map(i => new Image(getClass.getResource(s"/ui/player/Death/Death$i.png").toExternalForm)).toList
    )

    deathAnimationLeft = Animation(
      (1 to 12).map(i => new Image(getClass.getResource(s"/ui/player/Death/Death_left_$i.png").toExternalForm)).toList
    )

    currentAnimation = idleAnimation
  }

  def updateAnimation(game: Game): Unit = {
    if (isDead()) {
      if (!deathAnimationFinished) {
        currentAnimation = if (isFacingLeft) deathAnimationLeft else deathAnimation
        if (currentAnimation.currentFrame == currentAnimation.frames.length - 1) {
          deathAnimationFinished = true
          game.gameOver = true
        }
      }
    } else if (isAttacking) {
      currentAnimation = if (isFacingLeft) attackAnimationLeft else attackAnimation
      // Reset isAttacking when the animation is done
      if (currentAnimation.currentFrame == currentAnimation.frames.length - 1) {
        isAttacking = false
      }
    } else if (isRunning) {
      currentAnimation = if (isFacingLeft) runAnimationLeft else runAnimation
    } else {
      currentAnimation = if (isFacingLeft) idleAnimationLeft else idleAnimation
    }
    currentAnimation.update()
  }

  def getCurrentAnimationFrame(): Image = {
    currentAnimation.getCurrentFrame()
  }

  def continueAttack(game: Game): Unit = {
    val targetX = x + facingDirection._1
    val targetY = y + facingDirection._2
    val target = game.currentLevel.entities.collectFirst {
      case e: Enemy if e.x == targetX && e.y == targetY => e
    }
    
    target.foreach { t =>
      attack(t, game)
      if (t.isDead()) {
        game.removeEnemy(t) 
      }
    }
  }

  override def equip(item: Item): Option[Item] = {
    if (!isEquipped(item)) {
      inventory.removeItem(item)
    }

    val previousItem = item match {
      case weapon: Weapon =>
        equipment.weapon.foreach(inventory.addItem)
        equipment.weapon = Some(weapon)
        println(s"${name} equipped ${weapon.name}")
        Some(weapon)

      case helmet: Helmet =>
        equipment.helmet.foreach(inventory.addItem)
        equipment.helmet = Some(helmet)
        println(s"${name} equipped ${helmet.name}")
        Some(helmet)
      case chestplate: Chestplate =>
        equipment.chestplate.foreach(inventory.addItem)
        equipment.chestplate = Some(chestplate)
        println(s"${name} equipped ${chestplate.name}")
        Some(chestplate)
      case shield: Shield =>
        equipment.shield.foreach(inventory.addItem)
        equipment.shield = Some(shield)
        println(s"${name} equipped ${shield.name}")
        Some(shield)
      case pendant: Pendant =>
        equipment.pendant.foreach(inventory.addItem)
        equipment.pendant = Some(pendant)
        this.stats.attack += pendant.attack 
        println(
          s"${name} equipped ${pendant.name} and gained ${pendant.attack} attack permanently!"
        )
        Some(pendant)

      case ring: Ring =>
        equipment.ring.foreach(inventory.addItem)
        equipment.ring = Some(ring)
        this.stats.health += ring.health 
        println(
          s"${name} equipped ${ring.name} and gained ${ring.health} health permanently!"
        )
        Some(ring)

      case _ =>
        println("Cannot equip this item.")
        None
    }
    updateStats()
    previousItem
  }

  def unequipBySlot(slotType: String): Option[Item] = {
    val unequippedItem = slotType match {
      case "Weapon" =>
        val unequpped = equipment.weapon
        equipment.weapon.foreach(inventory.addItem)
        equipment.weapon = None
        unequpped
      case "Chestplate" =>
        val unequpped = equipment.chestplate
        equipment.chestplate.foreach(inventory.addItem)
        equipment.chestplate = None
        unequpped
      case "Helmet" =>
        val unequpped = equipment.helmet
        equipment.helmet.foreach(inventory.addItem)
        equipment.helmet = None
        unequpped
      case "Shield" =>
        val unequpped = equipment.shield
        equipment.shield.foreach(inventory.addItem)
        equipment.shield = None
        unequpped
      case "Pendant" =>
        val unequpped = equipment.pendant
        equipment.pendant.foreach { p =>
          inventory.addItem(p)
          this.stats.attack -= p.attack // Remove the attack bonus
        }
        equipment.pendant = None
        unequpped
      case "Ring" =>
        val unequpped = equipment.ring
        equipment.ring.foreach { r =>
          inventory.addItem(r)
          this.stats.health -= r.health 
        }
        equipment.ring = None
        unequpped
      case _ =>
        println(s"Cannot unequip from slot type: $slotType")
        None
    }
    updateStats() 
    unequippedItem
  }

  def isEquipped(item: Item): Boolean = {
    item match {
      case w: Weapon => equipment.weapon.contains(w)
      case h: Helmet => equipment.helmet.contains(h)
      case c: Chestplate => equipment.chestplate.contains(c)
      case s: Shield => equipment.shield.contains(s)
      case p: Pendant => equipment.pendant.contains(p)
      case r: Ring => equipment.ring.contains(r)
      case _ => false
    }
  }

  private def updateStats(): Unit = {
    this.stats.attack = 10
    this.stats.defense = 5
    this.stats.maxHealth = 100 

    // Apply bonuses from equipment
    stats.attack += equipment.weapon.map(_.attack).getOrElse(0)
    stats.attack += equipment.pendant.map(_.attack).getOrElse(0)
    stats.defense += equipment.chestplate.map(_.defense).getOrElse(0)
    stats.defense += equipment.helmet.map(_.defense).getOrElse(0)
    stats.defense += equipment.shield.map(_.defense).getOrElse(0)
    
    this.stats.health = math.min(stats.health, stats.maxHealth)

    println(s"Player stats updated - Attack: ${stats.attack}, Defense: ${stats.defense}, Health: ${stats.health}")
  }
}