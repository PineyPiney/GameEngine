package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.GameEngineI.Companion.defaultFont
import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.util.directory
import org.lwjgl.opengl.GL11C.glClearColor
import org.lwjgl.opengl.GL11C.glEnable
import org.lwjgl.opengl.GL13C.GL_MULTISAMPLE

fun main() {

    LibrarySetUp.initLibraries()

    val window = ExampleWindow()

    val fileResources = FileResourcesLoader("$directory/src/main/resources")

    val engine = object : GameEngine<Game>(fileResources) {
        override val window: WindowI = window
        override var TARGET_FPS: Int = 1000
        override val TARGET_UPS: Int = 20

        init {
            defaultFont = "Large Font"

            // Create all the fonts
            FontLoader.INSTANCE.loadFontWithTexture("Large Font.bmp", resourcesLoader, 128, 256, 0.0625f)
            FontLoader.INSTANCE.loadFontFromTTF("LightSlab.ttf", resourcesLoader, res = 200)
        }

        override fun init() {
            super.init()
            glEnable(GL_MULTISAMPLE)
            glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        }

        override var activeScreen: Game = Game(this)
    }

    engine.run()
}