package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo
import org.lwjgl.vulkan.VkDescriptorPoolSize

abstract class VulkanDescriptorAllocator(val device: VulkanDevice) : Deletable {

	abstract fun init(maxSets: Int, ratios: Map<Int, Float>)

	abstract fun allocate(layout: VulkanDescriptorLayout): Long

	abstract fun clearPools()

	fun createPool(maxSets: Int, ratios: Map<Int, Float>): Long {
		return MemoryStack.stackPush().use { stack ->
			val sizes = VkDescriptorPoolSize.calloc(ratios.count(), stack)
			for ((type, ratio) in ratios) {
				sizes.get().set(type, (ratio * maxSets).toInt())
			}
			val poolCreateInfo = VkDescriptorPoolCreateInfo.calloc(stack)
				.`sType$Default`()
				.flags(0)
				.maxSets(maxSets)
				.pPoolSizes(sizes.flip())

			val buf = stack.mallocLong(1)
			VK10.vkCreateDescriptorPool(device.device, poolCreateInfo, null, buf)
//			device.nameObject(buf[0], VK10.VK_OBJECT_TYPE_DESCRIPTOR_POOL, name)
			buf[0]
		}

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