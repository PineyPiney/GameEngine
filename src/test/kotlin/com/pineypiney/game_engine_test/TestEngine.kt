package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.ApiManager
import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.DirectorySource
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.fonts.FontLoader
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngine

class TestEngine<E : WindowGameLogic>(
	override val window: WindowI,
	manager: ApiManager,
	val screen: (TestEngine<E>) -> E,
	ups: Int,
	fps: Int
) : WindowedGameEngine<E>(manager, ResourcesLoader(manager.createResourceFactory(), listOf(DirectorySource("src/test/resources"), DirectorySource("src/main/resources")))) {

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
		activeScreen = screen(this)
		deletionQueue.push(activeScreen)
	}
}