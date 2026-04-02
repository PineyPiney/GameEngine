package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.KHRSwapchain
import java.nio.LongBuffer

class VulkanSwapchainHandler(val device: VulkanDevice, val buffer: LongBuffer, val images: List<VulkanSwapchainImage>) : Deletable {

	val handle get() = buffer[0]

	val imageIndices = MemoryUtil.memAllocInt(1)
	val imageIndex get() = imageIndices[0]

	fun acquireNextImage(timeout: Long, semaphore: Long, fence: VulkanFence?): VulkanSwapchainImage {
		if (VkUtil.isError(KHRSwapchain.vkAcquireNextImageKHR(device.device, handle, timeout, semaphore, fence?.handle ?: 0L, imageIndices))) return images.first()
		val image = images[imageIndex]
		return image
	}

	override fun delete() {
		KHRSwapchain.vkDestroySwapchainKHR(device.device, handle, null)
		for (image in images) {
			image.delete()
		}
		buffer.free()
		imageIndices.free()
	}
}