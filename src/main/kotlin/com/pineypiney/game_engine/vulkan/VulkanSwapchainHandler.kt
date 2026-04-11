package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.extension_functions.delete
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.KHRSwapchain
import java.nio.LongBuffer

class VulkanSwapchainHandler(val device: VulkanDevice, val buffer: LongBuffer, val images: List<VulkanSwapchainImage>) : Deletable {

	val handle get() = buffer[0]

	val pImageIndex = MemoryUtil.memCallocInt(1)
	val imageIndex get() = pImageIndex[0]

	fun acquireNextImage(timeout: Long, semaphore: VulkanSemaphoreHandler, fence: VulkanFence?): VulkanSwapchainImage? {
		val err = KHRSwapchain.vkAcquireNextImageKHR(device.device, handle, timeout, semaphore.handle, fence?.handle ?: 0L, pImageIndex)
		if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || err == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
			return null
		}
		if (VkUtil.isError(err)) {
			pImageIndex.put(0, 0)
		}
		val image = images[imageIndex]
		return image
	}

	override fun delete() {
		KHRSwapchain.vkDestroySwapchainKHR(device.device, handle, null)
		images.delete()
		buffer.free()
		pImageIndex.free()
	}
}