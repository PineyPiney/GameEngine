package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.resources.FileResourcesLoader

class DefaultWindowedEngine(override val window: Window, screen: (DefaultWindowedEngine) -> WindowGameLogic, resources: FileResourcesLoader = FileResourcesLoader()): WindowedGameEngine<WindowGameLogic>(resources) {
	override var TARGET_FPS: Int = 1000
	override val TARGET_UPS: Int = 20

	override val activeScreen: WindowGameLogic = screen(this)
}