package com.pineypiney.game_engine.resources.shaders.vulkan.pipeline

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.vulkan.VulkanDescriptorBinding
import com.pineypiney.game_engine.vulkan.VulkanDescriptorLayout
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.vulkan.VK10

class VulkanPipelineLayout(val device: VulkanDevice, val handle: Long, val descriptorLayouts: List<VulkanDescriptorLayout>, pushConstants: Map<ShaderStage, DataType.PushConstants>) : Deletable {

	val pushConstantBuffers = pushConstants.mapValues { (stage, pushConstants) ->
		VulkanDescriptorBinding.UniformBuffer(device, 0, pushConstants.size, pushConstants.getOffsets())
	}

	override fun delete() {
		VK10.vkDestroyPipelineLayout(device.device, handle, null)
		descriptorLayouts.delete()
		pushConstantBuffers.delete()
	}
}