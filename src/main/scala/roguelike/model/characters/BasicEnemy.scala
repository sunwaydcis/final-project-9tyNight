package roguelike.model.characters

import scalafx.scene.paint.Color
import roguelike.model.Stats
import roguelike.model.characters.ai.AI

class BasicEnemy(
                  _x: Int,
                  _y: Int,
                  _color: Color,
                  name: String,
                  stats: Stats,
                  ai: AI
                ) extends Enemy(_x, _y, 'E', _color, name, stats, ai) {}