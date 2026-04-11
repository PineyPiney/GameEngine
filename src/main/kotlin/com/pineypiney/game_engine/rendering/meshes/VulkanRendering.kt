package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.window.Viewport
import org.lwjgl.vulkan.VK10

class VulkanRendering(val cmd: PoolAndBuffer) : RenderingApi {

	override fun bindShader(handle: Int) {
		throw UnsupportedOperationException("Vulkan shaders should have Long handles")
	}

	override fun bindPipeline(handle: Long, bindPoint: Int) {
		VK10.vkCmdBindPipeline(cmd.buffer, bindPoint, handle)
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

	override fun setScissors(viewport: Viewport) {
		cmd.setScissors(viewport)
	}
}