package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.shaders.StencilOp
import com.pineypiney.game_engine.resources.shaders.parameters.CompareOp
import com.pineypiney.game_engine.resources.shaders.vulkan.pipeline.VulkanPipeline
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureUsage
import com.pineypiney.game_engine.window.Viewport
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import glm_.vec4.Vec4

interface RenderingApi : Deletable {

	fun bindShader(handle: Int)
	fun bindPipeline(pipeline: VulkanPipeline)

	fun bindTextureToPipeline(pipeline: VulkanPipeline, uniformName: String, texture: Texture)
	fun updateUniforms(pipeline: VulkanPipeline)

	fun bindFramebuffer(framebuffer: Framebuffer, colour: Vec4 = Vec4(0f), depth: Float = 0f, stencil: Int = 0, viewport: Viewport = Viewport(Vec2i(0), framebuffer.size))
	fun endFramebuffer(framebuffer: Framebuffer)

	fun bindVertices(handle: Int)
	fun bindIndices(handle: Int)
	fun bindIndices(handle: Long, offset: Long, type: Int)

	fun draw(vertexCount: Int, drawMode: Int, firstVertex: Int = 0)
	fun drawInstanced(vertexCount: Int, drawMode: Int, instanceCount: Int, firstVertex: Int = 0, firstInstance: Int = 0)
	fun drawIndexed(indexCount: Int, drawMode: Int, firstIndex: Int = 0)
	fun drawIndexedInstanced(indexCount: Int, drawMode: Int, instanceCount: Int, firstIndex: Int = 0, firstInstance: Int = 0)

	fun setViewport(viewport: Viewport)
	fun setDepthTest(compare: CompareOp?)
	fun clearStencil(value: Int)
	fun disableStencil()
	fun setStencil(enabled: Boolean, reference: Int, mask: Int, failOp: StencilOp, passOp: StencilOp, depthFailOp: StencilOp, compare: CompareOp)
	fun setStencilComparison(reference: Int, mask: Int, compare: CompareOp)
	fun setStencilOperations(failOp: StencilOp, passOp: StencilOp, depthFailOp: StencilOp)
	fun setStencilWriteMask(mask: Int)
	fun setScissors(viewport: Viewport)


	fun getMaxTessellationPatchSize(): Int
	fun getMaxTessellationLevel(): Int
	fun getMaxComputeWorkgroupSize(): Vec3i
	fun getMaxComputeWorkgroups(): Vec3i
	fun getMaxComputeWorkgroupInvocations(): Int
	fun getMaxViewport(): Vec2i

	fun updateGui(mat: Mat4, aspectRatio: Float)

	fun createFramebuffer(
		width: Int,
		height: Int,
		colourFormat: TextureFormat = TextureFormat.RGBA8,
		depthStencilFormat: TextureFormat = TextureFormat.DEPTH24_STENCIL8,
		usage: Collection<TextureUsage> = emptySet()
	): Framebuffer
}