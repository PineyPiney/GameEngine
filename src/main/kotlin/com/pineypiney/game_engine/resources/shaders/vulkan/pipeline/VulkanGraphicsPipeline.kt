package com.pineypiney.game_engine.resources.shaders.vulkan.pipeline

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanShaderModule
import com.pineypiney.game_engine.util.RandomHelper
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.extension_functions.popFirst
import com.pineypiney.game_engine.vulkan.VkStructs
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*

class VulkanGraphicsPipeline(
	pipeline: Long,
	override val vertex: VulkanShaderModule,
	override val fragment: VulkanShaderModule,
	override val stages: List<VulkanShaderModule>,
	override val parameters: RenderShaderParameters,
	layout: VulkanPipelineLayout
) : VulkanPipeline(pipeline, layout), RenderShader {

	override val screenMask: Byte = RandomHelper.createMask(layout::containsBinding, "view", "projection", "guiProjection", "viewport", "viewPos").toByte()

	override val lightMask: Byte = RandomHelper.createMask(
		layout::containsBinding,
		"dirLight.ambient",
		"pointLight.ambient",
		"spotLight.ambient"
	).toByte()

	override fun draw(meshName: String, mesh: Mesh, api: RenderingApi) {
		setMesh(meshName, mesh)
		endUniforms(api)
		mesh.draw(api)
	}

	override fun getBindPoint(): Int = VK10.VK_PIPELINE_BIND_POINT_GRAPHICS

	override fun getAllModules(): Iterable<VulkanShaderModule> {
		val list = mutableListOf(vertex, fragment)
		list.addAll(stages)
		return list
	}

	override fun toString(): String {
		return "Graphics Pipeline('${vertex.getName()}, ${fragment.getName()}, ${stages.joinToString(transform = VulkanShaderModule::getName)}')"
	}

	class Builder : VulkanPipeline.Builder<VulkanGraphicsPipeline>() {

		val modules = mutableListOf<VulkanShaderModule>()
		var parameters = RenderShaderParameters()
		val inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc().`sType$Default`()
		val rasterization = VkPipelineRasterizationStateCreateInfo.calloc().`sType$Default`()
		val colourBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1)
		val multisample = VkPipelineMultisampleStateCreateInfo.calloc().`sType$Default`()
		val depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc().`sType$Default`()
		val renderInfo = VkPipelineRenderingCreateInfo.calloc().`sType$Default`()
		val dynamic = VkPipelineDynamicStateCreateInfo.calloc()

		fun shaders(vertex: VulkanShaderModule, fragment: VulkanShaderModule): Builder {
			modules.clear()
			modules.addAll(vertex, fragment)
			return this
		}

		fun shaders(vertexName: String, fragmentName: String): Builder {
			val vertex = ShaderLoader.INSTANCE.getSubShader(ResourceKey(vertexName)) as VulkanShaderModule
			val fragment = ShaderLoader.INSTANCE.getSubShader(ResourceKey(fragmentName)) as VulkanShaderModule
			return shaders(vertex, fragment)
		}

		fun setLayout(layout: VulkanPipelineLayout): Builder {
			this.layout = layout
			return this
		}

		fun generateLayout(device: VulkanDevice): Builder {
			MemoryStack.stackPush().use { stack ->
				val descriptorLayouts = compileDescriptorLayouts(device, modules.associate { it.getStage() to it.data })
				val pushConstants = compilePushConstants(modules)
				this.layout = VkUtil.createPipelineLayout(device, stack, descriptorLayouts, pushConstants)
			}
			return this
		}

		fun parameters(parameters: RenderShaderParameters): Builder {
			this.parameters = parameters
			inputTopology(parameters.topology.vulkan)
				.polygonMode(parameters.fillMode.vulkan)
				.cullMode(parameters.cullMode.vulkan, VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE)

			parameters.depthTestOp?.let { enableDepthTest(true, it.vulkan) } ?: disableDepthTest()
			parameters.blending?.let { (src, dst, op) -> enableBlending(src.vulkan, dst.vulkan, op.vulkan) } ?: disableBlending()
			if (parameters.multisampling == 1) disableMultisampling() else enableMultisampling(parameters.multisampling)

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

		fun enableMultisampling(samples: Int): Builder {
			multisample.sampleShadingEnable(true)
				.rasterizationSamples(samples)
				.minSampleShading(1f)
				.pSampleMask(null)
				.alphaToCoverageEnable(false)
				.alphaToOneEnable(false)
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
				.front(VkStencilOpState::clear)
				.back(VkStencilOpState::clear)
				.minDepthBounds(0f)
				.maxDepthBounds(1f)
			return this
		}

		fun enableStencilTest(): Builder {
			depthStencil.stencilTestEnable(true)
			return this
		}

		fun disableStencilTest(): Builder {
			depthStencil.stencilTestEnable(false)
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

				val dynamicStatesArray = intArrayOf(
					VK10.VK_DYNAMIC_STATE_VIEWPORT,
					VK10.VK_DYNAMIC_STATE_SCISSOR,
					VK10.VK_DYNAMIC_STATE_STENCIL_COMPARE_MASK,
					VK10.VK_DYNAMIC_STATE_STENCIL_WRITE_MASK,
					VK10.VK_DYNAMIC_STATE_STENCIL_REFERENCE,
					VK13.VK_DYNAMIC_STATE_STENCIL_TEST_ENABLE,
					VK13.VK_DYNAMIC_STATE_STENCIL_OP,
				)
				val dynamicStates = stack.mallocInt(dynamicStatesArray.size).put(dynamicStatesArray).flip()
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

				VkUtil.processResult(VK10.vkCreateGraphicsPipelines(layout!!.device.device, 0L, pipelineInfo, null, pPipeline), "Failed to create Graphics Pipeline")
				val optionalModules = modules.toMutableList()
				val vertex = optionalModules.popFirst { it.getStage() == ShaderStage.VERTEX }
				val fragment = optionalModules.popFirst { it.getStage() == ShaderStage.FRAGMENT }

				val name = "Render Shader(${vertex.getName()}, ${fragment.getName()}, ${optionalModules.joinToString(transform = VulkanShaderModule::getName)})"
				device.nameObject(pPipeline[0], VK10.VK_OBJECT_TYPE_PIPELINE, name)
				device.nameObject(layout!!.handle, VK10.VK_OBJECT_TYPE_PIPELINE_LAYOUT, "$name Layout")

				return VulkanGraphicsPipeline(pPipeline[0], vertex, fragment, optionalModules, parameters, layout!!)
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