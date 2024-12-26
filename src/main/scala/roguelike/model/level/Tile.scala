package roguelike.model.level

object Tile {
  val Size = 20
}

class Tile(val x: Int, val y: Int, val terrainType: TerrainType, val isBlocking: Boolean) {
}