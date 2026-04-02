package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.vulkan.KHRSurface
import org.lwjgl.vulkan.VkInstance

class VulkanSurface(val instance: VkInstance, val handle: Long) : Deletable {
	override fun delete() {
		KHRSurface.vkDestroySurfaceKHR(instance, handle, null)
	}
}