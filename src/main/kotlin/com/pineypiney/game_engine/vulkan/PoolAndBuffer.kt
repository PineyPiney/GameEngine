package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.shaders.vulkan.pipeline.VulkanPipeline
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.window.Viewport
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import kool.free
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import java.nio.ByteBuffer

class PoolAndBuffer(val pool: Long, val buffer: VkCommandBuffer, val deletion: DeletionQueue) : Deletable {

	fun begin(info: VkCommandBufferBeginInfo) {
		VkUtil.processError(VK10.vkBeginCommandBuffer(buffer, info), "Failed to begin Command Buffer")
	}

	fun begin(flags: Int) {
		val info = VkCommandBufferBeginInfo.calloc()
			.`sType$Default`()
			.flags(flags)
		begin(info)
		info.free()
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

	fun pushConstants(pipeline: VulkanPipeline, stage: Int, constants: ByteBuffer) {
		VK10.vkCmdPushConstants(buffer, pipeline.layout.handle, stage, 0, constants)
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

	fun immediateSubmit(func: (cmd: PoolAndBuffer) -> Unit) {
		resetBuffer(0)
		begin(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
		func(this)
		end()
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
		fun create(device: VulkanDevice, stack: MemoryStack): PoolAndBuffer {
			MemoryStack.stackPush().use { stack ->
				val pool = device.createCommandPool(stack)
				val buffer = device.createCommandBuffer(stack, pool)
				val deletion = DeletionQueue()
				return PoolAndBuffer(pool, buffer, deletion)
			}

		}
	}
}