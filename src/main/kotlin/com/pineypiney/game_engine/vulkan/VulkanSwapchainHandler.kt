package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deleteable
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.KHRSwapchain
import org.lwjgl.vulkan.VK10
import java.nio.LongBuffer

class VulkanSwapchainHandler(val device: VulkanDevice, val buffer: LongBuffer, val images: LongArray, val imageViews: LongArray) : Deleteable {

	val handle get() = buffer[0]

	val imageIndices = MemoryUtil.memAllocInt(1)
	val imageIndex get() = imageIndices[0]

	fun acquireNextImage(timeout: Long, semaphore: Long, fence: VulkanFence?): Long {
		if (VkUtil.isError(KHRSwapchain.vkAcquireNextImageKHR(device.device, handle, timeout, semaphore, fence?.handle ?: 0L, imageIndices))) return 0L
		val image = images[imageIndex]
		return image
	}

	override fun delete() {
		KHRSwapchain.vkDestroySwapchainKHR(device.device, handle, null)
		for (view in imageViews) {
			VK10.vkDestroyImageView(device.device, view, null)
		}
		buffer.free()
		imageIndices.free()
	}
}