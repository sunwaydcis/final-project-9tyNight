package roguelike

import scalafx.application.JFXApp3
import roguelike.ui.MainMenu
import scalafx.scene.Scene

object Main extends JFXApp3 {
  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "Roguelike Game"
    }
    stage.scene = new MainMenu(stage)
  }
}