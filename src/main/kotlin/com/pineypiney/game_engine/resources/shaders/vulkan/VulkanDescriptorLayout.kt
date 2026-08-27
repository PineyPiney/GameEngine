package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo
import java.nio.LongBuffer

class VulkanDescriptorLayout(val device: VulkanDevice, val pointer: LongBuffer, val stages: Int, val set: Int, val bindings: List<VulkanDescriptorBinding>) : Deletable {

	val handle get() = pointer[0]

	override fun delete() {
		bindings.delete()
		VK10.vkDestroyDescriptorSetLayout(device.device, handle, null)
	}

	class Builder(val set: Int) {

		var stages = 0
		val bindings = mutableSetOf<VulkanDescriptorBinding>()

		fun addStage(stage: ShaderStage) {
			stages = stages or stage.vulkan
		}

		fun addBinding(binding: VulkanDescriptorBinding): Builder {
			bindings.add(binding)
			return this
		}

		fun addStorageImage(binding: Int, name: String) = addBinding(VulkanDescriptorBinding.StorageImage(binding, name))
		fun addCombinedImage(binding: Int, name: String) = addBinding(VulkanDescriptorBinding.CombinedSampler(binding, name))
		fun addStorageBuffer(device: VulkanDevice, binding: Int, name: String, size: Int, offsets: Map<String, Int>) =
			addBinding(VulkanDescriptorBinding.UniformBuffer(device, binding, name, size, offsets))

		fun clear() = bindings.clear()

		fun build(device: VulkanDevice, flags: Int = 0): VulkanDescriptorLayout {
			val bindingBuffer = VkDescriptorSetLayoutBinding.calloc(bindings.size)

			for (binding in bindings) {
				bindingBuffer.get()
					.binding(binding.binding)
					.descriptorType(binding.type)
					.descriptorCount(1)
					.stageFlags(stages)
			}

			val layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc()
				.`sType$Default`()
				.pBindings(bindingBuffer.flip())
				.flags(flags)

			val buf = MemoryUtil.memAllocLong(1)
			val err = VK10.vkCreateDescriptorSetLayout(device.device, layoutInfo, null, buf)
			VkUtil.processResult(err, "Failed to create Descriptor Set Layout")
//			device.nameObject(buf[0], VK10.VK_OBJECT_TYPE_DESCRIPTOR_SET_LAYOUT, name)
			return VulkanDescriptorLayout(device, buf, stages, set, bindings.toList())
		}
	}
}