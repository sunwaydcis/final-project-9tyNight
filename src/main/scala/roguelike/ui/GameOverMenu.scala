package roguelike.ui

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.VBox
import scalafx.application.JFXApp3
import roguelike.model.Difficulty
import roguelike.model.Game

class GameOverMenu(stage: JFXApp3.PrimaryStage) extends Scene {
  private val playAgainButton = new Button("Play Again") {
    onAction = _ => {
      val game = new Game() 
      game.loadLevelForDifficulty(Difficulty.Easy)
      stage.scene = new GameScene(stage, game) // Create a new GameScene
    }
  }
  
  private val homeButton = new Button("Home") {
    onAction = _ => stage.scene = new MainMenu(stage)
  }
  
  root = new VBox(10) {
    padding = Insets(25)
    alignment = Pos.Center
    children = Seq(playAgainButton, homeButton)
  }
}