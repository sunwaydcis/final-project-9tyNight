package roguelike.model.characters

import scalafx.scene.image.Image

case class Animation(frames: List[Image], var currentFrame: Int = 0, var frameDuration: Int = 5) {

  private var frameTimer: Int = 0

  def update(): Unit = {
    frameTimer += 1
    if (frameTimer >= frameDuration) {
      currentFrame = (currentFrame + 1) % frames.length
      frameTimer = 0
    }
  }

  def getCurrentFrame(): Image = {
    frames(currentFrame)
  }

  def reset(): Unit = {
    currentFrame = 0
    frameTimer = 0
  }
}