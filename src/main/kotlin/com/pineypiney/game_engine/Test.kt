package com.pineypiney.game_engine

import com.pineypiney.game_engine.cameras.Camera
import com.pineypiney.game_engine.renderers.GameRenderer
import com.pineypiney.game_engine.visual.ScreenObjectCollection

fun main() {

    val window = Window.INSTANCE

    val engine = object : GameEngine(window) {
        override var TARGET_FPS: Int = 1000
        override val TARGET_UPS: Int = 20
        override var activeScreen: IGameLogic = object : GameLogic(this){
            override var camera: Camera = Camera()
            override var renderer: GameRenderer = object : GameRenderer(){
                override fun render(window: Window, camera: Camera, game: IGameLogic, tickDelta: Double) {
                    clear()
                    game.gameObjects.forEachItem { it.render(vp, tickDelta) }
                }
                override fun updateAspectRatio(window: Window, objects: ScreenObjectCollection) {}
                override fun delete() {}
            }
            override fun regenerateFrameBuffers() {}
        }
    }
    engine.run()
}