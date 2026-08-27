package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.vulkan.VK10

class VulkanFence(val device: VulkanDevice, val handle: Long) : Deletable {

	fun wait(timeout: Number) {
		VkUtil.processResult(VK10.vkWaitForFences(device.device, handle, true, timeout.toLong()), "Failed to wait for Vulkan Fence")
	}

	fun wait(timeout: Double) = wait(timeout.toLong())

	fun reset(): Int {
		return VK10.vkResetFences(device.device, handle)
	}

	override fun delete() {
		reset()
		VK10.vkDestroyFence(device.device, handle, null)
	}
}