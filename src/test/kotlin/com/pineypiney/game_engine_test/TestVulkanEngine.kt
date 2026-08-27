package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.rendering.vulkan.VulkanGameRenderer
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.vulkan.VulkanGameEngine
import com.pineypiney.game_engine.vulkan.VulkanManager
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI

class TestVulkanEngine<E : WindowGameLogic, C : CameraI>(window: WindowI, val screen: (TestVulkanEngine<E, C>, VulkanGameRenderer<E, C>) -> E, val camera: (WindowI) -> C, ups: Int, fps: Int) :
	VulkanGameEngine<E>(window, VulkanManager()) {

	override val TARGET_UPS: Int = ups
	override val TARGET_FPS: Int = fps

	override fun loadResources() {
		super.loadResources()

		GameEngineI.defaultFont = "SemiSlab"
//		GameEngineI.defaultFont = "Simplified Hans Light"

		// Create all the fonts
		FontLoader.INSTANCE.loadFontFromTTF("SemiSlab.ttf", resourcesLoader, res = 200)
	}

	override fun setLogic() {
		activeScreen = screen(this, VulkanGameRenderer(window, vulkanManager, camera(window)))
	}

}