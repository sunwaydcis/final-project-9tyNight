package roguelike.ui

import roguelike.model.items.Item

trait Slot {
  def setItem(item: Option[Item]): Unit
  def clearSlot(): Unit
  def setGameScene(gameScene: GameScene): Unit
  def getItem(): Option[Item]
}