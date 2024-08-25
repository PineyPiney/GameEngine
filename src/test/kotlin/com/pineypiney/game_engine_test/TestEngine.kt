package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.round
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowedGameEngine
import com.pineypiney.game_engine.window.WindowedGameEngineI
import com.pineypiney.game_engine_test.test3D.Game3D
import glm_.vec4.Vec4

class TestEngine<E: WindowGameLogic>(screen: (WindowedGameEngineI<E>) -> E, resourcesLoader: ResourcesLoader = FileResourcesLoader()): WindowedGameEngine<E>(resourcesLoader) {

	override val window: TestWindow = TestWindow.INSTANCE

	init {
		GameEngineI.defaultFont = "Large Font"

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

	override fun render(tickDelta: Double) {
		super.render(tickDelta)

		if(Timer.frameTime > nextFPSCount){
			nextFPSCount += 5.0
			FPS = FPSTally * 0.2f
			FPSTally = 1
			(activeScreen as? Game3D)?.fpsText?.getComponent<TextRendererComponent>()?.text?.text = "FPS: ${FPS.round(1)}"
		}
		else FPSTally++
	}

	companion object{
		var FPS = 0f
		var nextFPSCount = Timer.getCurrentTime()
		var FPSTally = 0
	}
}