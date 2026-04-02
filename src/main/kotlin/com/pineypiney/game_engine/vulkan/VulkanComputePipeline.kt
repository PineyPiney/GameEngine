package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkPushConstantRange

class VulkanComputePipeline(val device: VulkanDevice, val layout: Long, val pipeline: Long, val constants: VkPushConstantRange.Buffer? = null) : Deletable {


	override fun delete() {
		VK10.vkDestroyPipeline(device.device, pipeline, null)
		VK10.vkDestroyPipelineLayout(device.device, layout, null)
	}
}