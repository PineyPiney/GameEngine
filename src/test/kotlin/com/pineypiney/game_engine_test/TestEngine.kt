package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngine
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec4.Vec4

class TestEngine<E: WindowGameLogic>(override val window: WindowI ,screen: (WindowedGameEngineI<E>) -> E, ups: Int, fps: Int): WindowedGameEngine<E>(FileResourcesLoader()) {

	override val TARGET_UPS: Int = ups
	override val TARGET_FPS: Int = fps

	init {
		GameEngineI.defaultFont = "SemiSlab"

		// Create all the fonts
		FontLoader.INSTANCE.loadFontFromTexture("Large Font.png", resourcesLoader, 128, 256, 0.03125f)
		FontLoader.INSTANCE.loadFontFromTTF("SemiSlab.ttf", resourcesLoader, res = 200)
	}

	override var activeScreen: E = screen(this)

	override fun init() {
		super.init()
		GLFunc.multiSample = true
		GLFunc.clearColour = Vec4(1.0f, 0.0f, 0.0f, 1.0f)
	}
}