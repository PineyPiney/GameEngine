package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.vulkan.VK10

class VulkanShaderModule(val data: VulkanShaderData, val stage: ShaderStage, val device: VulkanDevice, val handle: Long) : Deletable {

	override fun delete() {
		VK10.vkDestroyShaderModule(device.device, handle, null)
	}
}