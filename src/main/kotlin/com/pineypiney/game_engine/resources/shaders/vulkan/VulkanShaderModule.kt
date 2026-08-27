package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.resources.shaders.ShaderModule
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.vulkan.VK10

class VulkanShaderModule(val data: VulkanShaderData, private val stage: ShaderStage, val device: VulkanDevice, val handle: Long) : ShaderModule {

	override fun getName(): String = data.name
	override fun getStage(): ShaderStage = stage

	override fun delete() {
		VK10.vkDestroyShaderModule(device.device, handle, null)
	}

	override fun toString(): String {
		return "ShaderModule[${getName()}]"
	}
}