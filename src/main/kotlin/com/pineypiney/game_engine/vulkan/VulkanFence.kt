package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deleteable
import org.lwjgl.vulkan.VK10

class VulkanFence(val device: VulkanDevice, val handle: Long) : Deleteable {

	fun wait(timeout: Long): Int {
		return VK10.vkWaitForFences(device.device, handle, true, timeout)
	}

	fun reset(): Int {
		return VK10.vkResetFences(device.device, handle)
	}

	override fun delete() {
		VK10.vkDestroyFence(device.device, handle, null)
	}
}