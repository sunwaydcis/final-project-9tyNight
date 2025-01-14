package roguelike.model.characters.ai

import roguelike.model.Game
import roguelike.model.characters.Enemy

trait AI {
  def performAction(self: Enemy, game: Game): Unit
}