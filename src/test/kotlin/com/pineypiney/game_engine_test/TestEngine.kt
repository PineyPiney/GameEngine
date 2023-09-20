package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowedGameEngine
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec4.Vec4

class TestEngine<E: WindowGameLogic>(resourcesLoader: ResourcesLoader, screen: (WindowedGameEngineI<E>) -> E): WindowedGameEngine<E>(resourcesLoader) {

    override val window: TestWindow = TestWindow.INSTANCE
    override var TARGET_FPS: Int = 1000
    override val TARGET_UPS: Int = 20

    init {
        GameEngineI.defaultFont = "SemiSlab"

        // Create all the fonts
        FontLoader.INSTANCE.loadFontFromTexture("Large Font.png", resourcesLoader, 128, 256, 0.03125f)
        FontLoader.INSTANCE.loadFontFromTTF("SemiSlab.ttf", resourcesLoader, res = 200)
    }

    override fun init() {
        super.init()
        GLFunc.multiSample = true
        GLFunc.clearColour = Vec4(1.0f, 0.0f, 0.0f, 1.0f)
    }

    override var activeScreen: E = screen(this)
}