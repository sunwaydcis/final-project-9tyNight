package roguelike.ui

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.VBox
import scalafx.application.JFXApp3
import roguelike.model.Difficulty
import roguelike.model.Game

class GameOverMenu(stage: JFXApp3.PrimaryStage) extends Scene {
  // "Play Again" button
  private val playAgainButton = new Button("Play Again") {
    onAction = _ => {
      val game = new Game(stage) 
      game.loadLevelForDifficulty(Difficulty.Easy)
      stage.scene = new GameScene(stage, game)
    }
  }

  // "Home" button
  private val homeButton = new Button("Home") {
    onAction = _ => stage.scene = new MainMenu(stage)
  }

  // Layout
  root = new VBox(10) {
    padding = Insets(25)
    alignment = Pos.Center
    children = Seq(playAgainButton, homeButton)
  }
}