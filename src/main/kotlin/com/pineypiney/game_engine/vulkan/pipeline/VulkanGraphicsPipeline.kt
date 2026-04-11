package com.pineypiney.game_engine.vulkan.pipeline

import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanDevice
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*

class VulkanGraphicsPipeline(device: VulkanDevice, pipeline: Long, layout: Long) : VulkanPipeline(device, pipeline, layout) {

	override fun getBindPoint(): Int = VK10.VK_PIPELINE_BIND_POINT_GRAPHICS

	class Builder : VulkanPipeline.Builder<VulkanGraphicsPipeline>() {

		val stages = VkPipelineShaderStageCreateInfo.calloc(2).`sType$Default`()
		val inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc().`sType$Default`()
		val rasterization = VkPipelineRasterizationStateCreateInfo.calloc().`sType$Default`()
		val colourBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1)
		val multisample = VkPipelineMultisampleStateCreateInfo.calloc().`sType$Default`()
		val depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc().`sType$Default`()
		val renderInfo = VkPipelineRenderingCreateInfo.calloc().`sType$Default`()

		fun shaders(vertex: Long, fragment: Long): Builder {
			stages.clear()
				.put(createStageInfo(VK10.VK_SHADER_STAGE_VERTEX_BIT, vertex))
				.put(createStageInfo(VK10.VK_SHADER_STAGE_FRAGMENT_BIT, fragment))
				.flip()
			return this
		}

		fun shaders(vertexName: String, fragmentName: String): Builder {
			val vertex = ShaderLoader.INSTANCE.shaderModules[ResourceKey(vertexName)]!!
			val fragment = ShaderLoader.INSTANCE.shaderModules[ResourceKey(fragmentName)]!!
			return shaders(vertex, fragment)
		}

		fun setLayout(layout: Long): Builder {
			pLayout.clear().put(layout).flip()
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
			val viewport = VkPipelineViewportStateCreateInfo.calloc()
				.`sType$Default`()
				.viewportCount(1)
				.scissorCount(1)

			val colourBlend = VkPipelineColorBlendStateCreateInfo.calloc()
				.`sType$Default`()
				.logicOpEnable(false)
				.logicOp(VK10.VK_LOGIC_OP_COPY)
				.pAttachments(colourBlendAttachment)
				.attachmentCount(1)

			val vertexInput = VkPipelineVertexInputStateCreateInfo.calloc().`sType$Default`()


			val dynamicStates = MemoryUtil.memAllocInt(2)
				.put(VK10.VK_DYNAMIC_STATE_VIEWPORT)
				.put(VK10.VK_DYNAMIC_STATE_SCISSOR)
				.flip()
			val dynamicState = VkPipelineDynamicStateCreateInfo.calloc()
				.`sType$Default`()
				.pDynamicStates(dynamicStates)

			val pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1)
				.`sType$Default`()
				.pNext(renderInfo)
				.pStages(stages)
				.stageCount(stages.capacity())
				.pVertexInputState(vertexInput)
				.pInputAssemblyState(inputAssembly)
				.pViewportState(viewport)
				.pRasterizationState(rasterization)
				.pMultisampleState(multisample)
				.pColorBlendState(colourBlend)
				.pDepthStencilState(depthStencil)
				.pDynamicState(dynamicState)
				.layout(pLayout[0])


			val pPipeline = MemoryUtil.memAllocLong(1)
			val err = VK10.vkCreateGraphicsPipelines(device.device, 0L, pipelineInfo, null, pPipeline)

			val handle = pPipeline[0]
			pPipeline.clear()

			VkUtil.processError(err, "Failed to create Graphics Pipeline")
			return VulkanGraphicsPipeline(device, handle, pLayout[0])
		}

		fun clear(): Builder {
			stages.clear()
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
			pLayout.clear()

			return this
		}

		override fun delete() {
			stages.free()
			inputAssembly.free()
			rasterization.free()
			colourBlendAttachment.free()
			multisample.free()
			depthStencil.free()
			renderInfo.free()
			pLayout.free()
		}
	}
}