package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.window.Viewport
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*

object VkStructs {

	fun rect(offset: Vec2i, size: Vec2i): VkRect2D {
		val res = VkRect2D.calloc()
		res.offset().set(offset)
		res.extent().set(size)
		return res
	}

	fun clearColour(colour: Vec4): VkClearColorValue {
		return VkClearColorValue.calloc()
			.float32(0, colour.x)
			.float32(1, colour.y)
			.float32(2, colour.z)
			.float32(3, colour.w)
	}

	fun clearDepthStencil(depth: Float, stencil: Int): VkClearDepthStencilValue {
		return VkClearDepthStencilValue.calloc().set(depth, stencil)
	}

	fun clear(colour: Vec4, depth: Float = 0f, stencil: Int = 0): VkClearValue {
		val clearValue = VkClearValue.calloc()
			.color(clearColour(colour))
		clearValue.depthStencil().set(depth, stencil)
		return clearValue
	}

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

	fun createAttachmentInfo(image: VulkanImageI, clear: VkClearValue?): VkRenderingAttachmentInfo {
		val res = VkRenderingAttachmentInfo.calloc()
			.`sType$Default`()
			.imageView(image.imageView)
			.imageLayout(image.layout)
			.loadOp(if (clear != null) VK10.VK_ATTACHMENT_LOAD_OP_CLEAR else VK10.VK_ATTACHMENT_LOAD_OP_LOAD)
			.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
		if (clear != null) {
			res.clearValue(clear)
		}
		return res
	}

	fun createAttachmentInfo(image: VulkanImageI, colour: Vec4, depth: Float = 0f, stencil: Int = 0) {
		val clearValue = clear(colour, depth, stencil)
		createAttachmentInfo(image, clearValue)
	}

	fun createAttachmentInfos(image: VulkanImageI, clear: VkClearValue?): VkRenderingAttachmentInfo.Buffer {
		val res = VkRenderingAttachmentInfo.calloc(1)
			.`sType$Default`()
			.imageView(image.imageView)
			.imageLayout(image.layout)
			.loadOp(if (clear != null) VK10.VK_ATTACHMENT_LOAD_OP_CLEAR else VK10.VK_ATTACHMENT_LOAD_OP_LOAD)
			.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
		if (clear != null) {
			res.clearValue(clear)
		}
		return res
	}

	fun createAttachmentInfos(image: VulkanImageI, colour: Vec4, depth: Float, stencil: Int = 0): VkRenderingAttachmentInfo.Buffer {
		val clearValue = VkClearValue.calloc()
			.color(clearColour(colour))
		clearValue.depthStencil().set(depth, stencil)

		return createAttachmentInfos(image, clearValue)
	}

	fun createRenderingInfo(
		size: Vec2i,
		colourAttachments: VkRenderingAttachmentInfo.Buffer,
		depthAttachment: VkRenderingAttachmentInfo? = null,
		stencilAttachment: VkRenderingAttachmentInfo? = null
	): VkRenderingInfo {
		return VkRenderingInfo.calloc()
			.`sType$Default`()
			.renderArea(rect(Vec2i(0), size))
			.layerCount(1)
			.pColorAttachments(colourAttachments)
			.pDepthAttachment(depthAttachment)
			.pStencilAttachment(stencilAttachment)
	}

	fun createViewport(viewport: Viewport): VkViewport {
		return VkViewport.calloc()
			.set(
				viewport.bl.x.toFloat(), viewport.bl.y.toFloat(),
				viewport.size.x.toFloat(), viewport.size.y.toFloat(),
				0f, 1f
			)
	}

	fun createViewports(viewports: Iterable<Viewport>): VkViewport.Buffer {
		val res = VkViewport.calloc(viewports.count())
		for (viewport in viewports) {
			res.get().set(
				viewport.bl.x.toFloat(), viewport.bl.y.toFloat(),
				viewport.size.x.toFloat(), viewport.size.y.toFloat(),
				0f, 1f
			)
		}
		return res.flip()
	}

	fun createScissors(viewports: Iterable<Viewport>): VkRect2D.Buffer {
		val res = VkRect2D.calloc(viewports.count())
		for (viewport in viewports) {
			res.put(rect(viewport.bl, viewport.size))
		}
		return res.flip()
	}

	fun createBufferSubmits(cmd: VkCommandBuffer, deviceMask: Int): VkCommandBufferSubmitInfo.Buffer {
		return VkCommandBufferSubmitInfo.calloc(1)
			.`sType$Default`()
			.commandBuffer(cmd)
			.deviceMask(deviceMask)
	}

	fun createSemaphoreSubmits(semaphore: VulkanSemaphoreHandler, stageMask: Long, deviceIndex: Int, value: Long): VkSemaphoreSubmitInfo.Buffer {
		return VkSemaphoreSubmitInfo.calloc(1)
			.`sType$Default`()
			.semaphore(semaphore.handle)
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
			.pImageIndices(swapchain.pImageIndex)
	}
}