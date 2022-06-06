package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.IGameLogic
import org.lwjgl.opengl.GL11C.glClearColor

fun main() {

    val window = ExampleWindow("Example Window", 960, 540, false)

    val engine = object : GameEngine(window) {
        override var TARGET_FPS: Int = 1000
        override val TARGET_UPS: Int = 20

        override fun init() {
            super.init()
            glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        }

        override var activeScreen: IGameLogic = Game(this)
    }

    engine.run()
}