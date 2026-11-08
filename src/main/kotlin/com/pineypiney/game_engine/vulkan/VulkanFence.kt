package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.vulkan.VK10

class VulkanFence(val device: VulkanDevice, val handle: Long) : Deletable {

	fun wait(timeout: Number): Boolean {
		val res = VK10.vkWaitForFences(device.device, handle, true, timeout.toLong())
		if (res == VK10.VK_SUCCESS) return true
		else if (res == VK10.VK_TIMEOUT) return false
		VkUtil.processResult(res, "Failed to wait for Vulkan Fence")
		return false
	}

	fun wait(timeout: Double) = wait(timeout.toLong())

	fun reset(): Int {
		return VK10.vkResetFences(device.device, handle)
	}

	override fun delete() {
		VK10.vkDestroyFence(device.device, handle, null)
	}
}