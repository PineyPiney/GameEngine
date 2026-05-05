package com.pineypiney.game_engine.resources.shaders.vulkan.pipeline

import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanShaderModule
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.vulkan.VkStructs
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*

class VulkanGraphicsPipeline(pipeline: Long, layout: VulkanPipelineLayout) : VulkanPipeline(pipeline, layout) {

	override fun getBindPoint(): Int = VK10.VK_PIPELINE_BIND_POINT_GRAPHICS

	class Builder : VulkanPipeline.Builder<VulkanGraphicsPipeline>() {

		val modules = mutableListOf<VulkanShaderModule>()
		val inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc().`sType$Default`()
		val rasterization = VkPipelineRasterizationStateCreateInfo.calloc().`sType$Default`()
		val colourBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1)
		val multisample = VkPipelineMultisampleStateCreateInfo.calloc().`sType$Default`()
		val depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc().`sType$Default`()
		val renderInfo = VkPipelineRenderingCreateInfo.calloc().`sType$Default`()

		fun shaders(vertex: VulkanShaderModule, fragment: VulkanShaderModule): Builder {
			modules.clear()
			modules.addAll(vertex, fragment)
			return this
		}

		fun shaders(vertexName: String, fragmentName: String): Builder {
			val vertex = ShaderLoader.INSTANCE.shaderModules[ResourceKey(vertexName)]!!
			val fragment = ShaderLoader.INSTANCE.shaderModules[ResourceKey(fragmentName)]!!
			return shaders(vertex, fragment)
		}

		fun setLayout(layout: VulkanPipelineLayout): Builder {
			this.layout = layout
			return this
		}

		fun generateLayout(device: VulkanDevice): Builder {
			MemoryStack.stackPush().use { stack ->
				val descriptorLayouts = compileDescriptorLayouts(device, modules.associate { it.stage to it.data })
				val pushConstantsList = mutableSetOf<VkPushConstantRange>()
				val pushConstantsMap = mutableMapOf<ShaderStage, DataType.PushConstants>()
				for ((_, module) in modules.withIndex()) {
					val pushConstants = module.data.pushConstants
					if (pushConstants != null) {
						pushConstantsList.add(VkPushConstantRange.calloc(stack).set(module.stage.vulkan, 0, pushConstants.second.size))
						pushConstantsMap[module.stage] = pushConstants.second
					}
				}
				val pushConstantsBuffer = VkPushConstantRange.calloc(pushConstantsList.size, stack)
				for (range in pushConstantsList) pushConstantsBuffer.put(range)
				this.layout = VkUtil.createPipelineLayout(device, stack, descriptorLayouts, pushConstantsBuffer.flip(), pushConstantsMap)
			}
			return this
		}

		fun inputTopology(topology: Int): Builder {
			inputAssembly.topology(topology)
				.primitiveRestartEnable(false)
			return this
		}

		fun polygonMode(mode: Int): Builder {
			rasterization.polygonMode(mode)
				.lineWidth(1f)
			return this
		}

		fun cullMode(mode: Int, face: Int): Builder {
			rasterization.cullMode(mode)
				.frontFace(face)
			return this
		}

		fun enableBlending(srcColour: Int = VK10.VK_BLEND_FACTOR_SRC_ALPHA, dstColour: Int = VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA, op: Int = VK10.VK_BLEND_OP_ADD): Builder {
			colourBlendAttachment.colorWriteMask(15)
				.blendEnable(true)
				.srcColorBlendFactor(srcColour)
				.dstColorBlendFactor(dstColour)
				.colorBlendOp(op)
				.srcAlphaBlendFactor(VK10.VK_BLEND_FACTOR_ONE)
				.dstAlphaBlendFactor(VK10.VK_BLEND_FACTOR_ZERO)
				.alphaBlendOp(op)

			return this
		}

		fun disableBlending(): Builder {
			// 15 = RGBA bitmask
			colourBlendAttachment.colorWriteMask(15)
				.blendEnable(false)
			return this
		}

		fun disableMultisampling(): Builder {
			multisample.sampleShadingEnable(false)
				.rasterizationSamples(1)
				.minSampleShading(1f)
				.pSampleMask(null)
				.alphaToCoverageEnable(false)
				.alphaToOneEnable(false)
			return this
		}

