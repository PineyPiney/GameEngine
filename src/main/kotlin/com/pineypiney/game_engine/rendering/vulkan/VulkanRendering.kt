package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.shaders.StencilOp
import com.pineypiney.game_engine.resources.shaders.parameters.CompareOp
import com.pineypiney.game_engine.resources.shaders.vulkan.pipeline.VulkanPipeline
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.vulkan.GrowableVulkanDescriptorAllocator
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.vulkan.VkStructs
import com.pineypiney.game_engine.window.Viewport
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkClearAttachment
import org.lwjgl.vulkan.VkClearRect

open class VulkanRendering(val cmd: PoolAndBuffer, val descriptorAllocator: GrowableVulkanDescriptorAllocator, val drawImage: () -> VulkanImage2D, val depthImage: () -> VulkanImage2D) : RenderingApi {

	val device get() = descriptorAllocator.device

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

	override fun clearStencil(value: Int) {
		MemoryStack.stackPush().use { stack ->
			val img = depthImage()
			val clearValue = VkStructs.clear(stack, Vec4(0f), 0f, value)
			val attachments = VkClearAttachment.calloc(1, stack).aspectMask(VK13.VK_IMAGE_ASPECT_STENCIL_BIT).clearValue(clearValue)
			val rects = VkClearRect.calloc(1, stack).rect(VkStructs.rect(stack, Vec2i(), img.size)).layerCount(1)
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
}