package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.extension_functions.delete
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo
import java.nio.LongBuffer

class VulkanDescriptorLayout(val device: VulkanDevice, val pointer: LongBuffer, val bindings: List<VulkanDescriptorBinding>) : Deletable {

	val handle get() = pointer[0]

	override fun delete() {
		bindings.delete()
		VK10.vkDestroyDescriptorSetLayout(device.device, handle, null)
	}

	class Builder {

		val bindings = mutableSetOf<VulkanDescriptorBinding>()

		fun addBinding(binding: VulkanDescriptorBinding): Builder {
			bindings.add(binding)
			return this
		}

		fun addStorageImage(binding: Int, name: String) = addBinding(VulkanDescriptorBinding.StorageImage(binding, name))
		fun addCombinedImage(binding: Int, name: String) = addBinding(VulkanDescriptorBinding.CombinedSampler(binding, name))
		fun addStorageBuffer(device: VulkanDevice, binding: Int, size: Int, offsets: Map<String, Int>) = addBinding(VulkanDescriptorBinding.UniformBuffer(device, binding, size, offsets))

		fun clear() = bindings.clear()

		fun build(device: VulkanDevice, shaderFlags: Int, flags: Int = 0): VulkanDescriptorLayout {
			val bindingBuffer = VkDescriptorSetLayoutBinding.calloc(bindings.size)

			for (binding in bindings) {
				bindingBuffer.get()
					.binding(binding.binding)
					.descriptorType(binding.type)
					.descriptorCount(1)
					.stageFlags(shaderFlags)
			}

			val layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc()
				.`sType$Default`()
				.pBindings(bindingBuffer.flip())
				.flags(flags)

			val buf = MemoryUtil.memAllocLong(1)
			val err = VK10.vkCreateDescriptorSetLayout(device.device, layoutInfo, null, buf)

			VkUtil.processError(err, "Failed to create Descriptor Set Layout")
			return VulkanDescriptorLayout(device, buf, bindings.toList())
		}
	}
}