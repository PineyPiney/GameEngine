package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkDevice

class VulkanDeletionQueue(val device: VulkanDevice) : DeletionQueue() {

	fun <E> push(obj: E, func: (VkDevice, E, VkAllocationCallbacks?) -> Unit): DeletionQueue {
		return push { func(device.device, obj, null) }
	}
}