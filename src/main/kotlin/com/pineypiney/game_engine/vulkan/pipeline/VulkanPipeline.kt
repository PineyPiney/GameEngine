package com.pineypiney.game_engine.vulkan.pipeline

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.meshes.RenderingApi
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo

abstract class VulkanPipeline(val device: VulkanDevice, val pipeline: Long, val layout: Long) : Deletable {

	fun bind(api: RenderingApi) {
		api.bindPipeline(pipeline, getBindPoint())
	}

	abstract fun getBindPoint(): Int

	override fun delete() {
		VK10.vkDestroyPipeline(device.device, pipeline, null)
		VK10.vkDestroyPipelineLayout(device.device, layout, null)
	}

	abstract class Builder<P : VulkanPipeline> : Deletable {

		val pLayout = MemoryUtil.memAllocLong(1)

		fun createStageInfo(stage: Int, module: Long): VkPipelineShaderStageCreateInfo {
			return VkPipelineShaderStageCreateInfo.calloc()
				.`sType$Default`()
				.stage(stage)
				.module(module)
				.pName(MemoryUtil.memUTF8("main"))
		}

		abstract fun build(device: VulkanDevice): P
	}
}