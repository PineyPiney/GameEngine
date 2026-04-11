package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.vulkan.pipeline.VulkanComputePipeline
import com.pineypiney.game_engine.vulkan.pipeline.VulkanPipeline
import com.pineypiney.game_engine.window.Viewport
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import java.nio.ByteBuffer

class PoolAndBuffer(val pool: Long, val buffer: VkCommandBuffer) : Deletable {

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

	fun setViewport(viewport: Viewport) {
		VK10.vkCmdSetViewport(buffer, 0, VkStructs.createViewports(listOf(viewport)))
	}

	fun setScissors(viewport: Viewport) {
		VK10.vkCmdSetScissor(buffer, 0, VkStructs.createScissors(listOf(viewport)))
	}

	fun bindPipeline(pipeline: VulkanPipeline) {
		VK10.vkCmdBindPipeline(buffer, pipeline.getBindPoint(), pipeline.pipeline)
	}

	fun bindIndices(buffer: Long, offset: Long, type: Int) {
		VK10.vkCmdBindIndexBuffer(this.buffer, buffer, offset, type)
	}

	fun bindDescriptorSets(vulkan: VulkanManager, pipeline: VulkanComputePipeline) {
		val buf = MemoryUtil.memAllocLong(1).put(vulkan.descriptorSet).flip()
		VK10.vkCmdBindDescriptorSets(buffer, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, pipeline.layout, 0, buf, null)
		buf.free()
	}

	fun pushConstants(pipeline: VulkanPipeline, stage: Int, constants: ByteBuffer) {
		VK10.vkCmdPushConstants(buffer, pipeline.layout, stage, 0, constants)
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

	fun clearColourImage(colour: Vec4, image: VulkanImageI) {
		val colour = VkStructs.clearColour(colour)
		val clearRange = VkStructs.createImageRange(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
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