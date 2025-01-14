package roguelike.model.items

import roguelike.model.characters.Player

class Equipment() {
  var weapon: Option[Weapon] = None
  var chestplate: Option[Chestplate] = None
  var helmet: Option[Helmet] = None
  var shield: Option[Shield] = None
  var pendant: Option[Pendant] = None
  var ring: Option[Ring] = None
  
}