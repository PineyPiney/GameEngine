package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.rendering.opengl.DefaultWindowRenderer
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.OpenGlResourceFactory
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngine
import glm_.vec4.Vec4

class TestOpenGlEngine<E : WindowGameLogic, C : CameraI>(
	override val window: WindowI,
	val screen: (TestOpenGlEngine<E, C>, DefaultWindowRenderer<E, C>) -> E,
	val camera: (WindowI) -> C,
	ups: Int,
	fps: Int
) :
	WindowedGameEngine<E>(FileResourcesLoader(OpenGlResourceFactory())) {

	override val TARGET_UPS: Int = ups
	override val TARGET_FPS: Int = fps

	override lateinit var activeScreen: E

	override fun loadResources() {
		super.loadResources()

		GameEngineI.defaultFont = "SemiSlab"
//		GameEngineI.defaultFont = "Simplified Hans Light"

		// Create all the fonts
		FontLoader.INSTANCE.loadFontFromTTF("SemiSlab.ttf", resourcesLoader, res = 200)
	}

	override fun setLogic() {
		activeScreen = screen(this, DefaultWindowRenderer(window, camera(window)))
	}

	override fun init() {
		super.init()
		GLFunc.multiSample = true
		GLFunc.clearColour = Vec4(1.0f, 0.0f, 0.0f, 1.0f)
	}
}