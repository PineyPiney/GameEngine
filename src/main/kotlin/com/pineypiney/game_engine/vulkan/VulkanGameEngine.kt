package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngine
import org.lwjgl.vulkan.VK10

class VulkanGameEngine<E : WindowGameLogic>(override val window: WindowI, screen: (VulkanGameEngine<E>) -> E) : WindowedGameEngine<E>(FileResourcesLoader()) {

	val instance = VkUtil.createInstance(true)
	val gpu = VkUtil.getPhysicalDevices(instance).first()
	val device = VkUtil.getDevice(gpu.physicalDevice)
	val surface = VkUtil.createSurface(instance, window)
	val colourFormatSpace = VkUtil.getColourFormatAndSpace(gpu, surface.handle)

	override val activeScreen: E = screen(this)

	override fun cleanUp() {
		super.cleanUp()

		surface.delete()
		device.delete()
		VK10.vkDestroyInstance(instance, null)
	}
}