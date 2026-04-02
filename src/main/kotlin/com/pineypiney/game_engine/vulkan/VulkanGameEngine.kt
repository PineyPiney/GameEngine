package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngine

class VulkanGameEngine<E : WindowGameLogic>(override val window: WindowI, val screen: (VulkanGameEngine<E>) -> E) : WindowedGameEngine<E>(FileResourcesLoader()) {

	val vulkanManager = VulkanManager(window)

	override lateinit var activeScreen: E

	override fun setLogic() {
		activeScreen = screen(this)
	}

	override fun cleanUp() {
		super.cleanUp()
		vulkanManager.cleanUp()
	}
}