		fun enableDepthTest(write: Boolean, op: Int): Builder {
			depthStencil.depthTestEnable(true)
				.depthWriteEnable(write)
				.depthCompareOp(op)
				.depthBoundsTestEnable(false)
				.stencilTestEnable(false)
				.front(VkStencilOpState::clear)
				.back(VkStencilOpState::clear)
				.minDepthBounds(0f)
				.maxDepthBounds(1f)

			return this
		}

		fun disableDepthTest(): Builder {
			depthStencil.depthTestEnable(false)
				.depthWriteEnable(false)
				.depthCompareOp(VK10.VK_COMPARE_OP_NEVER)
				.depthBoundsTestEnable(false)
				.stencilTestEnable(false)
				.front(VkStencilOpState::clear)
				.back(VkStencilOpState::clear)
				.minDepthBounds(0f)
				.maxDepthBounds(1f)
			return this
		}

		fun colourFormat(format: Int): Builder {

			renderInfo.colorAttachmentCount(1)
				.pColorAttachmentFormats(MemoryUtil.memAllocInt(1).put(format).flip())
			return this
		}

		fun depthFormat(format: Int): Builder {
			renderInfo.depthAttachmentFormat(format)
			return this
		}

		override fun build(device: VulkanDevice): VulkanGraphicsPipeline {


			if (layout == null) throw Error("No layout was set for this pipeline")

			MemoryStack.stackPush().use { stack ->
				val viewport = VkPipelineViewportStateCreateInfo.calloc(stack)
					.`sType$Default`()
					.viewportCount(1)
					.scissorCount(1)

				val colourBlend = VkPipelineColorBlendStateCreateInfo.calloc(stack)
					.`sType$Default`()
					.logicOpEnable(false)
					.logicOp(VK10.VK_LOGIC_OP_COPY)
					.pAttachments(colourBlendAttachment)
					.attachmentCount(1)

				val vertexInput = VkPipelineVertexInputStateCreateInfo.calloc(stack).`sType$Default`()


				val dynamicStates = stack.mallocInt(2)
					.put(VK10.VK_DYNAMIC_STATE_VIEWPORT)
					.put(VK10.VK_DYNAMIC_STATE_SCISSOR)
					.flip()
				val dynamicState = VkPipelineDynamicStateCreateInfo.calloc(stack)
					.`sType$Default`()
					.pDynamicStates(dynamicStates)

				val stageInfos = VkPipelineShaderStageCreateInfo.calloc(modules.size, stack)
				for (module in modules) stageInfos.put(VkStructs.createStageInfo(stack, module))

				val pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
					.`sType$Default`()
					.pNext(renderInfo)
					.pStages(stageInfos.flip())
					.stageCount(stageInfos.capacity())
					.pVertexInputState(vertexInput)
					.pInputAssemblyState(inputAssembly)
					.pViewportState(viewport)
					.pRasterizationState(rasterization)
					.pMultisampleState(multisample)
					.pColorBlendState(colourBlend)
					.pDepthStencilState(depthStencil)
					.pDynamicState(dynamicState)
					.layout(layout!!.handle)

				val pPipeline = stack.mallocLong(1)

				VkUtil.processError(VK10.vkCreateGraphicsPipelines(layout!!.device.device, 0L, pipelineInfo, null, pPipeline), "Failed to create Graphics Pipeline")
				return VulkanGraphicsPipeline(pPipeline[0], layout!!)
			}
		}

		fun clear(): Builder {
			modules.clear()
			inputAssembly.clear()
			inputAssembly.`sType$Default`()
			rasterization.clear()
			rasterization.`sType$Default`()
			colourBlendAttachment.clear()
			multisample.clear()
			multisample.`sType$Default`()
			depthStencil.clear()
			depthStencil.`sType$Default`()
			renderInfo.clear()
			renderInfo.`sType$Default`()
			layout = null

			return this
		}

		override fun delete() {
			modules.clear()
			inputAssembly.free()
			rasterization.free()
			colourBlendAttachment.free()
			multisample.free()
			depthStencil.free()
			renderInfo.free()
		}
	}
}