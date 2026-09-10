package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.VulkanResourceFactory
import com.pineypiney.game_engine.vulkan.VulkanManager

class VulkanWindowedEngine<E : WindowGameLogic>(
	override val window: WindowI,
	val vulkan: VulkanManager,
	val screen: (VulkanWindowedEngine<E>) -> E,
	resources: ResourcesLoader = FileResourcesLoader(VulkanResourceFactory(vulkan)),
	ups: Int = 20,
	fps: Int = 2000
) : WindowedGameEngine<E>(resources) {

	override lateinit var activeScreen: E
	override val TARGET_FPS: Int = fps
	override val TARGET_UPS: Int = ups

	override fun loadResources() {
		super.loadResources()
		GameEngineI.defaultFont = "Simplified Hans Light"
	}

	override fun setLogic() {
		activeScreen = screen(this)
	}

	override fun cleanUp() {
		vulkan.device.waitIdle()
		super.cleanUp()
		vulkan.cleanUp()
	}
}