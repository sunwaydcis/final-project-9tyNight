package roguelike.ui

import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.VBox
import scalafx.application.JFXApp3
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.application.Platform
import roguelike.model.Game

class MainMenu(stage: JFXApp3.PrimaryStage) extends Scene {
  private val playButton = new Button("Play") {
    onAction = _ => {
      stage.scene = new DifficultySelectionMenu(stage)
    }
  }

  private val exitButton = new Button("Exit") {
    onAction = _ => Platform.exit()
  }

  root = new VBox(10) {
    padding = Insets(25)
    alignment = Pos.Center
    children = Seq(playButton, exitButton)
  }
}