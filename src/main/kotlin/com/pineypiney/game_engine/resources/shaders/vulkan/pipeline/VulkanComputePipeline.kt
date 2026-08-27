package com.pineypiney.game_engine.resources.shaders.vulkan.pipeline

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.vulkan.VulkanRendering
import com.pineypiney.game_engine.resources.shaders.ComputeShader
import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanShaderModule
import com.pineypiney.game_engine.vulkan.VkStructs
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VkUtil.processResult
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkComputePipelineCreateInfo
import org.lwjgl.vulkan.VkPushConstantRange

class VulkanComputePipeline(layout: VulkanPipelineLayout, override val compute: VulkanShaderModule, pipeline: Long) : VulkanPipeline(pipeline, layout), ComputeShader {

	override fun getBindPoint(): Int = VK10.VK_PIPELINE_BIND_POINT_COMPUTE

	override fun dispatch(api: RenderingApi, x: Int, y: Int, z: Int) {
		endUniforms(api)
		(api as VulkanRendering).cmd.dispatch(x, y, z)
	}

	override fun getAllModules(): Iterable<VulkanShaderModule> = listOf(compute)

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
					val descriptorLayouts = compileDescriptorLayouts(device, mapOf(stage.getStage() to stage.data))
					val pushConstantsMap = mutableMapOf<ShaderStage, Pair<String, DataType.PushConstants>>()
					val pushConstants = stage.data.pushConstants
					val pushConstantsBuffer = if (pushConstants != null) {
						val buffer = VkPushConstantRange.calloc(1, stack)
						buffer[0].set(stage.getStage().vulkan, 0, pushConstants.second.size)
						pushConstantsMap[stage.getStage()] = pushConstants
						buffer
					} else null
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
				processResult(VK10.vkCreateComputePipelines(layout!!.device.device, 0L, pipelineCreateInfo, null, buf), "Failed to create Compute Pipeline")


				val name = "Compute Shader(${stage!!.getName()})"
				device.nameObject(buf[0], VK10.VK_OBJECT_TYPE_PIPELINE, name)
				device.nameObject(layout!!.handle, VK10.VK_OBJECT_TYPE_PIPELINE_LAYOUT, "$name Layout")

				return VulkanComputePipeline(layout!!, stage!!, buf[0])
			}
		}

		override fun delete() {

		}
	}
}