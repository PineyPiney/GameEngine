package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanShaderModule
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import com.pineypiney.game_engine.window.Viewport
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*

object VkStructs {

	fun rect(stack: MemoryStack, offset: Vec2i, size: Vec2i): VkRect2D {
		val res = VkRect2D.calloc(stack)
		res.offset().set(offset)
		res.extent().set(size)
		return res
	}

	fun clearColour(stack: MemoryStack, colour: Vec4): VkClearColorValue {
		return VkClearColorValue.calloc(stack)
			.float32(0, colour.x)
			.float32(1, colour.y)
			.float32(2, colour.z)
			.float32(3, colour.w)
	}

	fun clearDepthStencil(stack: MemoryStack, depth: Float, stencil: Int): VkClearDepthStencilValue {
		return VkClearDepthStencilValue.calloc(stack).set(depth, stencil)
	}

	fun clear(stack: MemoryStack, colour: Vec4, depth: Float = 0f, stencil: Int = 0): VkClearValue {
		val clearValue = VkClearValue.calloc(stack)
			.color(clearColour(stack, colour))
		clearValue.depthStencil().set(depth, stencil)
		return clearValue
	}

	fun createCommandPoolInfo(stack: MemoryStack, familyIndex: Int, flags: Int): VkCommandPoolCreateInfo {
		return VkCommandPoolCreateInfo.calloc(stack)
			.`sType$Default`()
			.queueFamilyIndex(familyIndex)
			.flags(flags)
	}

	fun createCommandBufferInfo(stack: MemoryStack, commandPool: Long): VkCommandBufferAllocateInfo {
		return VkCommandBufferAllocateInfo.calloc(stack)
			.`sType$Default`()
			.commandPool(commandPool)
			.level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
			.commandBufferCount(1)

	}

	fun createFenceInfo(stack: MemoryStack, flags: Int): VkFenceCreateInfo {
		return VkFenceCreateInfo.calloc(stack)
			.`sType$Default`()
			.flags(flags)
	}

	fun createSemaphoreInfo(stack: MemoryStack, flags: Int): VkSemaphoreCreateInfo {
		return VkSemaphoreCreateInfo.calloc(stack)
			.`sType$Default`()
			.flags(flags)
	}

	fun createSamplerInfo(stack: MemoryStack, magFilter: Int, minFilter: Int): VkSamplerCreateInfo {
		return VkSamplerCreateInfo.calloc(stack)
			.`sType$Default`()
			.magFilter(magFilter)
			.minFilter(minFilter)
	}

	fun createImageInfo(stack: MemoryStack, imageType: Int, format: Int, extent: VkExtent3D, samples: Int, tiling: Int, usage: Int, flags: Int): VkImageCreateInfo {
		return VkImageCreateInfo.calloc(stack)
			.`sType$Default`()
			.imageType(imageType)
			.format(format)
			.extent(extent)
			.mipLevels(1)
			.arrayLayers(1)
			.samples(samples)
			.tiling(tiling)
			.usage(usage)
			.flags(flags)
	}

	fun createImageViewInfo(stack: MemoryStack, viewType: Int, image: Long, format: Int, imageRange: VkImageSubresourceRange): VkImageViewCreateInfo {
		return VkImageViewCreateInfo.calloc(stack)
			.`sType$Default`()
			.viewType(viewType)
			.image(image)
			.format(format)
			.subresourceRange(imageRange)
	}

	fun createImageRange(
		stack: MemoryStack,
		aspectMask: Int,
		baseLevel: Int = 0,
		levelCount: Int = VK10.VK_REMAINING_MIP_LEVELS,
		baseLayer: Int = 0,
		layerCount: Int = VK10.VK_REMAINING_ARRAY_LAYERS
	): VkImageSubresourceRange {
		return VkImageSubresourceRange.calloc(stack).set(aspectMask, baseLevel, levelCount, baseLayer, layerCount)

	}

	fun createImageLayers(stack: MemoryStack, aspectMask: Int, level: Int = 0, baseLayer: Int = 0, layers: Int = VK10.VK_REMAINING_ARRAY_LAYERS): VkImageSubresourceLayers {
		return VkImageSubresourceLayers.calloc(stack).set(aspectMask, level, baseLayer, layers)

	}

