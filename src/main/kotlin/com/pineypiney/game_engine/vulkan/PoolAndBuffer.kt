package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanDescriptorSet
import com.pineypiney.game_engine.resources.shaders.vulkan.pipeline.VulkanPipeline
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.window.Viewport
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import kool.free
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import java.nio.ByteBuffer

class PoolAndBuffer(val pool: Long, val buffer: VkCommandBuffer, val deletion: DeletionQueue) : Deletable {

	val currentOps = Vec4i()

	fun begin(info: VkCommandBufferBeginInfo) {
		VkUtil.processResult(VK10.vkBeginCommandBuffer(buffer, info), "Failed to begin Command Buffer")

		// For error https://vulkan.lunarg.com/doc/view/1.3.296.0/windows/1.3-extensions/vkspec.html#VUID-vkCmdDrawIndexed-None-07847
		setStencil(false)
	}

	fun begin(flags: Int) {
		MemoryStack.stackPush().use { stack ->
			val info = VkCommandBufferBeginInfo.calloc(stack)
				.`sType$Default`()
				.flags(flags)
			begin(info)
		}
	}

	fun beginRendering(info: VkRenderingInfo) {
		VK13.vkCmdBeginRendering(buffer, info)
	}

	fun setViewport(stack: MemoryStack, viewport: Viewport) {
		VK10.vkCmdSetViewport(buffer, 0, VkStructs.createViewports(stack, listOf(viewport)))
	}

	fun setViewport(viewport: Viewport) {
		MemoryStack.stackPush().use { stack -> setViewport(stack, viewport) }
	}

	fun setStencil(enabled: Boolean) {
		VK13.vkCmdSetStencilTestEnable(buffer, enabled)
	}

	/**
	 * Set up stencilling for render pipelines
	 *
	 * @param reference Stencil reference value
	 * @param mask Stencil compare mask
	 * @param failOp Stencil Operation performed when the [compare] operation fails
	 * @param passOp Stencil Operation performed when the [compare] operation and depth check both pass
	 * @param depthFailOp Stencil Operation performed when the [compare] operation passes but the depth check fails
	 * @param compare Compare operation performed on current stencil value and [reference]
	 */
	fun setStencil(faceMask: Int, reference: Int, mask: Int, failOp: Int, passOp: Int, depthFailOp: Int, compare: Int) {
		VK13.vkCmdSetStencilReference(buffer, faceMask, reference)
		VK13.vkCmdSetStencilCompareMask(buffer, faceMask, mask)
		VK13.vkCmdSetStencilOp(buffer, faceMask, failOp, passOp, depthFailOp, compare)
		currentOps(failOp, passOp, depthFailOp, compare)
	}

	fun setStencilComparison(faceMask: Int, reference: Int, mask: Int, compare: Int) {
		VK13.vkCmdSetStencilReference(buffer, faceMask, reference)
		VK13.vkCmdSetStencilCompareMask(buffer, faceMask, mask)
		VK13.vkCmdSetStencilOp(buffer, faceMask, currentOps.x, currentOps.y, currentOps.z, compare)
		currentOps.w = compare
	}

	fun setStencilOperations(faceMask: Int, failOp: Int, passOp: Int, depthFailOp: Int) {
		VK13.vkCmdSetStencilOp(buffer, faceMask, failOp, passOp, depthFailOp, currentOps.w)
		currentOps.x = failOp
		currentOps.y = passOp
		currentOps.z = depthFailOp
	}

	fun setStencilWriteMask(mask: Int) {
		VK13.vkCmdSetStencilWriteMask(buffer, 3, mask)
	}

	fun setScissors(stack: MemoryStack, viewport: Viewport) {
		VK10.vkCmdSetScissor(buffer, 0, VkStructs.createScissors(stack, listOf(viewport)))
	}

	fun setScissors(viewport: Viewport) {
		MemoryStack.stackPush().use { stack -> setScissors(stack, viewport) }
	}

	fun bindPipeline(pipeline: VulkanPipeline) {
		VK10.vkCmdBindPipeline(buffer, pipeline.getBindPoint(), pipeline.pipeline)
	}

	fun bindIndices(buffer: Long, offset: Long, type: Int) {
		VK10.vkCmdBindIndexBuffer(this.buffer, buffer, offset, type)
	}

	fun bindDescriptorSet(pipeline: VulkanPipeline, set: VulkanDescriptorSet) {
		val buf = MemoryUtil.memAllocLong(1).put(set.handle).flip()
		VK10.vkCmdBindDescriptorSets(buffer, pipeline.getBindPoint(), pipeline.layout.handle, 0, buf, null)
		buf.free()
	}

