package com.pineypiney.game_engine.vulkan

import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo

class VulkanDescriptorLayoutBuilder {

	val bindings = mutableSetOf<VkDescriptorSetLayoutBinding>()

	fun addBinding(binding: Int, type: Int) {
		bindings.add(
			VkDescriptorSetLayoutBinding.calloc()
				.binding(binding)
				.descriptorType(type)
				.descriptorCount(1)
		)
	}

	fun clear() = bindings.clear()

	fun build(device: VulkanDevice, shaderFlags: Int, flags: Int = 0): Long {
		val bindingBuffer = VkDescriptorSetLayoutBinding.calloc(bindings.size)

		for (binding in bindings) {
			binding.stageFlags(binding.stageFlags() or shaderFlags)

			bindingBuffer.put(binding)
		}

		val layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc()
			.`sType$Default`()
			.pBindings(bindingBuffer.flip())
			.flags(flags)

		val buf = MemoryUtil.memAllocLong(1)
		val err = VK10.vkCreateDescriptorSetLayout(device.device, layoutInfo, null, buf)
		val layout = buf[0]

//		bindingBuffer.free()
		VkUtil.processError(err, "Failed to create Descriptor Set Layout")
		return layout
	}
}