	fun createAttachmentInfo(stack: MemoryStack, image: VulkanImage, clear: VkClearValue?): VkRenderingAttachmentInfo {
		val res = VkRenderingAttachmentInfo.calloc(stack)
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

	fun createAttachmentInfo(stack: MemoryStack, image: VulkanImage, colour: Vec4, depth: Float = 0f, stencil: Int = 0) {
		val clearValue = clear(stack, colour, depth, stencil)
		createAttachmentInfo(stack, image, clearValue)
	}

	fun createAttachmentInfos(stack: MemoryStack, image: VulkanImage, clear: VkClearValue?): VkRenderingAttachmentInfo.Buffer {
		val res = VkRenderingAttachmentInfo.calloc(1, stack)
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

	fun createAttachmentInfos(stack: MemoryStack, image: VulkanImage, colour: Vec4, depth: Float, stencil: Int = 0): VkRenderingAttachmentInfo.Buffer {
		val clearValue = VkClearValue.calloc(stack)
			.color(clearColour(stack, colour))
		clearValue.depthStencil().set(depth, stencil)

		return createAttachmentInfos(stack, image, clearValue)
	}

	fun createStageInfo(stack: MemoryStack, module: VulkanShaderModule): VkPipelineShaderStageCreateInfo {
		return VkPipelineShaderStageCreateInfo.calloc(stack)
			.`sType$Default`()
			.stage(module.stage.vulkan)
			.module(module.handle)
			.pName(stack.UTF8("main"))
	}

	fun createRenderingInfo(
		stack: MemoryStack,
		size: Vec2i,
		colourAttachments: VkRenderingAttachmentInfo.Buffer,
		depthAttachment: VkRenderingAttachmentInfo? = null,
		stencilAttachment: VkRenderingAttachmentInfo? = null
	): VkRenderingInfo {
		return VkRenderingInfo.calloc(stack)
			.`sType$Default`()
			.renderArea(rect(stack, Vec2i(0), size))
			.layerCount(1)
			.pColorAttachments(colourAttachments)
			.pDepthAttachment(depthAttachment)
			.pStencilAttachment(stencilAttachment)
	}

	fun createViewport(stack: MemoryStack, viewport: Viewport): VkViewport {
		return VkViewport.calloc(stack)
			.set(
				viewport.bl.x.toFloat(), viewport.bl.y.toFloat(),
				viewport.size.x.toFloat(), viewport.size.y.toFloat(),
				0f, 1f
			)
	}

	fun createViewports(stack: MemoryStack, viewports: Iterable<Viewport>): VkViewport.Buffer {
		val res = VkViewport.calloc(viewports.count(), stack)
		for (viewport in viewports) {
			res.get().set(
				viewport.bl.x.toFloat(), viewport.bl.y.toFloat(),
				viewport.size.x.toFloat(), viewport.size.y.toFloat(),
				0f, 1f
			)
		}
		return res.flip()
	}

	fun createScissors(stack: MemoryStack, viewports: Iterable<Viewport>): VkRect2D.Buffer {
		val res = VkRect2D.calloc(viewports.count())
		for (viewport in viewports) {
			res.put(rect(stack, viewport.bl, viewport.size))
		}
		return res.flip()
	}

	fun createBufferSubmits(stack: MemoryStack, cmd: VkCommandBuffer, deviceMask: Int): VkCommandBufferSubmitInfo.Buffer {
		return VkCommandBufferSubmitInfo.calloc(1, stack)
			.`sType$Default`()
			.commandBuffer(cmd)
			.deviceMask(deviceMask)
	}

	fun createSemaphoreSubmits(stack: MemoryStack, semaphore: VulkanSemaphoreHandler, stageMask: Long, deviceIndex: Int, value: Long): VkSemaphoreSubmitInfo.Buffer {
		return VkSemaphoreSubmitInfo.calloc(1, stack)
			.`sType$Default`()
			.semaphore(semaphore.handle)
			.stageMask(stageMask)
			.deviceIndex(deviceIndex)
			.value(value)
	}

	fun createSubmitInfo(
		stack: MemoryStack,
		cmd: PoolAndBuffer,
		signalSemaphore: VulkanSemaphoreHandler?,
		waitSemaphore: VulkanSemaphoreHandler?,
		waitMask: Int = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT
	): VkSubmitInfo {
		val buffer = stack.mallocPointer(1).put(0, cmd.buffer)
		val maskBuffer = stack.mallocInt(1).put(0, waitMask)
		return VkSubmitInfo.calloc(stack)
			.`sType$Default`()
			.pCommandBuffers(buffer)
			.pSignalSemaphores(signalSemaphore?.buffer)
			.pWaitDstStageMask(maskBuffer)
			.pWaitSemaphores(waitSemaphore?.buffer)
	}

	fun createSubmitInfo2s(
		stack: MemoryStack,
		cmd: VkCommandBufferSubmitInfo.Buffer,
		signalSemaphore: VkSemaphoreSubmitInfo.Buffer?,
		waitSemaphore: VkSemaphoreSubmitInfo.Buffer?
	): VkSubmitInfo2.Buffer {
		return VkSubmitInfo2.calloc(1, stack)
			.`sType$Default`()
			.pCommandBufferInfos(cmd)
			.pSignalSemaphoreInfos(signalSemaphore)
			.pWaitSemaphoreInfos(waitSemaphore)
	}

	fun createPresentInfo(stack: MemoryStack, swapchain: VulkanSwapchainHandler, waitSemaphore: VulkanSemaphoreHandler?): VkPresentInfoKHR {
		return VkPresentInfoKHR.calloc(stack)
			.`sType$Default`()
			.pSwapchains(swapchain.buffer)
			.swapchainCount(1)
			.pWaitSemaphores(waitSemaphore?.buffer)
			.swapchainCount(1)
			.pImageIndices(swapchain.pImageIndex)
	}
}