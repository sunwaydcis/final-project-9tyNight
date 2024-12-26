package roguelike.ui

import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.VBox
import scalafx.application.JFXApp3
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import roguelike.model.Game
import roguelike.model.Difficulty

class DifficultySelectionMenu(stage: JFXApp3.PrimaryStage) extends Scene {
  private def createDifficultyButton(difficulty: Difficulty, buttonText: String): Button =
    new Button(buttonText) {
      onAction = _ => {
        val game = new Game()
        game.loadLevelForDifficulty(difficulty)
        stage.scene = new GameScene(stage, game)
        game.setGameScene(stage.scene.value.asInstanceOf[GameScene])
      }
    }

  private val easyButton = createDifficultyButton(Difficulty.Easy, "Easy")
  private val mediumButton = createDifficultyButton(Difficulty.Medium, "Medium")
  private val hardButton = createDifficultyButton(Difficulty.Hard, "Hard")
  
  private val backButton = new Button("Back") {
    onAction = _ => stage.scene = new MainMenu(stage) // Go back to the main menu
  }
  
  root = new VBox(10) {
    padding = Insets(25)
    alignment = Pos.Center
    children = Seq(easyButton, mediumButton, hardButton, backButton)
  }
}