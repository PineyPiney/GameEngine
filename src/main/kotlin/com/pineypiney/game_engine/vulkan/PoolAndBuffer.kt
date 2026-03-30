package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deleteable
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkCommandBuffer
import org.lwjgl.vulkan.VkCommandBufferBeginInfo

class PoolAndBuffer(val pool: Long, val buffer: VkCommandBuffer) : Deleteable {

	fun begin(info: VkCommandBufferBeginInfo) {
		VK10.vkBeginCommandBuffer(buffer, info)
	}

	fun end() {
		VK10.vkEndCommandBuffer(buffer)
	}

	fun resetBuffer(flags: Int = 0) {
		VK10.vkResetCommandBuffer(buffer, flags)
	}

	override fun delete() {
		VK10.vkDestroyCommandPool(buffer.device, pool, null)
	}

	companion object {
		fun create(device: VulkanDevice): PoolAndBuffer {
			val pool = VkUtil.createCommandPool(device)
			val buffer = VkUtil.createCommandBuffer(device, pool)
			return PoolAndBuffer(pool, buffer)
		}
	}
}