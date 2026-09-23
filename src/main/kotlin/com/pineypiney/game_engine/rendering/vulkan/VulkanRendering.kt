package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.shaders.StencilOp
import com.pineypiney.game_engine.resources.shaders.parameters.CompareOp
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanDescriptorAllocator
import com.pineypiney.game_engine.resources.shaders.vulkan.pipeline.VulkanPipeline
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureUsage
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import com.pineypiney.game_engine.util.extension_functions.getVec3i
import com.pineypiney.game_engine.util.extension_functions.orOfInt
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.vulkan.VkStructs
import com.pineypiney.game_engine.window.Viewport
import glm_.detail.GLM_DEPTH_CLIP_SPACE
import glm_.detail.GlmDepthClipSpace
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkClearAttachment
import org.lwjgl.vulkan.VkClearRect

abstract class VulkanRendering(val viewport: () -> Viewport) : RenderingApi {

	init {
		GLM_DEPTH_CLIP_SPACE = GlmDepthClipSpace.ZERO_TO_ONE
	}

	val device get() = descriptorAllocator.device
	val gpu get() = descriptorAllocator.device.physicalDevice

	abstract val cmd: PoolAndBuffer
	abstract val descriptorAllocator: VulkanDescriptorAllocator

	override fun bindShader(handle: Int) {
		throw UnsupportedOperationException("Vulkan shaders should have Long handles")
	}

	override fun bindPipeline(pipeline: VulkanPipeline) {
		cmd.bindPipeline(pipeline)
	}

	override fun bindTextureToPipeline(pipeline: VulkanPipeline, uniformName: String, texture: Texture) {
		if (texture is VulkanImage) pipeline.setImage(uniformName, texture)
	}

	override fun updateUniforms(pipeline: VulkanPipeline) {
		pipeline.updateDescriptors(cmd, descriptorAllocator)
		pipeline.updatePushConstants(cmd)
	}

	override fun bindFramebuffer(framebuffer: Framebuffer, colour: Vec4, depth: Float, stencil: Int, viewport: Viewport) {
		if (framebuffer !is VulkanFramebuffer) return

		framebuffer.colour.transition(cmd, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, false)
		framebuffer.depthStencil.transition(cmd, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, false)

		MemoryStack.stackPush().use { stack ->
			val colourAttachments = VkStructs.createColourAttachmentInfos(stack, framebuffer.colour, colour)
			val depthAttachment = VkStructs.createDepthStencilAttachmentInfo(stack, framebuffer.depthStencil, depth, stencil)
			val renderInfo = VkStructs.createRenderingInfo(stack, framebuffer.size, colourAttachments, depthAttachment)
			cmd.beginRendering(renderInfo)
		}

		setViewport(viewport)
		setScissors(viewport)
	}

