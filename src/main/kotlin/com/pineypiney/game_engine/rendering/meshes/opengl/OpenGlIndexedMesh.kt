package com.pineypiney.game_engine.rendering.meshes.opengl

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.util.GLFunc
import kool.cap
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL31C.*
import java.nio.ByteBuffer

open class OpenGlIndexedMesh protected constructor(override val buffers: List<OpenGlVertexBuffer>, val count: Int) : OpenGlMesh() {

	private val EBO = if (GLFunc.isLoaded) glGenBuffers() else -1

	constructor(buffers: List<Pair<ByteBuffer, Map<VertexAttribute<*, *>, Long>>>, indices: IntArray) : this(buffers.map { OpenGlVertexBuffer(it.second) }, indices.size) {
		if (GLFunc.isLoaded) {

			glBindVertexArray(VAO)
			var index = 0
			for ((i, data) in buffers.withIndex()) {
				this.buffers[i].setData(data.first, index)
				index += data.second.size
			}

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

			glBindVertexArray(0)
			glBindBuffer(GL_ARRAY_BUFFER, 0)
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
		}
	}

	constructor(vertices: ByteBuffer, attributes: Iterable<VertexAttribute<*, *>>, indices: IntArray) : this(listOf(vertices to Mesh.createAttributes(attributes)), indices)

	fun getIndices(): ByteBuffer {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
		val buffer = BufferUtils.createByteBuffer(glGetBufferParameteri(GL_ELEMENT_ARRAY_BUFFER, GL_BUFFER_SIZE))
		glGetBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0L, buffer)
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
		return buffer
	}

	fun getIndicesInts(): IntArray {
		val buffer = getIndices().asIntBuffer()
		val array = IntArray(buffer.cap)
		buffer.get(array)
		return array
	}

	override fun bind() {
		super.bind()
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
	}

	override fun draw(api: RenderingApi, mode: Int) {
		api.drawIndexed(count, mode, 0)
	}

	override fun drawInstanced(api: RenderingApi, amount: Int, mode: Int) {
		api.drawIndexedInstanced(count, mode, amount)
	}

	override fun delete() {
		super.delete()
		glDeleteBuffers(EBO)
	}

	companion object {
		fun empty() = OpenGlIndexedMesh(emptyList(), 0)
	}
}
