package com.pineypiney.game_engine.vulkan

import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*

object VkStructs {

	fun createImageInfo(imageType: Int, format: Int, extent: VkExtent3D, samples: Int, tiling: Int, usage: Int): VkImageCreateInfo {
		return VkImageCreateInfo.calloc()
			.`sType$Default`()
			.imageType(imageType)
			.format(format)
			.extent(extent)
			.mipLevels(1)
			.arrayLayers(1)
			.samples(samples)
			.tiling(tiling)
			.usage(usage)
	}

	fun createImageViewInfo(viewType: Int, image: Long, format: Int, imageRange: VkImageSubresourceRange): VkImageViewCreateInfo {
		return VkImageViewCreateInfo.calloc()
			.`sType$Default`()
			.viewType(viewType)
			.image(image)
			.format(format)
			.subresourceRange(imageRange)
	}

	fun createImageRange(
		aspectMask: Int,
		baseLevel: Int = 0,
		levelCount: Int = VK10.VK_REMAINING_MIP_LEVELS,
		baseLayer: Int = 0,
		layerCount: Int = VK10.VK_REMAINING_ARRAY_LAYERS
	): VkImageSubresourceRange {
		return VkImageSubresourceRange.calloc().set(aspectMask, baseLevel, levelCount, baseLayer, layerCount)

	}

	fun createImageLayers(aspectMask: Int, level: Int = 0, baseLayer: Int = 0, layers: Int = VK10.VK_REMAINING_ARRAY_LAYERS): VkImageSubresourceLayers {
		return VkImageSubresourceLayers.calloc().set(aspectMask, level, baseLayer, layers)

	}

	fun createBufferSubmits(cmd: VkCommandBuffer, deviceMask: Int): VkCommandBufferSubmitInfo.Buffer {
		return VkCommandBufferSubmitInfo.calloc(1)
			.`sType$Default`()
			.commandBuffer(cmd)
			.deviceMask(deviceMask)
	}

	fun createSemaphoreSubmits(semaphore: Long, stageMask: Long, deviceIndex: Int, value: Long): VkSemaphoreSubmitInfo.Buffer {
		return VkSemaphoreSubmitInfo.calloc(1)
			.`sType$Default`()
			.semaphore(semaphore)
			.stageMask(stageMask)
			.deviceIndex(deviceIndex)
			.value(value)
	}

	fun createSubmitInfo(
		cmd: PoolAndBuffer,
		signalSemaphore: VulkanSemaphoreHandler?,
		waitSemaphore: VulkanSemaphoreHandler?,
		waitMask: Int = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT
	): VkSubmitInfo {
		val buffer = MemoryUtil.memAllocPointer(1)
		buffer.put(cmd.buffer)
		val maskBuffer = MemoryUtil.memAllocInt(1)
		maskBuffer.put(waitMask)
		return VkSubmitInfo.calloc()
			.`sType$Default`()
			.pCommandBuffers(buffer)
			.pSignalSemaphores(signalSemaphore?.buffer)
			.pWaitDstStageMask(maskBuffer)
			.pWaitSemaphores(waitSemaphore?.buffer)
	}

	fun createSubmitInfo2s(cmd: VkCommandBufferSubmitInfo.Buffer, signalSemaphore: VkSemaphoreSubmitInfo.Buffer?, waitSemaphore: VkSemaphoreSubmitInfo.Buffer?): VkSubmitInfo2.Buffer {
		return VkSubmitInfo2.calloc(1)
			.`sType$Default`()
			.pCommandBufferInfos(cmd)
			.pSignalSemaphoreInfos(signalSemaphore)
			.pWaitSemaphoreInfos(waitSemaphore)
	}

	fun createPresentInfo(swapchain: VulkanSwapchainHandler, waitSemaphore: VulkanSemaphoreHandler?): VkPresentInfoKHR {
		return VkPresentInfoKHR.calloc()
			.`sType$Default`()
			.pSwapchains(swapchain.buffer)
			.swapchainCount(1)
			.pWaitSemaphores(waitSemaphore?.buffer)
			.swapchainCount(1)
			.pImageIndices(swapchain.imageIndices)
	}
}