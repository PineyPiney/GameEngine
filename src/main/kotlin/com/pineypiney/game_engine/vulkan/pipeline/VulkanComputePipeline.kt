package com.pineypiney.game_engine.vulkan.pipeline

import com.pineypiney.game_engine.vulkan.VkUtil.processError
import com.pineypiney.game_engine.vulkan.VulkanDevice
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkComputePipelineCreateInfo
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo

class VulkanComputePipeline(device: VulkanDevice, layout: Long, pipeline: Long) : VulkanPipeline(device, pipeline, layout) {

	override fun getBindPoint(): Int = VK10.VK_PIPELINE_BIND_POINT_COMPUTE

	class Builder : VulkanPipeline.Builder<VulkanComputePipeline>() {

		val stage = VkPipelineShaderStageCreateInfo.calloc()

		fun setModule(module: Long): Builder {
			stage.set(createStageInfo(VK10.VK_SHADER_STAGE_COMPUTE_BIT, module))
			return this
		}

		fun setLayout(layout: Long): Builder {
			pLayout.clear().put(layout).flip()
			return this
		}

		override fun build(device: VulkanDevice): VulkanComputePipeline {

			val pipelineCreateInfo = VkComputePipelineCreateInfo.calloc(1)
				.`sType$Default`()
				.stage(stage)
				.layout(pLayout[0])

			val buf = MemoryUtil.memAllocLong(1)
			val err = VK10.vkCreateComputePipelines(device.device, 0L, pipelineCreateInfo, null, buf)
			val pipeline = buf[0]
			buf.free()
			pipelineCreateInfo.free()
			processError(err, "Failed to create Compute Pipeline")

			return VulkanComputePipeline(device, pLayout[0], pipeline)
		}

		override fun delete() {

		}
	}
}