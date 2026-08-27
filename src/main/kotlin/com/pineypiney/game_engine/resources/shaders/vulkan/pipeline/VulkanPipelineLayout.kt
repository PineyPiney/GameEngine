package com.pineypiney.game_engine.resources.shaders.vulkan.pipeline

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanDescriptorLayout
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.vulkan.VulkanDevice
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10

class VulkanPipelineLayout(val device: VulkanDevice, val handle: Long, val descriptorLayouts: List<VulkanDescriptorLayout>, val pushConstants: Map<ShaderStage, Pair<String, DataType.PushConstants>>) :
	Deletable {

	val pushConstantBuffer = MemoryUtil.memAlloc(pushConstants.maxOfOrNull { it.value.second.max } ?: 0)

	fun containsBinding(name: String): Boolean {
		return descriptorLayouts.any { it.bindings.any { b -> b.contains(name) } }
	}

	override fun delete() {
		VK10.vkDestroyPipelineLayout(device.device, handle, null)
		descriptorLayouts.delete()
		pushConstantBuffer.free()
	}
}