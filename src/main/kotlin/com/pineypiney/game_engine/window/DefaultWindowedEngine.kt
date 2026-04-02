package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader

class DefaultWindowedEngine<E : WindowGameLogic>(
	override val window: WindowI,
	val screen: (DefaultWindowedEngine<E>) -> E,
	resources: ResourcesLoader = FileResourcesLoader(),
	ups: Int = 20,
	fps: Int = 2000
) : WindowedGameEngine<E>(resources) {

	override lateinit var activeScreen: E
	override val TARGET_FPS: Int = fps
	override val TARGET_UPS: Int = ups

	override fun loadResources() {
		super.loadResources()

		GameEngineI.defaultFont = "Simplified Hans Light"
		//FontLoader.INSTANCE.loadFontFromTexture("Large Font.png", resourcesLoader, 128, 256, 0.03125f)
	}

	override fun setLogic() {
		activeScreen = screen(this)
	}
}