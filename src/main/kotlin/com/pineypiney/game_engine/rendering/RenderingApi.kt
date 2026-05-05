package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.resources.shaders.vulkan.pipeline.VulkanPipeline
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.window.Viewport

interface RenderingApi {

	fun bindShader(handle: Int)
	fun bindPipeline(handle: Long, bindPoint: Int)

	fun bindTextureToPipeline(pipeline: VulkanPipeline, uniformName: String, texture: Texture)
	fun updateUniforms(pipeline: VulkanPipeline)


	fun bindVertices(handle: Int)
	fun bindIndices(handle: Int)
	fun bindIndices(handle: Long, offset: Long, type: Int)
	fun draw(vertexCount: Int, drawMode: Int, firstVertex: Int = 0)
	fun drawInstanced(vertexCount: Int, drawMode: Int, instanceCount: Int, firstVertex: Int = 0, firstInstance: Int = 0)
	fun drawIndexed(indexCount: Int, drawMode: Int, firstIndex: Int = 0)
	fun drawIndexedInstanced(indexCount: Int, drawMode: Int, instanceCount: Int, firstIndex: Int = 0, firstInstance: Int = 0)

	fun setViewport(viewport: Viewport)
	fun setScissors(viewport: Viewport)
}