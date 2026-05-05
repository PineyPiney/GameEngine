package com.pineypiney.game_engine.resources.shaders.vulkan.pipeline

import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanShaderModule
import com.pineypiney.game_engine.vulkan.VkStructs
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VkUtil.processError
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkComputePipelineCreateInfo
import org.lwjgl.vulkan.VkPushConstantRange

class VulkanComputePipeline(layout: VulkanPipelineLayout, pipeline: Long) : VulkanPipeline(pipeline, layout) {

	override fun getBindPoint(): Int = VK10.VK_PIPELINE_BIND_POINT_COMPUTE

	class Builder : VulkanPipeline.Builder<VulkanComputePipeline>() {

		var stage: VulkanShaderModule? = null

		fun setModule(module: VulkanShaderModule): Builder {
			stage = module
			return this
		}

		fun setLayout(layout: VulkanPipelineLayout): Builder {
			this.layout = layout
			return this
		}

		fun generateLayout(device: VulkanDevice): Builder {
			stage?.let { stage ->
				MemoryStack.stackPush().use { stack ->
					val descriptorLayouts = compileDescriptorLayouts(device, mapOf(stage.stage to stage.data))
					val pushConstantsBuffer = VkPushConstantRange.calloc(1, stack)
					val pushConstantsMap = mutableMapOf<ShaderStage, DataType.PushConstants>()
					val pushConstants = stage.data.pushConstants
					if (pushConstants != null) {
						pushConstantsBuffer[0].set(stage.stage.vulkan, 0, pushConstants.second.size)
						pushConstantsMap[stage.stage] = pushConstants.second
					}
					this.layout = VkUtil.createPipelineLayout(device, stack, descriptorLayouts, pushConstantsBuffer, pushConstantsMap)
				}
			}
			return this
		}

		override fun build(device: VulkanDevice): VulkanComputePipeline {

			if (stage == null) throw Error("No Compute Shader was set for this compute pipeline")
			else if (layout == null) throw Error("No layout was set for this pipeline")

			MemoryStack.stackPush().use { stack ->
				val pipelineCreateInfo = VkComputePipelineCreateInfo.calloc(1, stack)
					.`sType$Default`()
					.stage(VkStructs.createStageInfo(stack, stage!!))
					.layout(layout!!.handle)

				val buf = stack.mallocLong(1)
				processError(VK10.vkCreateComputePipelines(layout!!.device.device, 0L, pipelineCreateInfo, null, buf), "Failed to create Compute Pipeline")
				return VulkanComputePipeline(layout!!, buf[0])
			}
		}

		override fun delete() {

		}
	}
}