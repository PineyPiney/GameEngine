package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanDescriptorAllocator
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanDescriptorLayout
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo

class StaticVulkanDescriptorAllocator(device: VulkanDevice) : VulkanDescriptorAllocator(device) {

	var pool = 0L

	override fun init(maxSets: Int, ratios: Map<Int, Float>) {
		delete()
		pool = createPool(maxSets, ratios)
	}

	override fun allocate(layout: VulkanDescriptorLayout): Long {
		val allocateInfo = VkDescriptorSetAllocateInfo.calloc()
			.`sType$Default`()
			.descriptorPool(pool)
			.pSetLayouts(layout.pointer)

		val buffer = MemoryUtil.memAllocLong(1)
		val err = VK10.vkAllocateDescriptorSets(device.device, allocateInfo, buffer)
		allocateInfo.free()
		val descriptorSet = buffer[0]
		buffer.free()
		VkUtil.processResult(err, "Failed to allocate descriptor set")
		return descriptorSet
	}

	override fun clearPools() {
		resetPool(pool)
	}

	override fun delete() {
		deletePool(pool)
		pool = 0L
	}
}