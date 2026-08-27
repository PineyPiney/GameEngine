package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.VulkanResourceFactory
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngine

abstract class VulkanGameEngine<E : WindowGameLogic>(override val window: WindowI, val vulkanManager: VulkanManager) :
	WindowedGameEngine<E>(FileResourcesLoader(VulkanResourceFactory(vulkanManager))) {

	override lateinit var activeScreen: E

	init {
		GameEngineI.defaultFont = "Simplified Hans Light"
	}

	override fun cleanUp() {
		vulkanManager.device.waitIdle()
		super.cleanUp()
		vulkanManager.cleanUp()
	}
}