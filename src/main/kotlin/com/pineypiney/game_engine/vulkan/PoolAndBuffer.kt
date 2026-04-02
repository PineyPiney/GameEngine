package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkClearColorValue
import org.lwjgl.vulkan.VkCommandBuffer
import org.lwjgl.vulkan.VkCommandBufferBeginInfo
import java.nio.ByteBuffer

class PoolAndBuffer(val pool: Long, val buffer: VkCommandBuffer) : Deletable {

	fun begin(info: VkCommandBufferBeginInfo) {
		VK10.vkBeginCommandBuffer(buffer, info)
	}

	fun bindPipeline(pipeline: VulkanComputePipeline) {
		VK10.vkCmdBindPipeline(buffer, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, pipeline.pipeline)
	}

	fun bindDescriptorSets(vulkan: VulkanManager, pipeline: VulkanComputePipeline) {
		val buf = MemoryUtil.memAllocLong(1).put(vulkan.descriptorSet).flip()
		VK10.vkCmdBindDescriptorSets(buffer, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, pipeline.layout, 0, buf, null)
		buf.free()
	}

	fun pushConstants(pipeline: VulkanComputePipeline, constants: ByteBuffer) {
		VK10.vkCmdPushConstants(buffer, pipeline.layout, VK10.VK_SHADER_STAGE_COMPUTE_BIT, 0, constants)
	}

	fun clearColourImage(colour: Vec4, image: VulkanImageI) {
		val colour = VkClearColorValue.calloc()
			.float32(0, colour.x)
			.float32(1, colour.y)
			.float32(2, colour.z)
			.float32(3, colour.w)
		val clearRange = VkStructs.createImageRange(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
		VK10.vkCmdClearColorImage(buffer, image.image, VK10.VK_IMAGE_LAYOUT_GENERAL, colour, clearRange)
	}

	fun dispatch(size: Vec3i) {
		VK10.vkCmdDispatch(buffer, size.x, size.y, size.z)
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