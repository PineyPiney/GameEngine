package com.pineypiney.game_engine.vulkan

import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkPhysicalDevice

class VulkanPhysicalDevice(val physicalDevice: VkPhysicalDevice) {

	constructor(instance: VkInstance, handle: Long) : this(VkPhysicalDevice(handle, instance))

	init {

	}
}