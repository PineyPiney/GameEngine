package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.GameEngineI.Companion.defaultFont
import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.directory
import glm_.vec4.Vec4

fun main() {

    LibrarySetUp.initLibraries()

    val window = ExampleWindow()

    val fileResources = FileResourcesLoader("$directory/src/main/resources")
    TextureLoader.setFlags("fonts/Large Font.png", 1)

    val engine = object : GameEngine<Game>(fileResources) {
        override val window: ExampleWindow = window
        override var TARGET_FPS: Int = 1000
        override val TARGET_UPS: Int = 20

        init {
            defaultFont = "Large Font"

            // Create all the fonts
            FontLoader.INSTANCE.loadFontFromTexture("Large Font.png", resourcesLoader, 128, 256, 0.03125f)
            FontLoader.INSTANCE.loadFontFromTTF("SemiSlab.ttf", resourcesLoader, res = 200)
        }

        override fun init() {
            super.init()
            GLFunc.multiSample = true
            GLFunc.clearColour = Vec4(1.0f, 0.0f, 0.0f, 1.0f)
        }

        override var activeScreen: Game = Game(this)
    }

    engine.run()
}