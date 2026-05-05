package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo
import org.lwjgl.vulkan.VkDescriptorPoolSize

abstract class VulkanDescriptorAllocator(val device: VulkanDevice) : Deletable {

	abstract fun init(maxSets: Int, ratios: Map<Int, Float>)

	abstract fun allocate(layout: VulkanDescriptorLayout): Long

	abstract fun clearPools()

	fun createPool(maxSets: Int, ratios: Map<Int, Float>): Long {
		val sizes = VkDescriptorPoolSize.create(ratios.count())
		for ((type, ratio) in ratios) {
			sizes.put(
				VkDescriptorPoolSize.calloc()
					.type(type)
					.descriptorCount((ratio * maxSets).toInt())
			)
		}
		val poolCreateInfo = VkDescriptorPoolCreateInfo.calloc()
			.`sType$Default`()
			.flags(0)
			.maxSets(maxSets)
			.pPoolSizes(sizes.flip())

		val buf = MemoryUtil.memAllocLong(1)
		VK10.vkCreateDescriptorPool(device.device, poolCreateInfo, null, buf)
		val pool = buf.get()
		buf.free()
		return pool
	}

	fun allocateDescriptorSet(layout: VulkanDescriptorLayout): VulkanDescriptorSet {
		val descriptorSet = allocate(layout)
		return VulkanDescriptorSet(layout, descriptorSet)
	}

	fun resetPool(pool: Long) {
		VK10.vkResetDescriptorPool(device.device, pool, 0)
	}

	fun deletePool(pool: Long) {
		VK10.vkDestroyDescriptorPool(device.device, pool, null)
	}

	data class PoolSizeRatio(val type: Int, val ratio: Float)
}