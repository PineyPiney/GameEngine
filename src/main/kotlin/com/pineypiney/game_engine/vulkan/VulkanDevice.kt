package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deleteable
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDevice
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties

class VulkanDevice(val device: VkDevice, val queueFamilyIndex: Int, val memoryProperties: VkPhysicalDeviceMemoryProperties) : Deleteable {

	fun waitIdle() = VK10.vkDeviceWaitIdle(device)

	override fun delete() {
		VK10.vkDestroyDevice(device, null)
	}
}