	override fun endFramebuffer(framebuffer: Framebuffer) {
		cmd.endRendering()
		val usage = (framebuffer as VulkanFramebuffer).colourUsage
		if (usage and VK10.VK_IMAGE_USAGE_SAMPLED_BIT > 0) (framebuffer.colour as VulkanImage).transition(cmd, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
	}

	override fun bindVertices(handle: Int) {

	}

	override fun bindIndices(handle: Int) {
		throw UnsupportedOperationException("Vulkan meshes should have Long handles")
	}

	override fun bindIndices(handle: Long, offset: Long, type: Int) {
		cmd.bindIndices(handle, offset, type)
	}

	override fun draw(vertexCount: Int, drawMode: Int, firstVertex: Int) {
		cmd.draw(vertexCount, 1, firstVertex, 0)
	}

	override fun drawInstanced(vertexCount: Int, drawMode: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int) {
		cmd.draw(vertexCount, instanceCount, firstVertex, firstInstance)
	}

	override fun drawIndexed(indexCount: Int, drawMode: Int, firstIndex: Int) {
		cmd.drawIndexed(indexCount, 1, firstIndex, 0)
	}

	override fun drawIndexedInstanced(indexCount: Int, drawMode: Int, instanceCount: Int, firstIndex: Int, firstInstance: Int) {
		cmd.drawIndexed(indexCount, instanceCount, firstIndex, 0, firstInstance)
	}

	override fun setViewport(viewport: Viewport) {
		cmd.setViewport(viewport)
	}

	override fun setDepthTest(compare: CompareOp?) {
		if (compare == null) cmd.setDepthTest(false)
		else {
			cmd.setDepthTest(true)
			cmd.setDepthFunc(compare.vulkan)
		}
	}

	override fun clearStencil(value: Int) {
		MemoryStack.stackPush().use { stack ->
			val clearValue = VkStructs.clear(stack, Vec4(0f), 0f, value)
			val attachments = VkClearAttachment.calloc(1, stack).aspectMask(VK13.VK_IMAGE_ASPECT_STENCIL_BIT).clearValue(clearValue)
			val rects = VkClearRect.calloc(1, stack).rect(VkStructs.rect(stack, viewport())).layerCount(1)
			VK13.vkCmdClearAttachments(cmd.buffer, attachments, rects)
		}
	}

	override fun disableStencil() {
		cmd.setStencil(false)
	}

	override fun setStencil(enabled: Boolean, reference: Int, mask: Int, failOp: StencilOp, passOp: StencilOp, depthFailOp: StencilOp, compare: CompareOp) {
		cmd.setStencil(enabled)
		cmd.setStencil(3, reference, mask, failOp.vulkan, passOp.vulkan, depthFailOp.vulkan, compare.vulkan)
	}

	override fun setStencilComparison(reference: Int, mask: Int, compare: CompareOp) {
		cmd.setStencilComparison(3, reference, mask, compare.vulkan)
	}

	override fun setStencilOperations(failOp: StencilOp, passOp: StencilOp, depthFailOp: StencilOp) {
		cmd.setStencilOperations(3, failOp.vulkan, passOp.vulkan, depthFailOp.vulkan)
	}

	override fun setStencilWriteMask(mask: Int) {
		cmd.setStencilWriteMask(mask)
	}

	override fun setScissors(viewport: Viewport) {
		cmd.setScissors(viewport)
	}

	override fun getMaxTessellationPatchSize(): Int {
		return gpu.getLimits().maxTessellationPatchSize()
	}

	override fun getMaxTessellationLevel(): Int {
		return gpu.getLimits().maxTessellationGenerationLevel()
	}

	override fun getMaxComputeWorkgroupSize(): Vec3i = gpu.getLimits().maxComputeWorkGroupSize().getVec3i()

	override fun getMaxComputeWorkgroups(): Vec3i = gpu.getLimits().maxComputeWorkGroupCount().getVec3i()

	override fun getMaxComputeWorkgroupInvocations(): Int = gpu.getLimits().maxComputeWorkGroupInvocations()

	override fun getMaxViewport(): Vec2i {
		val buffer = gpu.getLimits().maxViewportDimensions()
		return Vec2i(buffer[0], buffer[1])
	}

	override fun updateGui(mat: Mat4, aspectRatio: Float) {
		glm.orthoLhZo(-aspectRatio, aspectRatio, -1f, 1f, 1f, 0f, mat)
	}

	override fun createFramebuffer(width: Int, height: Int, colourFormat: TextureFormat, depthStencilFormat: TextureFormat, usage: Collection<TextureUsage>): Framebuffer {
		val usage = usage.orOfInt(TextureUsage::vulkan)
		return VulkanFramebuffer(device, Vec2i(width, height), colourFormat, depthStencilFormat, usage)
	}

	companion object {
		fun create(cmd: PoolAndBuffer, descriptorAllocator: VulkanDescriptorAllocator, viewport: () -> Viewport): VulkanRendering {
			return object : VulkanRendering(viewport) {
				override val cmd: PoolAndBuffer = cmd
				override val descriptorAllocator: VulkanDescriptorAllocator = descriptorAllocator

				override fun delete() {}
			}
		}
	}
}