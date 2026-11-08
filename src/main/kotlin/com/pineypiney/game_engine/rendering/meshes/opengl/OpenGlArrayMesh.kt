package com.pineypiney.game_engine.rendering.meshes.opengl

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15C.glBindBuffer
import org.lwjgl.opengl.GL30C.glBindVertexArray
import org.lwjgl.opengl.GL31C
import java.nio.ByteBuffer

open class OpenGlArrayMesh : OpenGlMesh {

	final override val buffers: List<OpenGlVertexBuffer>
	val count: Int

	constructor(buffers: List<Pair<ByteBuffer, Map<VertexAttribute<*, *>, Long>>>) : super() {
		this.buffers = buffers.map { OpenGlVertexBuffer(it.second) }
		this.count = buffers.first().first.remaining() / this.buffers.first().stride

		glBindVertexArray(VAO)
		var index = 0
		for ((i, data) in buffers.withIndex()) {
			this.buffers[i].setData(data.first, index)
			index += data.second.size
		}

		glBindVertexArray(0)
		glBindBuffer(GL_ARRAY_BUFFER, 0)
	}

	override fun bind(api: RenderingApi) {
		glBindVertexArray(VAO)
	}

	override fun draw(api: RenderingApi, mode: Int) {
		GL11C.glDrawArrays(mode, 0, count)
	}

	override fun drawInstanced(api: RenderingApi, amount: Int, mode: Int) {
		GL31C.glDrawArraysInstanced(mode, 0, count, amount)
	}
}