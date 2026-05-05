package com.pineypiney.game_engine.vulkan

import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK11
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo
import kotlin.math.min

class GrowableVulkanDescriptorAllocator(device: VulkanDevice) : VulkanDescriptorAllocator(device) {

	val fullPools = mutableListOf<Long>()
	val readyPools = mutableListOf<Long>()
	var setsPerPool = 10

	val ratios = mutableMapOf<Int, Float>()

	override fun init(maxSets: Int, ratios: Map<Int, Float>) {
		this.ratios.clear()
		this.ratios.putAll(ratios)

		val newPool = createPool(maxSets, ratios)
		setsPerPool = (maxSets * 1.5f).toInt()
		readyPools.add(newPool)
	}

	override fun allocate(layout: VulkanDescriptorLayout): Long {

		var pool = getPool()

		val allocateInfo = VkDescriptorSetAllocateInfo.calloc()
			.`sType$Default`()
			.descriptorPool(pool)
			.pSetLayouts(layout.pointer)

		val buffer = MemoryUtil.memAllocLong(1)
		var err = VK10.vkAllocateDescriptorSets(device.device, allocateInfo, buffer)

		// If the allocation fails, try again
		if (err == VK11.VK_ERROR_OUT_OF_POOL_MEMORY || err == VK10.VK_ERROR_FRAGMENTED_POOL) {
			fullPools.add(pool)
			pool = getPool()
			allocateInfo.descriptorPool(pool)
			err = VK10.vkAllocateDescriptorSets(device.device, allocateInfo, buffer)
		}

		allocateInfo.free()
		val descriptorSet = buffer[0]
		buffer.free()
		VkUtil.processError(err, "Failed to allocate descriptor set")

		readyPools.add(pool)
		return descriptorSet
	}

	override fun clearPools() {
		for (pool in readyPools) resetPool(pool)
		for (pool in fullPools) {
			resetPool(pool)
			readyPools.add(pool)
		}
		fullPools.clear()
	}

	override fun delete() {
		for (pool in readyPools) deletePool(pool)
		for (pool in fullPools) deletePool(pool)
		readyPools.clear()
		fullPools.clear()
	}

	fun getPool(): Long {
		if (readyPools.isNotEmpty()) return readyPools.removeLast()

		val newPool = createPool(setsPerPool, ratios)
		setsPerPool = min((setsPerPool * 1.5f).toInt(), 4092)
		return newPool
	}
}