package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.util.GLFunc
import kool.cap
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL31C.*
import java.nio.ByteBuffer

open class OpenGlIndexedMesh protected constructor(override val attributes: Map<VertexAttribute<*, *>, Long>, override val count: Int) : OpenGlMesh() {

	private val EBO = if (GLFunc.isLoaded) glGenBuffers() else -1

	constructor(vertices: FloatArray, attributes: Map<VertexAttribute<*, *>, Long>, indices: IntArray) : this(attributes, indices.size) {
		if (GLFunc.isLoaded) {
			glBindVertexArray(VAO)

			glBindBuffer(GL_ARRAY_BUFFER, VBO)
			glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

			// How to read non-indices array
			setAttributes()

			glBindVertexArray(0)
			glBindBuffer(GL_ARRAY_BUFFER, 0)
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
		}
	}

	constructor(vertices: FloatArray, attributes: Iterable<VertexAttribute<*, *>>, indices: IntArray) : this(vertices, Mesh.createAttributes(attributes), indices)

	constructor(vertices: ByteBuffer, attributes: Iterable<VertexAttribute<*, *>>, indices: IntArray) : this(Mesh.createAttributes(attributes), indices.size) {
		if (GLFunc.isLoaded) {
			glBindVertexArray(VAO)

			glBindBuffer(GL_ARRAY_BUFFER, VBO)
			glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

			// How to read non-indices array
			setAttributes()

			glBindVertexArray(0)
			glBindBuffer(GL_ARRAY_BUFFER, 0)
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
		}
	}

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

	override fun bind(api: RenderingApi) {
		super.bind(api)
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
		glDeleteBuffers(VBO)
		glDeleteBuffers(EBO)
	}

	companion object {
		fun empty() = OpenGlIndexedMesh(emptyMap(), 0)
	}
}
