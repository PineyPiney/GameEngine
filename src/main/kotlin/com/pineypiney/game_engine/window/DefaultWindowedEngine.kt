package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.text.FontLoader

class DefaultWindowedEngine<E : WindowGameLogic>(
	override val window: Window,
	screen: (DefaultWindowedEngine<E>) -> E,
	resources: FileResourcesLoader = FileResourcesLoader(),
	ups: Int = 20,
	fps: Int = 2000
) : WindowedGameEngine<E>(resources) {
	
	init {
		GameEngineI.defaultFont = "Simplified Hans Light"
		FontLoader.INSTANCE.loadFontFromTexture("Large Font.png", resourcesLoader, 128, 256, 0.03125f)
	}

	override val activeScreen: E = screen(this)
	override val TARGET_FPS: Int = fps
	override val TARGET_UPS: Int = ups
}