package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.window.Viewport
import glm_.vec4.Vec4i
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL33C

class OpenGlRendering : RenderingApi {

	override fun bindShader(handle: Int) {
		GL33C.glUseProgram(handle)
	}

	override fun bindPipeline(handle: Long, bindPoint: Int) {
		throw UnsupportedOperationException("OpenGL shaders should have Integer handles")
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

	override fun setScissors(viewport: Viewport) {
		GLFunc.scissor = Vec4i(viewport.bl, viewport.size)
	}
}