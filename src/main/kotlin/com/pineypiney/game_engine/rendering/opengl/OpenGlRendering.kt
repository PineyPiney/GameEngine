package com.pineypiney.game_engine.rendering.opengl

import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.shaders.StencilOp
import com.pineypiney.game_engine.resources.shaders.parameters.CompareOp
import com.pineypiney.game_engine.resources.shaders.vulkan.pipeline.VulkanPipeline
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureUsage
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.window.Viewport
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL33C

object OpenGlRendering : RenderingApi {

	override fun bindShader(handle: Int) {
		GL33C.glUseProgram(handle)
	}

	override fun bindPipeline(pipeline: VulkanPipeline) {
		throw UnsupportedOperationException("OpenGL shaders should have Integer handles")
	}

	override fun bindTextureToPipeline(pipeline: VulkanPipeline, uniformName: String, texture: Texture) {
		throw UnsupportedOperationException("OpenGL pipelines are not supported")
	}

	override fun updateUniforms(pipeline: VulkanPipeline) {
		throw UnsupportedOperationException("OpenGL pipelines are not supported")
	}

	override fun bindFramebuffer(framebuffer: Framebuffer, colour: Vec4, depth: Float, stencil: Int, viewport: Viewport) {
		(framebuffer as OpenGlFramebuffer).bind()

		GLFunc.clearColour = colour
		GLFunc.depthClear = depth.toDouble()
		GLFunc.stencilClear = stencil
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT or GL11C.GL_DEPTH_BUFFER_BIT or GL11C.GL_STENCIL_BUFFER_BIT)

		setViewport(viewport)
		GLFunc.scissor = false
//		setScissors(viewport)
	}

	override fun endFramebuffer(framebuffer: Framebuffer) {
		OpenGlFramebuffer.unbind()
	}

	override fun bindVertices(handle: Int) {
		GL33C.glBindVertexArray(handle)
	}

	override fun bindIndices(handle: Int) {
		GL33C.glBindBuffer(GL33C.GL_ELEMENT_ARRAY_BUFFER, handle)
	}

	override fun bindIndices(handle: Long, offset: Long, type: Int) {
		throw UnsupportedOperationException("OpenGL meshes should have Integer handles")
	}

	override fun draw(vertexCount: Int, drawMode: Int, firstVertex: Int) {
		GL33C.glDrawArrays(drawMode, firstVertex, vertexCount)
	}

	override fun drawInstanced(vertexCount: Int, drawMode: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int) {
		if (firstInstance != 0) throw UnsupportedOperationException("OpenGL instanced drawing always starts at instance 0")
		GL33C.glDrawArraysInstanced(drawMode, firstInstance, vertexCount, instanceCount)
	}

	override fun drawIndexed(indexCount: Int, drawMode: Int, firstIndex: Int) {
		GL33C.glDrawElements(drawMode, indexCount, GL_UNSIGNED_INT, firstIndex.toLong())
	}

	override fun drawIndexedInstanced(indexCount: Int, drawMode: Int, instanceCount: Int, firstIndex: Int, firstInstance: Int) {
		GL33C.glDrawElementsInstanced(drawMode, indexCount, GL_UNSIGNED_INT, firstIndex.toLong(), instanceCount)
	}

	override fun setViewport(viewport: Viewport) {
		GLFunc.viewport = Vec4i(viewport.bl, viewport.size)
	}

	override fun setDepthTest(compare: CompareOp?) {
		if (compare == null) GLFunc.depthTest = false
		else {
			GLFunc.depthTest = true
			GLFunc.depthFunc - compare.opengl
		}
	}

	override fun clearStencil(value: Int) {
		GLFunc.stencilClear = value
		GL11C.glClear(GL11C.GL_STENCIL_BUFFER_BIT)
	}

	override fun disableStencil() {
		GLFunc.stencilTest = false
	}

	override fun setStencil(enabled: Boolean, reference: Int, mask: Int, failOp: StencilOp, passOp: StencilOp, depthFailOp: StencilOp, compare: CompareOp) {
		GLFunc.stencilTest = enabled
		GLFunc.stencilFRM = Vec3i(compare.opengl, reference, mask)
		GLFunc.stencilOp = Vec3i(failOp.opengl, depthFailOp.opengl, passOp.opengl)
	}

	override fun setStencilComparison(reference: Int, mask: Int, compare: CompareOp) {
		GLFunc.stencilFRM = Vec3i(compare.opengl, reference, mask)
	}

	override fun setStencilOperations(failOp: StencilOp, passOp: StencilOp, depthFailOp: StencilOp) {
		GLFunc.stencilOp = Vec3i(failOp.opengl, depthFailOp.opengl, passOp.opengl)
	}

	override fun setStencilWriteMask(mask: Int) {
		GLFunc.stencilWriteMask = mask
	}

	override fun setScissors(viewport: Viewport) {
		GLFunc.scissorBox = Vec4i(viewport.bl, viewport.size)
	}

	override fun getMaxTessellationPatchSize(): Int {
		return GLFunc.maxPatchVertices
	}

	override fun getMaxTessellationLevel(): Int {
		return GLFunc.maxTessLevel
	}

	override fun getMaxComputeWorkgroupSize(): Vec3i = GLFunc.maxComputeWorkGroupSize

	override fun getMaxComputeWorkgroups(): Vec3i = GLFunc.maxComputeWorkGroupCount

	override fun getMaxComputeWorkgroupInvocations(): Int = GLFunc.maxComputeWorkGroupInvocations

	override fun getMaxViewport(): Vec2i = GLFunc.maxViewPort

	override fun updateGui(mat: Mat4, aspectRatio: Float) {
		glm.ortho(-aspectRatio, aspectRatio, -1f, 1f, mat)
	}

	override fun createFramebuffer(width: Int, height: Int, colourFormat: TextureFormat, depthStencilFormat: TextureFormat, usage: Collection<TextureUsage>): Framebuffer {
		return OpenGlFramebuffer(width, height, colourFormat, depthStencilFormat)
	}

	override fun delete() {}
}