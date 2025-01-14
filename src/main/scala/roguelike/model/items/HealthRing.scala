package roguelike.model.items

import scalafx.scene.paint.Color

case class HealthRing() extends Ring("Health Ring", "A magical ring that permanently increases health.", 'R', 50) {
  override val color: SerializableColor = ColorHelper.fromScalaFXColor(Color.Green)

  override val imageName: String = "health_ring.png"
  override def equipmentSlot: Option[String] = Some("Ring")
  override def getColor(): SerializableColor = color
}