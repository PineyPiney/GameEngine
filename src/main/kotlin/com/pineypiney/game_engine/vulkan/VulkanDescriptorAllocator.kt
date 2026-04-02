package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo
import org.lwjgl.vulkan.VkDescriptorPoolSize
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo
import java.nio.LongBuffer

class VulkanDescriptorAllocator(val device: VulkanDevice) : Deletable {

	val poolBuffer = MemoryUtil.memCallocLong(1)
	val pool get() = poolBuffer[0]

	fun initPool(maxSets: Int, ratios: Set<PoolSizeRatio>) {
		val sizes = VkDescriptorPoolSize.create(ratios.size)
		for (ratio in ratios) {
			sizes.put(
				VkDescriptorPoolSize.calloc()
					.type(ratio.type)
					.descriptorCount((ratio.ratio * maxSets).toInt())
			)
		}
		val poolCreateInfo = VkDescriptorPoolCreateInfo.calloc()
			.`sType$Default`()
			.flags(0)
			.maxSets(maxSets)
			.pPoolSizes(sizes.flip())

		delete()
		VK10.vkCreateDescriptorPool(device.device, poolCreateInfo, null, poolBuffer)
	}

	fun clearDescriptors() {
		VK10.vkResetDescriptorPool(device.device, pool, 0)
	}

	fun allocate(layout: LongBuffer): Long {
		val allocateInfo = VkDescriptorSetAllocateInfo.calloc()
			.`sType$Default`()
			.descriptorPool(pool)
			.pSetLayouts(layout)

		val buffer = MemoryUtil.memAllocLong(1)
		val err = VK10.vkAllocateDescriptorSets(device.device, allocateInfo, buffer)
		allocateInfo.free()
		val descriptorSet = buffer[0]
		buffer.free()
		VkUtil.processError(err, "Failed to allocate descriptor set")
		return descriptorSet
	}

	fun createDescriptorLayout(): Long {
		val poolSizes = setOf(PoolSizeRatio(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE, 1f))
		initPool(10, poolSizes)

		val builder = VulkanDescriptorLayoutBuilder()
		builder.addBinding(0, VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
		val layout = builder.build(device, VK10.VK_SHADER_STAGE_COMPUTE_BIT)
		builder.clear()
		return layout
	}

	override fun delete() {
		VK10.vkDestroyDescriptorPool(device.device, pool, null)
	}

	data class PoolSizeRatio(val type: Int, val ratio: Float)
}