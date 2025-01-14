package roguelike.ui

import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.VBox
import scalafx.application.JFXApp3
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.application.Platform
import roguelike.model.Game
import roguelike.model.Difficulty

class MainMenu(stage: JFXApp3.PrimaryStage) extends Scene {
  // "Play" button to start the game.
  private val playButton = new Button("Play") {
    onAction = _ => {
      val game = new Game(stage)
      game.loadLevelForDifficulty(Difficulty.Easy)
      val gameScene = new GameScene(stage, game)
      game.setGameScene(gameScene)
      stage.scene = gameScene
    }
  }

  // "Exit" button to terminate the application.
  private val exitButton = new Button("Exit") {
    onAction = _ => Platform.exit()
  }

  // Layout of the main menu.
  root = new VBox(10) {
    padding = Insets(25)
    alignment = Pos.Center
    children = Seq(playButton, exitButton)
  }
}