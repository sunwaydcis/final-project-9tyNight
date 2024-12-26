package roguelike.model.characters

import roguelike.model.Game

trait AI {
  def performAction(self: Enemy, game: Game): Unit
}