	fun bindDescriptorSets(pipeline: VulkanPipeline, sets: Iterable<VulkanDescriptorSet>) {
		val buf = MemoryUtil.memAllocLong(sets.count())
		for (set in sets) buf.put(set.handle)
		VK10.vkCmdBindDescriptorSets(buffer, pipeline.getBindPoint(), pipeline.layout.handle, 0, buf.flip(), null)
		buf.free()
	}

	fun pushConstants(pipeline: VulkanPipeline, stage: Int, offset: Int, constants: ByteBuffer) {
		VK10.vkCmdPushConstants(buffer, pipeline.layout.handle, stage, offset, constants)
	}

	fun draw(vertexCount: Int, instanceCount: Int = 1, firstVertex: Int = 0, firstInstance: Int = 0) {
		VK10.vkCmdDraw(buffer, vertexCount, instanceCount, firstVertex, firstInstance)
	}

	fun drawIndexed(indexCount: Int, instanceCount: Int = 1, firstIndex: Int = 0, vertexOffset: Int = 0, firstInstance: Int = 0) {
		VK10.vkCmdDrawIndexed(buffer, indexCount, instanceCount, firstIndex, vertexOffset, firstInstance)
	}

	fun endRendering() {
		VK13.vkCmdEndRendering(buffer)
	}

	fun clearColourImage(stack: MemoryStack, colour: Vec4, image: VulkanImage) {
		val colour = VkStructs.clearColour(stack, colour)
		val clearRange = VkStructs.createImageRange(stack, VK10.VK_IMAGE_ASPECT_COLOR_BIT)
		VK10.vkCmdClearColorImage(buffer, image.image, VK10.VK_IMAGE_LAYOUT_GENERAL, colour, clearRange)
	}

	fun dispatch(x: Int = 1, y: Int = 1, z: Int = 1) {
		VK10.vkCmdDispatch(buffer, x, y, z)
	}

	fun dispatch(size: Vec3i) {
		dispatch(size.x, size.y, size.z)
	}

	fun copyBuffer(src: Long, dst: Long, regions: VkBufferCopy.Buffer) {
		VK10.vkCmdCopyBuffer(buffer, src, dst, regions)
	}

	fun copyBuffer(src: Long, dst: Long, srcOffset: Long, dstOffset: Long, size: Long) {
		val regions = VkBufferCopy.calloc(1)
			.srcOffset(srcOffset)
			.dstOffset(dstOffset)
			.size(size)
		VK10.vkCmdCopyBuffer(buffer, src, dst, regions)
	}

	fun copyBufferToImage(src: Long, dst: Long, layout: Int, regions: VkBufferImageCopy.Buffer) {
		VK10.vkCmdCopyBufferToImage(buffer, src, dst, layout, regions)
	}

	fun copyBufferToImage(src: VmaBuffer, image: VulkanImage, regions: VkBufferImageCopy.Buffer) {
		VK10.vkCmdCopyBufferToImage(buffer, src.buffer, image.image, image.layout, regions)
	}

	fun copyImageToBuffer(src: VmaBuffer, image: VulkanImage, regions: VkBufferImageCopy.Buffer) {
		VK10.vkCmdCopyImageToBuffer(buffer, image.image, image.layout, src.buffer, regions)
	}

	fun end() {
		VK10.vkEndCommandBuffer(buffer)
	}

	fun execute(func: (cmd: PoolAndBuffer) -> Unit) {
		resetBuffer(0)
		begin(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
		func(this)
		end()
	}

	fun <E> execute(func: (cmd: PoolAndBuffer) -> E): E {
		resetBuffer(0)
		begin(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
		val ret = func(this)
		end()
		return ret
	}

	fun resetBuffer(flags: Int = 0) {
		VK10.vkResetCommandBuffer(buffer, flags)
		deletion.flush()
	}

	override fun delete() {
		deletion.flush()
		VK10.vkDestroyCommandPool(buffer.device, pool, null)
	}

	companion object {
		fun create(device: VulkanDevice, stack: MemoryStack, name: String): PoolAndBuffer {
			val pool = device.createCommandPool(stack, name)
			val buffer = device.createCommandBuffer(stack, pool, name)
			val deletion = DeletionQueue()
			return PoolAndBuffer(pool, buffer, deletion)
		}
	}
}