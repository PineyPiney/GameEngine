package com.pineypiney.game_engine.resources.shaders.vulkan

class VulkanDescriptorSet(val layout: VulkanDescriptorLayout, val handle: Long) {

	init {
//		layout.device.nameObject(handle, VK10.VK_OBJECT_TYPE_DESCRIPTOR_SET, name)
	}
}